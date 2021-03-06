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

/**
 * Back-end class for Wesco Positive32 log scanner.
 * 
 * Last Modified: 8/14/2018
 * @author Javen Zamojcin
 */

public class Reader extends Interface implements Runnable {
	
	/**
	 * Version: 4.3
	 * CHANGED:
	 * - Removed archive feature
	 * + Reader now looks at "PosList.txt" for a list of directories to read from.
	 * + Changed "InputPath" to "PosList"
	 * + Reader now only reads logs from the day before.
	 */
	
	private ArrayList<ArrayList<String>> database;
	private ArrayList<String> posListDirectories;
	private ArrayList<File> directory;
	
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;

	private String storeInfo;
	private String date;
	private String posListDirectory;
	private String outputDirectory;
	
	private int finalTransactionNumber;
	private int firstTransactionNumber;
	private int totalTransactions = 0;
	private int completedTransactions = 0;
	private int totalFiles = 0;
	private int completedFiles = 0;
	
	public Reader(ProgressIndicator progind, ProgressBar progbar, String posListDirectory, String outputDirectory) {
	
		this.posListDirectory = posListDirectory;
		this.outputDirectory = outputDirectory;
		this.progressBar = progbar;
		this.progressIndicator = progind;
		
	}
	
	public void run() {
		
		try {

			posListDirectories = initializePosListDirectories();
			
			for ( int j = 0; j < posListDirectories.size(); j++ ) {

				directory = new ArrayList<File>();
				int copyCounter = 2;
				fillInputDirectory(j);	
				totalFiles = directory.size();

				for ( int i = 0; i < totalFiles; i++ ) {

					completedTransactions = 0;
					initializeArrayList(directory.get(i));
					storeInfo = retrieveStoreInfo(directory.get(i));
					date = retrieveLogDate(directory.get(i));
					splitAndReadFile(directory.get(i));
					File output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + 1 + ".txt");

					if (output.exists() ) {

						output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");

						while ( output.exists() ) { 

							copyCounter++;
							output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");

						}

					} else {

						copyCounter = 2;

					}

					writeTransactionInfo(output);
					updateFileProgress();
				}
			}
			
		} catch (FileNotFoundException e) { e.printStackTrace(); } 
	}

	private ArrayList<String> initializePosListDirectories() {
		
		ArrayList<String> temp = new ArrayList<String>();
		
		try {
			
			Scanner input = new Scanner(new File(posListDirectory));
			
			while ( input.hasNext() ) {
				
				temp.add("\\" + input.next() + "\\" + "c$" + "\\Positive\\Log");
				
			}
			
			input.close();
			
		} catch (FileNotFoundException e) { e.printStackTrace(); }
		
		return temp;
	}
	
	/**
	* First does a quick parse of the whole file for entries that contain a transaction number, then sends
	* a smaller set of data out to be parsed and output 
	**/
	private void splitAndReadFile(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String tempLine = null;

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

		input.close();
		for (Integer i = firstTransactionNumber; i < finalTransactionNumber; i++) { readFile(i, transactions.get(i)); }
	}
	
	/**
	 * Updates progress bar from extended interface class by calculating completed transactions per file.
	 */
	private void updateTransactionProgress() {
		
		if ( totalTransactions == 0 ) { progressBar.setProgress(0); }
		completedTransactions++;
		
		Platform.runLater(() -> { progressBar.setProgress( (completedTransactions + 0.0) / (totalTransactions * 2) ); });
	}
	
	/**
	 * Updates progress indicator (pie chart) from extended interface class by calculating total completed files.
	 */
	private void updateFileProgress() {
		
		if ( totalFiles == 0 ) { progressIndicator.setProgress(0); }
		completedFiles++;
		
		Platform.runLater(() -> {
			
			progressIndicator.setProgress( (completedFiles + 0.0) / totalFiles );
			
		});
	}
	
	/**
	 * Scans log file to retrieve and save transaction information.
	 * Time Complexity: O(n)
	 * @param transactionNumber
	 * @param inputTxString
	 * @throws FileNotFoundException
	 * @throws InterruptedException 
	 */
	private void readFile(Integer transactionNumber, String inputTxString) throws FileNotFoundException {
			
			Scanner input = new Scanner(inputTxString);

			while ( input.hasNext() ) {
				
				String tempTime = input.next();
				String tempLine = input.nextLine();
				
				if ( tempLine.contains("StartTransaction") && tempLine.contains("#" + transactionNumber) ) {
					
					database.get(0).set(transactionNumber - firstTransactionNumber, tempTime);
					
				} else if ( tempLine.contains("EndTransaction") && tempLine.contains("#" + transactionNumber) ) {
					
					database.get(1).set(transactionNumber - firstTransactionNumber, tempTime);
					
				} else if ( tempLine.contains("FTransType=") && tempLine.contains("" + transactionNumber) ) {
					
					if ( database.get(2).get(transactionNumber - firstTransactionNumber).equals("-1") ) {
						
						database.get(2).set(transactionNumber - firstTransactionNumber, (parseTransactionType(tempLine)));
						
					} else {
						
						database.get(3).set(transactionNumber - firstTransactionNumber, (parseTransactionType(tempLine)));
						
					}
					
				} else if ( tempLine.contains("PrePayTrsNumber=") && tempLine.contains("#" + transactionNumber)) {
					
					database.get(4).set(transactionNumber - firstTransactionNumber, "Prepaid");
					
				} else if ( !input.hasNextLine() ) {
					
					if ( database.get(1).get(transactionNumber - firstTransactionNumber).equals("-1") ) {
						database.get(1).set(transactionNumber - firstTransactionNumber, "");
					}
					
					if ( database.get(3).get(transactionNumber - firstTransactionNumber).equals("-1") ) {
						database.get(3).set(transactionNumber - firstTransactionNumber, "");
					}
				}
			}
			
			input.close();
			updateTransactionProgress();
		}
	
	/**
	 * Creates and fills database ArrayList which holds individual ArrayLists for each type of transaction information.
	 * Also instantiates variables which assist the log scanning process.
	 * @param inputFile
	 * @throws FileNotFoundException
	 */
	private void initializeArrayList( File inputFile ) throws FileNotFoundException {
		
		firstTransactionNumber = retrieveFirstTransactionNumber(inputFile);
		finalTransactionNumber = retrieveFinalTransactionNumber(inputFile, firstTransactionNumber);
		totalTransactions = (finalTransactionNumber - firstTransactionNumber);
		
		//database[0] = startTimes; database[1] = endTimes; database[2] = FTransType1; database[3] = FTransType2; database[4] = isPrepaid
		database = new ArrayList<ArrayList<String>>(5);
		
		for ( int i = 0; i < 5; i++ ) { database.add(new ArrayList<String>(totalTransactions)); }

		for ( int i = 0; i < totalTransactions; i++ ) {
			
			database.get(0).add("-1");
			database.get(1).add("-1");
			database.get(2).add("-1");
			database.get(3).add("-1");
			database.get(4).add("");
			
		}
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
	private void fillInputDirectory(int posListIndex) {
		
		File[] tempDirectory = new File(posListDirectories.get(posListIndex)).listFiles();
		
		for ( int i = 0; i < tempDirectory.length; i++ ) {
			
			try {
				
				if ( tempDirectory[i].getName().contains("Positive32") && retrieveLogDate(tempDirectory[i]) == getYesterdaysDate()) {
					
					directory.add(tempDirectory[i]);
					
				}
				
			} catch (FileNotFoundException e) { e.printStackTrace(); }
		}
	}
	
	/**
	 * Returns yesterdays date in format "MM-dd-yyyy".
	 * @return String
	 */
	private String getYesterdaysDate() {
		
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * Writes transaction information withheld in the database ArrayList to output file.
	 * @param outputFile
	 * @throws FileNotFoundException
	 */
	private void writeTransactionInfo(File outputFile) throws FileNotFoundException {
		
		try {
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, false));
			writer.close();
			
			writer = new PrintWriter(new FileOutputStream(outputFile, true));
			
			for ( int i = 0; i < database.get(0).size(); i++ ) {
				
					writer.println(firstTransactionNumber + i + "," + database.get(0).get(i) + "," + database.get(1).get(i) + "," + database.get(2).get(i) + ","
					+ database.get(3).get(i) + "," + database.get(4).get(i) + "," + storeInfo + "," + date);
				
			}
			
			writer.close();
			
		} catch (IOException e) {

			e.printStackTrace();
			
		}
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
	 * Returns last transaction number in log; used to determine when file reader should stop.
	 * @param inputFile
	 * @param firstTransactionNumber
	 * @return -1 if not found
	 * @throws FileNotFoundException
	 */
	private int retrieveFinalTransactionNumber(File inputFile, int firstTransactionNumber) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String temp = null;
		int finalTransactionNumber = -1;
		
		while ( input.hasNextLine() ) {
			
			temp = input.nextLine();
			
			if ( temp.contains("#" + Integer.toString(firstTransactionNumber).substring(0, 1)) && temp.contains("StartTransaction")) {
				
				finalTransactionNumber = Integer.parseInt(temp.substring(87, 94));
				
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
	private String retrieveLogDate(File inputFile) throws FileNotFoundException {
		
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
		return "";
	}
}