import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Back-end class for Wesco Positive32 log scanner.
 * 
 * Last Modified: 8/16/2018
 * @author Javen Zamojcin
 */

public class Reader implements Runnable {
	
	/**
	 * Version: 5.4
	 * CHANGED:
	 * - Fixed Initial transaction progress bar value
	 * - Console now includes SPR information with most messages
	 * 
	 */
	
	private ArrayList<String> posListDirectories;
	
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	private Stage stage;
	private TextField console;

	private String storeInfo;
	private String date;
	private String posListDirectory;
	private String outputDirectory;
	
	private Boolean fillInputDirectoryFailed;
	
	private int finalTransactionNumber;
	private int firstTransactionNumber;
	private int totalTransactions = 0;
	private int completedTransactions = 0;
	
	public Reader(ProgressIndicator progind, ProgressBar progbar, TextField console, String posListDirectory, String outputDirectory, Stage stage) {
	
		this.posListDirectory = posListDirectory;
		this.outputDirectory = outputDirectory;
		this.progressBar = progbar;
		this.progressIndicator = progind;
		this.console = console;
		this.stage = stage;
		
	}
	
	/**
	 * Starts the log reader class.
	 */
	public void run() {
		
		try {
			
			posListDirectories = initializePosListDirectories();
			
			for ( int j = 0; j < posListDirectories.size(); j++ ) {
				
				fillInputDirectoryFailed = false;
				File directory = fillInputDirectory(j);
				if ( fillInputDirectoryFailed ) { continue; }
				
				initializeLogInfo(directory);
				File outputFile = initializeOutputFile();
				splitAndReadFile(directory, outputFile);
				updateDirectoryProgress( j, posListDirectories.size() );
		
			}
			
			updateConsole("Finished");
			closeStage();
			
		} catch (FileNotFoundException e) { e.printStackTrace(); } 
	}
	
	/**
	 * Scans IP addresses found in PosList.txt and builds a directory path.
	 * @return ArrayList<String>
	 */
	private ArrayList<String> initializePosListDirectories() {
		
		ArrayList<String> temp = new ArrayList<String>();
		
		try {
			
			Scanner input = new Scanner(new File(posListDirectory));
			String tempLocation = input.next();
			
			while ( !tempLocation.equals(";") ) {
				
				temp.add("\\" + "\\" + tempLocation + "\\" + "c$" + "\\" + "Positive" + "\\" + "Log");
				tempLocation = input.next();
				
			}
			
			input.close();
			
		} catch (FileNotFoundException e) { 
			e.printStackTrace(); 
			updateConsole("FileNotFoundException: PosList.txt");
		}
		
		updateConsole("Total Directories Listed: " + temp.size());
		
		if ( temp.size() == 0 ) { closeStage(); }
		
		return temp;
	}
	
	/**
	* First does a quick parse of the whole file for entries that contain a transaction number, then sends
	* a smaller set of data out to be parsed and output 
	**/
	private void splitAndReadFile(File inputFile, File outputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String tempLine = null;
		updateConsole(storeInfo + ": Pre-parsing File");

		HashMap<Integer, String> transactions = new HashMap<Integer, String>();

		while ( input.hasNextLine() ) {

			tempLine = input.nextLine();

			for (Integer i = firstTransactionNumber; i <= finalTransactionNumber; i++){
				
				if (tempLine.contains("" + i)){

					if (transactions.get(i) == null) {
	
						transactions.put(i, tempLine);
						updateTransactionProgress();
						
					} else { transactions.put(i, transactions.get(i) + "\n" + tempLine); }
				}
			}
		}
		
		updateConsole(storeInfo + ": File Pre-parsed");
		input.close();
		for (Integer i = firstTransactionNumber; i < finalTransactionNumber; i++) { readFile(i, transactions.get(i), outputFile); }
	}
	
