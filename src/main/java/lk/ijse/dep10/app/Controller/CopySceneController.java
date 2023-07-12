package lk.ijse.dep10.app.Controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
    public void initialize(){
        prgLbl.setVisible(false);
    }

    @FXML
    void BtnDeleteOnAction(ActionEvent event) {
        if(sourceFile!=null) {
            sourceFile.delete();

        }

    }

    @FXML
    void btnCopyOnAction(ActionEvent event) throws InterruptedException {
        if(sourceFile==null || targetFolder==null){
            new Alert(Alert.AlertType.ERROR,"Enter valid Source File / Target Director to proceed").show();
        }
        prgLbl.setVisible(true);
        File targetFile = new File(targetFolder, sourceFile.getName());
        if(targetFile.exists()) {
            Optional<ButtonType> btnResult = new Alert(Alert.AlertType.CONFIRMATION, "File already Exists, do you want to proceed").showAndWait();
            if(btnResult.isEmpty()||btnResult.get()==ButtonType.NO) return;
        }

//        btnCopy.getScene().getWindow().setHeight(325);
//
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                FileInputStream fis = new FileInputStream(sourceFile);
                FileOutputStream fos = new FileOutputStream(targetFile);

                double write = 0;

                while (true){
                    byte[] buffer =new byte[1024*100];
                    int read = fis.read(buffer);
                    if(read==-1)break;
                    write+=read;
                    fos.write(buffer,0,read);
                    updateMessage(String.format("%02.1f",(double)write*100/sourceFile.length())+"% Completed");
                    updateProgress(write,sourceFile.length());
                }
                fis.close();
                fos.close();
                return null;
            }
        };

        task.exceptionProperty().addListener(observable -> {
            task.getException().printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Something went wrong, please try again!").show();
        });
        prgLbl.textProperty().bind(task.messageProperty());
        prgBar.progressProperty().bind(task.progressProperty());
        Thread t1 = new Thread(task, "t1");
        t1.start();
        t1.join();


    }

    @FXML
    void btnMoveOnAction(ActionEvent event) {
        btnCopy.fire();
        btnDelete.fire();



    }

    @FXML
    void btnResetOnAction(ActionEvent event) {
        sourceFile=null;
        targetFolder=null;
        txtSource.clear();
        txtTarget.clear();
        prgBar.progressProperty().unbind();
        prgBar.setProgress(0);
        prgLbl.textProperty().unbind();
        prgLbl.setText("");
        prgLbl.setVisible(false);



    }

    @FXML
    void btnSourceOnAction(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Source File");
        sourceFile = fileChooser.showOpenDialog(btnSource.getScene().getWindow());
        if(sourceFile==null)return;
        txtSource.setText(sourceFile.getAbsolutePath());
    }

    @FXML
    void btnTargetOnAction(ActionEvent event) {
        DirectoryChooser directoryChooser =new DirectoryChooser();
        directoryChooser.setTitle("Select Folder To Copy");
        targetFolder = directoryChooser.showDialog(btnTarget.getScene().getWindow());
        if(targetFolder==null) return;
        txtTarget.setText(targetFolder.getAbsolutePath());

    }

}
