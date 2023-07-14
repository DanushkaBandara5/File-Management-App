package lk.ijse.dep10.app.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.apache.commons.io.FileUtils;

import javax.swing.*;
import java.io.*;
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

    public void initialize(){

        prgLbl.setVisible(false);
        btnDelete.setDisable(true);
        btnCopy.setDisable(true);
        btnReset.setDisable(true);
        btnMove.setDisable(true);
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
    void btnCopyOnAction(ActionEvent event)  {
        if(sourceFile==null || targetFolder==null){
            new Alert(Alert.AlertType.ERROR,"Enter valid Source File / Target Director to proceed").show();
        }
        try {
            completedBytes=0;
            if(sourceFile.isFile()){
                totalBytes=sourceFile.length();
                File file = new File(targetFolder, sourceFile.getName());
                writeToFile(sourceFile,file);
            }else {
                totalBytes = FileUtils.sizeOfDirectory(sourceFile);
                folderCopy(sourceFile,targetFolder);
            }
            btnCopy.setDisable(true);
            btnMove.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong, please try again!").show();

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
        sourceFile=null;
        targetFolder=null;
        prgBar.setProgress(0);
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
        if(sourceFile==null){
            new Alert(Alert.AlertType.ERROR,"Select a Source File First").show();
            txtSource.requestFocus();
            return;
        }
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder To Copy");
        targetFolder = directoryChooser.showDialog(btnTarget.getScene().getWindow());
        if (targetFolder == null) return;
        if (targetFolder.equals(sourceFile.getParentFile())) {
            Optional<ButtonType> btnSelect = new Alert(Alert.AlertType.CONFIRMATION, "You have selected the same folder, are you going to continue?").showAndWait();
            if (btnSelect.get() == ButtonType.NO || btnSelect.isEmpty()) return;
            sourceFile = new File(sourceFile.getParent(), sourceFile.getName().concat("-copy"));
            txtSource.setText(sourceFile.getAbsolutePath());
            txtTarget.setText(targetFolder.getAbsolutePath());

        }else {
            txtTarget.setText(targetFolder.getAbsolutePath());
        }
        btnMove.setDisable(false);
        btnCopy.setDisable(false);
    }
    public void folderCopy(File file, File target) throws IOException {
        File newTarget = new File(target, file.getName());
        newTarget.mkdir();
        File[] newArray = file.listFiles();
        for (File file1 : newArray) {
            if (file1.isFile()) {
                File updatedTargetFile = new File(newTarget, file1.getName());
                writeToFile(file1,updatedTargetFile);
            } else if (file1.isDirectory()) {
                folderCopy(file1, newTarget);
            }
        }

    }
    private void writeToFile(File source,File target) throws IOException {
        FileInputStream fis = new FileInputStream(source);
        FileOutputStream fos = new FileOutputStream(target);
        while (true) {
            byte[] bytes = new byte[1024 * 10];
            int read = fis.read(bytes);
            if (read == -1) break;
            completedBytes+=read;
            System.out.println(totalBytes);
            fos.write(bytes, 0, read);
            prgBar.setProgress((completedBytes*100)/totalBytes);
        }
        fis.close();
        fos.close();
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

}
