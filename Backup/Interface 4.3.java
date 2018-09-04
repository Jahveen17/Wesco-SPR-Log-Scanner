import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Graphical User Interface class for Wesco Positive32 log scanner.
 * 
 * Last Modified: 8/14/2018
 * @author Javen Zamojcin
 */

public class Interface extends Application {
	
	public ProgressBar pb = new ProgressBar(0);
	public ProgressIndicator pi = new ProgressIndicator(0);
	
	private BorderPane bp = new BorderPane();
	private Stage stage = new Stage();
	private Scene scene = new Scene(bp, 400, 200);
	
	private Button run = new Button();
	private Button browsePosListPath = new Button(" Input ");
	private Button browseOutputPath = new Button(" Output ");
	private CheckBox runAutomatically = new CheckBox("Run Automatically");
	private TextField posListPath = new TextField();
	private TextField outputPath = new TextField();
	private Label signiture = new Label("Made by Javen");
	private HBox posListPathBox = new HBox(5);
	private HBox outputPathBox = new HBox(5);
	private HBox progressBox = new HBox(10);
	private VBox pathBox = new VBox(5);
	
	private File configFile = new File("config.txt");
	private File posListFile = new File("PosList.txt");
	private JFileChooser fc = new JFileChooser(configFile.getAbsolutePath());
	
	public static void main(String[] args) {
		launch();
	}
	
	@Override
	public void start(Stage arg0) throws Exception {
		
		initializeConfigFile();
		initializeHBoxes();
		initializeProps();
		
		bp.setTop(progressBox);
		bp.setCenter(pathBox);
		
		stage.setTitle("Wesco SPR Log Parser V4.3");
		stage.setResizable(false);
		stage.setScene(scene);
		stage.show();
		
		if ( runAutomatically.isSelected() ) { runReader(); }
		
	}
	
	@SuppressWarnings("deprecation")
	private void runReader() {
		
		run.setDisable(true);
		browsePosListPath.setDisable(true);
		browseOutputPath.setDisable(true);
		posListPath.setDisable(true);
		outputPath.setDisable(true);
		
		Reader reader = new Reader(pi, pb, posListPath.getText(), outputPath.getText());
		Thread t = new Thread(reader, "thread");
		t.start();
		
		stage.setOnCloseRequest(event -> { t.stop(); });
		
	}
	
	private void writeConfigFile(Boolean runAuto, String inputPath, String outputPath) throws FileNotFoundException {
		
		PrintWriter printer = new PrintWriter(new FileOutputStream(configFile, false));
		printer.println("RunAutomatically: " + runAuto);
		printer.println("PosList: " + inputPath);
		printer.println("OutputDirectory: " + outputPath);
		printer.close();
		
	}
	
	private void initializeConfigFile() throws IOException {

		if ( !configFile.exists() ) {
			
			File outputFolder = new File("output");
			outputFolder.mkdir();
			writeConfigFile(false, posListFile.getAbsolutePath(), outputFolder.getAbsolutePath());
			PrintWriter printer = new PrintWriter(new FileOutputStream(posListFile, false));
			printer.close();
			
		} 

		Scanner input = new Scanner(configFile);
		
		while ( input.hasNext() ) {
			
			if ( input.next().contains("RunAutomatically:") ) {
				
				String temp = input.next();
				
				if ( temp.contains("true")) { 
					
					runAutomatically.setSelected(true);
					
				} else { runAutomatically.setSelected(false); }
			}
			
			if ( input.next().contains("PosList:") ) { posListPath.setText(input.next()); }
			
			if ( input.next().contains("OutputDirectory:") ) { outputPath.setText(input.next()); }
			
		}
		
		input.close();
	}
		
	private void initializeHBoxes() {
	
		posListPathBox.setPadding(new Insets(5, 5, 5, 5 ));
		posListPathBox.setStyle("-fx-background-color: #336699;");
		posListPathBox.getChildren().addAll(posListPath, browsePosListPath);
		
		outputPathBox.setPadding(new Insets(5, 5, 5, 5 ));
		outputPathBox.setStyle("-fx-background-color: #336699;");
		outputPathBox.getChildren().addAll(outputPath, browseOutputPath);
		
		progressBox.setPadding(new Insets(10, 10, 10, 10 ));
		progressBox.setStyle("-fx-background-color: #336699;");
		progressBox.getChildren().addAll(run, runAutomatically, pb, pi);
		
		pathBox.setPadding(new Insets(10, 10, 10, 10 ));
		pathBox.setStyle("-fx-background-color: #336699;");
		pathBox.getChildren().addAll(posListPathBox, outputPathBox, signiture);
	}
	
	private void initializeProps() {
		
		run.setText("Start");
		run.setPrefSize(75, 40);
		Tooltip runHelper = new Tooltip("Begins process to parse log files. Files are taken from input directory path and completed files are placed in output directory path.");
		Tooltip.install(run, runHelper);
		run.setOnAction(event -> { runReader(); });
		
		pb.setPrefSize(125, 30);
		Tooltip pbHelper = new Tooltip("Total Transactions Completed for File");
		Tooltip.install(pb, pbHelper);
		
		pi.setPrefSize(100, 100);
		Tooltip piHelper = new Tooltip("Total Files Completed");
		Tooltip.install(pi, piHelper);
		
		browsePosListPath.setPrefSize(75, 30);
		browsePosListPath.setOnAction(event -> {
	
			fc.setDialogTitle("Choose PosList File");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fc.showOpenDialog(null);
			
			if ( returnValue == JFileChooser.APPROVE_OPTION) { 
				
				posListPath.setText(fc.getSelectedFile().getAbsolutePath()); 
			
				try {
					
					writeConfigFile(runAutomatically.isSelected(), posListPath.getText(), outputPath.getText());
					
				} catch (FileNotFoundException e1) { e1.printStackTrace(); }	
			}
		});
		
		browseOutputPath.setPrefSize(75, 30);
		browseOutputPath.setOnAction(event -> {
			
			fc.setDialogTitle("Choose Output Directory");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int returnValue = fc.showOpenDialog(null);
			
			if ( returnValue == JFileChooser.APPROVE_OPTION) { 
				
				outputPath.setText(fc.getSelectedFile().getAbsolutePath());
				
				try {
					
					writeConfigFile(runAutomatically.isSelected(), posListPath.getText(), outputPath.getText());
					
				} catch (FileNotFoundException e1) { e1.printStackTrace(); }	
			}
		});
	
		runAutomatically.setOnAction(e -> { 
			
			try {
				
				if ( runAutomatically.isSelected() ) {

					writeConfigFile(true, posListPath.getText(), outputPath.getText()); 

				} else { writeConfigFile(false, posListPath.getText(), outputPath.getText()); }
				
			} catch (FileNotFoundException e2 ) { e2.printStackTrace(); }
			
		});
		
		Tooltip autoRunHelper = new Tooltip("If selected, program will automatically run on program startup.");
		Tooltip.install(runAutomatically, autoRunHelper);
		
		posListPath.editableProperty().set(false);
		posListPath.setPrefSize(300, 30);
		Tooltip inputPathHelper = new Tooltip("Input Path Directory");
		Tooltip.install(posListPath, inputPathHelper);
		
		outputPath.editableProperty().set(false);
		outputPath.setPrefSize(300, 30);
		Tooltip outputPathHelper = new Tooltip("Output Path Directory");
		Tooltip.install(outputPath, outputPathHelper);	
		
	}
}

