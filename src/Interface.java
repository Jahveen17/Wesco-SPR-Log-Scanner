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
 * Last Modified: 8/16/2018
 * @author Javen Zamojcin
 */

public class Interface extends Application {
	
	public ProgressBar pb = new ProgressBar(0);
	public ProgressIndicator pi = new ProgressIndicator(0);
	
	final private String version = "5.4";
	
	private BorderPane mainBP = new BorderPane();
	private BorderPane settingBP = new BorderPane();
	private Stage stage = new Stage();
	private Scene mainScene = new Scene(mainBP, 250, 165);
	Scene settingScene = new Scene(settingBP, 300, 165);
	
	private Button run = new Button(" Start ");
	private Button back = new Button(" Back ");
	private Button settings = new Button(" Settings ");
	private Button browsePosListPath = new Button(" PosList ");
	private Button browseOutputPath = new Button(" Output ");
	
	private int buttonLength = 90;
	private int buttonWidth = 40;
	
	private CheckBox runAutomatically = new CheckBox("Run Automatically");
	
	private TextField console = new TextField();
	private TextField posListPath = new TextField();
	private TextField outputPath = new TextField();
	
	private Label signiture = new Label("Made by Javen");
	private Label versionLabel = new Label("Version: " + version);
	
	private HBox settingTopBox = new HBox(10);
	private HBox posListPathBox = new HBox(5);
	private HBox runBox = new HBox(15);
	private HBox outputPathBox = new HBox(5);
	private HBox consoleBox = new HBox(10);
	private HBox progressBox = new HBox(5);
	private HBox infoBox = new HBox(10);
	private VBox settingRightCornerBox = new VBox(10);
	private VBox pathBox = new VBox(5);
	
	private File configFile = new File("config.txt");
	private File posListFile = new File("PosList.txt");
	private JFileChooser fc = new JFileChooser(configFile.getAbsolutePath());
	
	public static void main(String[] args) { launch(); }
	
	@Override
	public void start(Stage arg0) throws Exception {
		
		initializeConfigFile();
		initializeHBoxes();
		initializeProps();
		
		mainBP.setTop(runBox);
		mainBP.setCenter(progressBox);
		mainBP.setBottom(consoleBox);
		
		settingBP.setTop(settingTopBox);
		settingBP.setCenter(pathBox);
		
		stage.setTitle("SPR Log Parser");
		stage.setResizable(false);
		stage.setScene(mainScene);
		stage.show();
		
		if ( runAutomatically.isSelected() ) { runReader(); }
		
	}
	
	@SuppressWarnings("deprecation")
	private void runReader() {
		
		run.setDisable(true);
		browsePosListPath.setDisable(true);
		browseOutputPath.setDisable(true);
		
		Reader reader = new Reader(pi, pb, console, posListPath.getText(), outputPath.getText(), stage);
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
	
	private void writePosListFile() throws FileNotFoundException {
		
		PrintWriter printer = new PrintWriter(new FileOutputStream(posListFile, false));
		printer.println();
		printer.println(";");
		printer.close();
		
	}
	
	private void initializeConfigFile() throws IOException {

		if ( !configFile.exists() ) {
			
			File outputFolder = new File("output");
			writeConfigFile(false, posListFile.getAbsolutePath(), outputFolder.getAbsolutePath());
			
		} 
		
		if ( !posListFile.exists() ) { writePosListFile(); }

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
	
		infoBox.setStyle("-fx-background-color: #336699;");
		infoBox.getChildren().addAll(versionLabel, signiture);
	
		settingRightCornerBox.setStyle("-fx-background-color: #336699;");
		settingRightCornerBox.getChildren().addAll(runAutomatically, infoBox);
		
		settingTopBox.setPadding(new Insets(15, 10, 5, 12));
		settingTopBox.setStyle("-fx-background-color: #336699;");
		settingTopBox.getChildren().addAll(back, settingRightCornerBox);
		
		posListPathBox.setPadding(new Insets(5, 5, 5, 5 ));
		posListPathBox.setStyle("-fx-background-color: #336699;");
		posListPathBox.getChildren().addAll(posListPath, browsePosListPath);
		
		outputPathBox.setPadding(new Insets(5, 5, 5, 5 ));
		outputPathBox.setStyle("-fx-background-color: #336699;");
		outputPathBox.getChildren().addAll(outputPath, browseOutputPath);
		
		runBox.setPadding(new Insets(15, 10, 10, 15));
		runBox.setStyle("-fx-background-color: #336699;");
		runBox.getChildren().addAll(run, settings);
		
		progressBox.setPadding(new Insets(10, 10, 5, 20 ));
		progressBox.setStyle("-fx-background-color: #336699;");
		progressBox.getChildren().addAll(pb, pi);
		
		consoleBox.setPadding(new Insets(5, 10, 10, 20 ));
		consoleBox.setStyle("-fx-background-color: #336699;");
		consoleBox.getChildren().addAll(console);
		
		pathBox.setPadding(new Insets(10, 10, 10, 10 ));
		pathBox.setStyle("-fx-background-color: #336699;");
		pathBox.getChildren().addAll(posListPathBox, outputPathBox);
	}
	
	private void initializeProps() {
		
		run.setPrefSize(buttonLength, buttonWidth);
		Tooltip runHelper = new Tooltip("Begins process to parse log files. Files are taken from input directory path and completed files are placed in output directory path.");
		Tooltip.install(run, runHelper);
		run.setOnAction(event -> { runReader(); });
		
		settings.setPrefSize(buttonLength, buttonWidth);
		settings.setOnAction(event -> { stage.setScene(settingScene); });
		
		back.setPrefSize(70, 40);
		back.setOnAction(event -> { stage.setScene(mainScene); });
		
		pb.setPrefSize(170, 30);
		Tooltip pbHelper = new Tooltip("Total Transactions Completed for File");
		Tooltip.install(pb, pbHelper);
		
		pi.setMinSize(35, 40);
		Tooltip piHelper = new Tooltip("Total Files Completed");
		Tooltip.install(pi, piHelper);
		
		browsePosListPath.setPrefSize(75, 30);
		browsePosListPath.setOnAction(event -> {
	
			fc.setDialogTitle("Choose PosList File");
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
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
		posListPath.setPrefSize(200, 30);
		Tooltip inputPathHelper = new Tooltip("Input Path Directory");
		Tooltip.install(posListPath, inputPathHelper);
		
		outputPath.editableProperty().set(false);
		outputPath.setPrefSize(200, 30);
		Tooltip outputPathHelper = new Tooltip("Output Path Directory");
		Tooltip.install(outputPath, outputPathHelper);
		
		console.editableProperty().set(false);
		console.setPrefSize(190, 30);
		Tooltip consoleHelper = new Tooltip("Console Message Display");
		Tooltip.install(console, consoleHelper);
		
	}
}