	/**
	 * Parses strings taken from log file to create transaction objects with desired information.
	 * @param transactionNumber
	 * @param inputTxString
	 * @param outputFile
	 * @throws FileNotFoundException
	 */
	private void readFile(Integer transactionNumber, String inputTxString, File outputFile) throws FileNotFoundException {
			
			Scanner input = new Scanner(inputTxString);
			Transaction trans = new Transaction(transactionNumber);

			while ( input.hasNext() ) {
				
				String tempTime = input.next();
				String tempLine = input.nextLine();
				
				if ( tempLine.contains("StartTransaction") && tempLine.contains("#" + transactionNumber) ) {
					
					trans.setStartTime(tempTime);
					
				} else if ( tempLine.contains("EndTransaction") && tempLine.contains("#" + transactionNumber) ) {
					
					trans.setEndTime(tempTime);
					
				} else if ( tempLine.contains("FTransType=") && tempLine.contains("" + transactionNumber) ) {
					
					if ( trans.getTransactionType1().equals("-1") ) {
						
						trans.setTransactionType1(parseTransactionType(tempLine));
						
					} else {
						
						trans.setTransactionType2(parseTransactionType(tempLine));
						
					}
					
				} else if ( tempLine.contains("PrePayTrsNumber=") && tempLine.contains("#" + transactionNumber)) {
					
					trans.setIsPrepaid("Prepaid");
					
				} else if ( !input.hasNextLine() ) {
					
					if ( trans.getEndTime().equals("-1") ) { trans.setEndTime(""); }
					
					if ( trans.getTransactionType2().equals("-1") ) { trans.setTransactionType2(""); }
				}
			}
			
			input.close();
			updateConsole(storeInfo + "- Transaction Completed: " + trans.getTransactionNumber());
			writeTransactionInfo(outputFile, trans);
			updateTransactionProgress();
		}
	
	/**
	 * Pulls prerequisite information from log file.
	 * @param inputFile
	 * @throws FileNotFoundException
	 */
	private void initializeLogInfo( File inputFile ) throws FileNotFoundException {
		
		completedTransactions = 0;
		totalTransactions = 0;
		updateTransactionProgress();
		
		storeInfo = retrieveStoreInfo(inputFile);
		updateConsole("SPR Information: " + storeInfo);
		firstTransactionNumber = retrieveFirstTransactionNumber(inputFile);
		updateConsole(storeInfo + "- First Transaction ID: " + firstTransactionNumber);
		finalTransactionNumber = retrieveFinalTransactionNumber(inputFile, firstTransactionNumber);
		updateConsole("Last Transaction ID: " + finalTransactionNumber);
		totalTransactions = (finalTransactionNumber - firstTransactionNumber);
		updateConsole(storeInfo + "- Total Transactions: " + totalTransactions);
		date = retrieveLogDate(inputFile, true);
		
	}
	
	/**
	 * Generates a blank output text file with appropriate name.
	 * @return
	 * @throws FileNotFoundException
	 */
	private File initializeOutputFile() throws FileNotFoundException {
		
		int copyCounter = 2;
		File output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + 1 + ".txt");

		if ( output.exists() ) {

			output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");

			while ( output.exists() ) { 

				copyCounter++;
				output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");

			}

		} else { copyCounter = 2; }
		
		PrintWriter writer = new PrintWriter(new FileOutputStream(output, false));
		writer.close();
		updateConsole("Output File Initialized");
		
