package pt3image;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class PT3Controller implements Initializable{
    
    @FXML
    private Button chooseFile;
    @FXML
    private Button saveFile;
    @FXML
    private Button threadButton;
    @FXML
    private Label timeLabel;
    @FXML
    private Label threadNumberLabel;
    @FXML
    private TableView imageTable;
    @FXML
    private TextArea textField;
    @FXML 
    TableColumn<ImageProcessingJob, String> imageNameColumn;
    @FXML 
    TableColumn<ImageProcessingJob, Double> progressColumn;
    @FXML 
    TableColumn<ImageProcessingJob, String> statusColumn;
    
    File outputDir;
    ObservableList<ImageProcessingJob> jobs;
    List<File> selectedFiles;
    int threadNumber = 1;
    ForkJoinPool pool = new ForkJoinPool(1);
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        imageNameColumn.setCellValueFactory( //nazwa pliku
        p -> new SimpleStringProperty(p.getValue().getFile().getName()));
        statusColumn.setCellValueFactory( //status przetwarzania
        p -> p.getValue().messageProperty());
        progressColumn.setCellFactory( //wykorzystanie paska postepu
        ProgressBarTableCell.<ImageProcessingJob>forTableColumn());
        progressColumn.setCellValueFactory( //postep przetwarzania
        p -> p.getValue().progressProperty().asObject());
        
        List<ImageProcessingJob> list = new ArrayList<>();
        jobs = FXCollections.observableList(list);
        imageTable.setItems(jobs);
    }

    @FXML
    private void selectFiles(ActionEvent event) {
        timeLabel.setText("");
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG images", "*.jpg"));
        selectedFiles = fileChooser.showOpenMultipleDialog(null);
        jobs.clear();
        selectedFiles.stream().forEach(f -> {jobs.add(new ImageProcessingJob(f));});
    }

    @FXML
    private void saveFiles(ActionEvent event) throws IOException {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        outputDir = directoryChooser.showDialog(null);
        new Thread(this::backgroundJob).start();
    }
    
    private void backgroundJob(){
        //operacje w tle
        jobs.stream().forEach(j -> {
            j.setOutputDir(outputDir);
            pool.submit(j);
        }); 
        
    }

    @FXML
    private void setThreadNumber(ActionEvent event) {
        String text = textField.getText();
        threadNumber = Integer.parseInt(text);
        switch(threadNumber){
            case 0:
                pool = new ForkJoinPool();
                threadNumberLabel.setText("Domyslna pula watkow");
                break;
            case 1:
                pool = new ForkJoinPool(1);
                threadNumberLabel.setText("Przetwarzanie sekwencyjne");
                break;
            default:
                pool = new ForkJoinPool(threadNumber);
                threadNumberLabel.setText("Uzywane watki " + threadNumber);
                break;
        }
    }
}
