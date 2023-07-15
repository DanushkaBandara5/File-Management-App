package lk.ijse.dep10.app.Controller;

import com.sun.tools.javac.Main;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class CopySceneController {

    @FXML
    private Button btnCopy;

    @FXML
    private Button btnDelete;

    @FXML
    private Button btnMove;

    @FXML
    private Button btnReset;

    @FXML
    private Button btnSource;

    @FXML
    private Button btnTarget;

    @FXML
    private ProgressBar prgBar;

    @FXML
    private Label prgLbl;

    @FXML
    private TextField txtSource;

    @FXML
    private TextField txtTarget;
    private File sourceFile;
    private File targetFolder;
    private long totalBytes;
    private long completedBytes;

    public void initialize() {
        prgLbl.setVisible(false);
        btnDelete.setDisable(true);
        btnCopy.setDisable(true);
        btnReset.setDisable(true);
        btnMove.setDisable(true);
        prgLbl.setVisible(true);

    }

    @FXML
    void BtnDeleteOnAction(ActionEvent event) {
        if (sourceFile == null) return;
        if (sourceFile.isFile()) {
            sourceFile.delete();
            return;
        }
        folderDelete(sourceFile);
    }

    @FXML
    void btnCopyOnAction(ActionEvent event) {
        if (sourceFile == null || targetFolder == null) {
            new Alert(Alert.AlertType.ERROR, "Enter valid Source File / Target Director to proceed").show();
        }
        try {
            completedBytes = 0;
            if (sourceFile.isFile()) {
                totalBytes = sourceFile.length();
                File file;
                if (sourceFile.getParent().equals(targetFolder)) {
                    file = new File(targetFolder, sourceFile.getName().concat("-copy"));
                } else {
                    file = new File(targetFolder, sourceFile.getName());
                }
                writeToFile(sourceFile, file);
            } else {
                totalBytes = getDirectorySizeLegacy(sourceFile);
                folderCopy(sourceFile, targetFolder);
            }
            btnCopy.setDisable(true);
            btnMove.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong, please try again!").show();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    @FXML
    void btnMoveOnAction(ActionEvent event) {
        btnCopy.fire();
        btnDelete.fire();
        btnCopy.setDisable(true);
        btnMove.setDisable(true);
    }

    @FXML
    void btnResetOnAction(ActionEvent event) {
        txtSource.clear();
        txtTarget.clear();
        btnDelete.setDisable(true);
        sourceFile = null;
        targetFolder = null;
        prgBar.progressProperty().unbind();
        prgBar.setProgress(0);
        prgLbl.textProperty().unbind();
        prgLbl.setText("");
    }

    @FXML
    void btnSourceOnAction(ActionEvent event) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.showOpenDialog(null);
        if (chooser.getSelectedFile() == null) return;
        sourceFile = chooser.getSelectedFile();
        txtSource.setText(sourceFile.getAbsolutePath());
        btnDelete.setDisable(false);
        btnReset.setDisable(false);


    }

    @FXML
    void btnTargetOnAction(ActionEvent event) {
        if (sourceFile == null) {
            new Alert(Alert.AlertType.ERROR, "Select a Source File First").show();
            txtSource.requestFocus();
            return;
        }
        JFileChooser selectedDirectory = new JFileChooser();
        selectedDirectory.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selectedDirectory.showOpenDialog(null);
        if (selectedDirectory.getSelectedFile() == null) return;
        targetFolder = selectedDirectory.getSelectedFile();
        if (targetFolder.equals(sourceFile.getParentFile())) {
            Optional<ButtonType> btnSelect = new Alert(Alert.AlertType.CONFIRMATION, "You have selected the same folder, are you going to continue?").showAndWait();
            if (btnSelect.get() == ButtonType.NO || btnSelect.isEmpty()) return;


        } else {
            txtTarget.setText(targetFolder.getAbsolutePath());
        }
        btnMove.setDisable(false);
        btnCopy.setDisable(false);
    }

    public void folderCopy(File file, File target) throws IOException, InterruptedException {

        File newTarget = new File(target, file.getName());
        newTarget.mkdir();
        File[] sourceFileArray = file.listFiles();
        for (File file1 : sourceFileArray) {
            if (file1.isFile()) {
                File updatedTargetFile = new File(newTarget, file1.getName());
                writeToFile(file1, updatedTargetFile);
            } else if (file1.isDirectory()) {
                folderCopy(file1, newTarget);
            }
        }
    }

    private void writeToFile(File source, File target) throws IOException, InterruptedException {
                Task task = new Task<Void>(){
                    @Override
                    protected Void call() throws Exception {
                        FileInputStream fis = new FileInputStream(source);
                        FileOutputStream fos = new FileOutputStream(target);
                        while (true) {
                            byte[] bytes = new byte[1024 * 10];
                            int read = fis.read(bytes);
                            if (read == -1) break;
                            completedBytes += read;
                            fos.write(bytes, 0, read);
                            updateProgress(completedBytes,totalBytes);
                            updateMessage(new Double((completedBytes*100)/totalBytes).toString());

                        }
                        fis.close();
                        fos.close();
                        return null;
                    }
                };
        Thread thread = new Thread(task);
        task.exceptionProperty().addListener(observable -> {
            task.getException().printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong, please try again!").show();
        });
        thread.start();

        prgLbl.textProperty().bind(task.messageProperty());
        prgBar.progressProperty().bind(task.progressProperty());


    }


    public void folderDelete(File file) {
        File[] files = file.listFiles();

        for (File file1 : files) {
            if (file1.isFile()) {
                file1.delete();
            } else {
                folderDelete(file1);
            }

        }
        file.delete();
    }
    public  long getDirectorySizeLegacy(File dir) {

        long length = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile())
                    length += file.length();
                else
                    length += getDirectorySizeLegacy(file);
            }
        }
        return length;
    }

}