		return output;
	}
	
	/**
	 * Updates the console field in the interface class.
	 * @param message
	 */
	private void updateConsole(String message) { Platform.runLater(() -> { console.setText(message); }); }
	
	/**
	 * Updates progress bar from extended interface class by calculating completed transactions per file.
	 */
	private void updateTransactionProgress() {
		
		if ( totalTransactions == 0 ) { 
			
			Platform.runLater(() -> { progressBar.setProgress( 0.0 ); }); 
			return;
			
		}
		
		completedTransactions++;
		Platform.runLater(() -> { progressBar.setProgress( (completedTransactions + 0.0) / (totalTransactions * 2) ); });
	}
	
	/**
	 * Updates progress indicator (pie chart) from extended interface class by calculating total completed directories.
	 */
	private void updateDirectoryProgress(int completedDirectories, int totalDirectories ) {
		
		if ( totalDirectories == 0 ) { Platform.runLater(() -> { 
			
			progressIndicator.setProgress(0.0); }); 
			return;
		
		}

		Platform.runLater(() -> { progressIndicator.setProgress( (completedDirectories + 0.0) / totalDirectories ); });
	}
	
	/**
	 * Parses strings from transaction log to retrieve transaction type.
	 * @param tempLine
	 * @return transaction type
	 */
	private String parseTransactionType(String tempLine) {
		
		if ( tempLine.contains("Sale")) { 
			
			return "Sale"; 
		
		} else if ( tempLine.contains("Void")) {
			
			return "Void";
			
		} else if ( tempLine.contains("PayOut")) {
			
			return "PayOut";
			
		} else if ( tempLine.contains("PayIn")) {
			
			return "PayIn";
			
		} else if ( tempLine.contains("Drop")) {
			
			return "Drop";
			
		} else if ( tempLine.contains("CloseBank")) {
			
			return "CloseBank";
			
		} else if ( tempLine.contains("OpenBank")) {
			
			return "OpenBank";
			
		} else if ( tempLine.contains("Refund")) {
		
			return "Refund";
			
		} else { return ""; }
	}

	/**
	 * Initializes the input directory to only contain files which contain "Positive32" in the name, and are from
	 * the date from the day before today.
	 */
	private File fillInputDirectory(int posListIndex) {
		
		File[] tempDirectory = new File(posListDirectories.get(posListIndex)).listFiles();
		updateConsole("Working on Directory[" + posListIndex + "]");
		
		try {

			for ( int i = 0; i < tempDirectory.length; i++ ) {	

				if ( tempDirectory[i].getName().contains("Positive32") ) {

					if ( retrieveLogDate(tempDirectory[i], true ).equals(getYesterdaysDate()) ) {

						updateConsole("Log Found: " + tempDirectory[i].getAbsolutePath());
						return tempDirectory[i];

					}
				}
			}
			
		} catch (FileNotFoundException e) { 

			updateConsole("Path Not Found for Directory[" + posListIndex + "]");
			fillInputDirectoryFailed = true;
			return null;
		
		} catch (NullPointerException e) {
			
			updateConsole("Path Not Found for Directory[" + posListIndex + "]");
			fillInputDirectoryFailed = true;
			return null;

		}
		
		fillInputDirectoryFailed = true;
		return null;
	}
	
	/**
	 * Closes the interface class graphical stage.
	 */
	private void closeStage() { Platform.runLater( () -> { stage.close(); } ); }
	
	/**
	 * Returns yesterdays date in format "yyyy-MM-dd".
	 * @return String
	 */
	private String getYesterdaysDate() {
		
		final Calendar cal = Calendar.getInstance();
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		cal.add(Calendar.DATE, -1);
		
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * Writes transaction information withheld in a transaction instance.
	 * @param outputFile
	 * @throws FileNotFoundException
	 */
	private void writeTransactionInfo(File outputFile, Transaction trans) throws FileNotFoundException {
		
		try {
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, true));
			writer.println(trans.getTransactionNumber() + "," + trans.getStartTime() + "," + trans.getEndTime() + "," + trans.getTransactionType1() + "," + 
			trans.getTransactionType2() + "," + trans.getIsPrepaid() + "," + storeInfo + "," + date);	
			writer.close();
			
		} catch (IOException e) { e.printStackTrace(); }
		
	}
	
	/**
	 * Used to initialize transaction number variable; Locates first transaction number used.
	 * @param inputFile
	 * @return transaction number
	 * @return -1 if transaction number is not found
	 * @throws FileNotFoundException
	 */
	private int retrieveFirstTransactionNumber(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		
		while ( input.hasNext() ) {
			
			String temp = input.next();

			if ( temp.contains("StartTransaction") ) {

				temp = input.next();
				
				if ( temp.equals("Trs") ) {
					
					temp = input.next();
					input.close();
					temp = temp.substring(1, temp.length());
					return Integer.parseInt(temp);
					
				}
			}
		}
		
		input.close();
		return -1;
	}
	
	/**
	 * Returns last transaction number in log
	 * @param inputFile
	 * @param firstTransactionNumber
	 * @return -1 if not found
	 * @throws FileNotFoundException
	 */
	private int retrieveFinalTransactionNumber(File inputFile, int firstTransactionNumber) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String firstTransactionNumberString = Integer.toString(firstTransactionNumber).substring(0, 1);
		String temp = null;
		int finalTransactionNumber = -1;
		
		while ( input.hasNextLine() ) {
			
			temp = input.nextLine();
			
			if ( temp.contains("#" + firstTransactionNumberString ) ) {
				
				if ( temp.contains("StartTransaction") ) { finalTransactionNumber = Integer.parseInt(temp.substring(87, 94)); }
				
			}
		}
		
		input.close();
		return finalTransactionNumber;
	}
	
	/**
	 * Retrieves register ID and store number
	 * @param inputFile
	 * @return computerName
	 * @throws FileNotFoundException
	 */
	private String retrieveStoreInfo(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String computerName = null;
		
		while ( computerName == null && input.hasNext() ) {
			
			String temp = input.next();
			
			if ( temp.startsWith("SPR") ) { computerName = temp; }
		}
		
		input.close();
		return computerName;
	}
	
	/**
	 * Retrieves date of log file
	 * @param inputFile
	 * @return date
	 * @throws FileNotFoundException
	 */
	private String retrieveLogDate(File inputFile, Boolean useFileName) throws FileNotFoundException {
		
		if ( useFileName ) {
			
			return inputFile.getName().substring(11, 19);
			
		} else {
			
			Scanner input = new Scanner(inputFile);
			String temp = null;
			
			while ( input.hasNext() ) {
				
				temp = input.next();
				
				if ( temp.contains("Date:")) {
					
					temp = input.next().replace('/', '-');
					input.close();
					return temp;
					
				}
			}
			
			input.close();
		}
		

		return "";
	}
}