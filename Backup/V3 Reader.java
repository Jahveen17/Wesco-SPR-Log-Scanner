import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;

//Run-time #1: 6.9733 minutes
//Run-time #2 (2 files): 14.2221 minutes

public class Reader extends Interface implements Runnable {
	
	private ProgressBar progressBar;
	private ProgressIndicator progressIndicator;
	private ArrayList<ArrayList<String>> database;
	private String storeInfo;
	private String date;
	private String inputDirectory;
	private String outputDirectory;
	private String archiveDirectory;
	private int finalTransactionNumber;
	private int firstTransactionNumber;
	private int totalTransactions = 0;
	private int completedTransactions = 0;
	private int totalFiles = 0;
	private int completedFiles = 0;
	
	public Reader(ProgressIndicator progind, ProgressBar progbar, String inputDirectory, String outputDirectory, String archiveDirectory) {
	
		this.inputDirectory = inputDirectory;
		this.outputDirectory = outputDirectory;
		this.archiveDirectory = archiveDirectory;
		this.progressBar = progbar;
		this.progressIndicator = progind;
		
	}
	
	public void run() {
		
		try {
			
			int copyCounter = 2;
			File[] directory = new File(inputDirectory).listFiles();
			totalFiles = directory.length;
			
			for ( int i = 0; i < totalFiles; i++ ) {
				
				completedTransactions = 0;
				initializeArrayList(directory[i]);
				storeInfo = retrieveStoreInfo(directory[i]);
				date = retrieveLogDate(directory[i]);
				readFile(directory[i]);
				File output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + 1 + ".txt");
				
				if (output.exists() ) {
					
					output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");
					
					while ( output.exists() ) { 
						
						copyCounter++;
						output = new File(outputDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt");
						
					}
					
					directory[i].renameTo(new File(archiveDirectory + "\\" + date + " " + storeInfo + "-" + copyCounter + ".txt"));
					directory[i].delete();
					
				} else {
					
					if ( directory[i].renameTo(new File(archiveDirectory + "\\" + date + " " + storeInfo + "-" + 1 + ".txt")) ) { directory[i].delete(); }
					copyCounter = 2;
					
				}
				
				writeTransactionInfo(output);
				updateFileProgress();	
			}
			
		} catch (FileNotFoundException e) { e.printStackTrace(); } 
	}
	
	/**
	 * Updates progress bar from extended interface class by calculating completed transactions per file.
	 */
	private void updateTransactionProgress() { 
		
		if ( totalTransactions == 0 ) { progressBar.setProgress(0); }
		completedTransactions++;
		
		Platform.runLater(() -> {
			
			progressBar.setProgress( (completedTransactions + 0.0) / totalTransactions );
			
		});
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
	
	// Potentially remove this line to save time ??
	// && !database.get(3).get(transactionNumber - firstTransactionNumber).equals("-1"))
	
	/**
	 * Scans log file to retrieve and save transaction information.
	 * Time Complexity: O(n)
	 * @param inputFile
	 * @throws FileNotFoundException
	 * @throws InterruptedException 
	 */
	private void readFile(File inputFile) throws FileNotFoundException {
			
			int transactionNumber = firstTransactionNumber;
			Scanner input = new Scanner(inputFile);

			while ( transactionNumber < finalTransactionNumber ) {
				
				String tempTime = input.next();
				String tempLine = input.nextLine();
				
				if ( !database.get(0).get(transactionNumber - firstTransactionNumber).equals("-1") && !database.get(1).get(transactionNumber - firstTransactionNumber).equals("-1") && !database.get(2).get(transactionNumber - firstTransactionNumber).equals("-1") && !database.get(3).get(transactionNumber - firstTransactionNumber).equals("-1")) {
					
					updateTransactionProgress();
					transactionNumber++;
					
				} else if ( tempLine.contains("StartTransaction") && tempLine.contains("#" + transactionNumber) ) {
					
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
					
					if ( database.get(1).get(transactionNumber - firstTransactionNumber).equals("-1") ) { database.get(1).set(transactionNumber - firstTransactionNumber, ""); } 
					
					if ( database.get(3).get(transactionNumber - firstTransactionNumber).equals("-1") ) { database.get(3).set(transactionNumber - firstTransactionNumber, ""); }
					
					input.close();
					input = new Scanner(inputFile);
				}
			}
			
			input.close();
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
		
		System.out.println(finalTransactionNumber);
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
