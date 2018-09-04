import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Reader {
	
	//Entire process took 7.54605 minutes.
	
	private ArrayList<ArrayList<String>> database;
	private int finalTransactionNumber;
	private int firstTransactionNumber;
	private int totalTransactions;
	
	public static void main(String[] args) {
		
		Reader testobj = new Reader();
		File input = new File("Positive32-20180609.log");
		File output = null;
		
		try {
			
			output = new File(testobj.retrieveStoreInfo(input) + ".txt");
		
		} catch (FileNotFoundException e1) {
			
			e1.printStackTrace();
			
		}
		
		try {
			
			testobj.readFile(input, output);
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
			
		}
	}

	/**
	 * Scans log file to retrieve and print transaction and store information.
	 * Currently uses an inefficient method of searching for transaction IDs; Scans through entire document for each transaction.
	 * @param inputFile
	 * @throws FileNotFoundException
	 */
	public void readFile(File inputFile, File outputFile) throws FileNotFoundException {
			
			long start = System.currentTimeMillis();
		
			initializeArrayList(inputFile);
			searchStartTimes(inputFile);
			searchEndTimes(inputFile);
			searchTransTypes(inputFile); 
			writeTransactionInfo(outputFile, retrieveStoreInfo(inputFile));
			
			long end = System.currentTimeMillis();
			
			System.out.println(end - start);
		}
	
	private void searchStartTimes(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		int transactionNumber = retrieveFirstTransactionNumber(inputFile);
		ArrayList<String> startTimes = new ArrayList<>(totalTransactions);
		
		while ( input.hasNextLine() ) {
			
			String tempTime = input.next();
			String tempLine = input.nextLine();

			if ( tempLine.contains("StartTransaction") && tempLine.contains("#" + transactionNumber) ) {
				
				startTimes.add(tempTime);
				transactionNumber++;
				
			}
		}
		
		input.close();
		System.out.println("finished");
		database.add(startTimes);
	}
	
	private void searchEndTimes(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		int transactionNumber = retrieveFirstTransactionNumber(inputFile);
		ArrayList<String> endTimes = new ArrayList<>(totalTransactions);
		
		for ( int i = 0; i < totalTransactions; i++ ) { endTimes.add("-1");}
		
		while ( transactionNumber < finalTransactionNumber ) {

			String tempTime = input.next();
			String tempLine = input.nextLine();

			if ( tempLine.contains("EndTransaction") && tempLine.contains("#" + transactionNumber) ) {
				
				endTimes.set(transactionNumber - firstTransactionNumber, tempTime);
				transactionNumber++;
				
			} else if ( !input.hasNextLine() && endTimes.get(transactionNumber - firstTransactionNumber).equals("-1")) {
				
				endTimes.set(transactionNumber - firstTransactionNumber, "N/A");
				input.close();
				input = new Scanner(inputFile);
				transactionNumber++;
				
			}
		}
		
		System.out.println("finished");
		database.add(endTimes);
		input.close();
	}
	
	private void searchTransTypes(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		int transactionNumber = retrieveFirstTransactionNumber(inputFile);
		ArrayList<String> FTransType1 = new ArrayList<>(totalTransactions);
		ArrayList<String> FTransType2 = new ArrayList<>(totalTransactions);
		
		for ( int i = 0; i < totalTransactions; i++ ) {
			FTransType1.add("-1");
			FTransType2.add("-1"); 
		}
		
		while ( transactionNumber < finalTransactionNumber ) {
			
			String tempLine = input.nextLine();

			if ( tempLine.contains("FTransType=") && tempLine.contains("" + transactionNumber) ) {
				
				if ( FTransType1.get(transactionNumber - firstTransactionNumber).equals("-1") ) {
					
					FTransType1.set(transactionNumber - firstTransactionNumber, (parseTransactionType(tempLine)));
					
				} else {
					
					FTransType2.set(transactionNumber - firstTransactionNumber, (parseTransactionType(tempLine)));
					transactionNumber++;
					
				}
				
			} else if ( !input.hasNextLine() && FTransType2.get(transactionNumber - firstTransactionNumber).equals("-1") ) {
				
				FTransType2.set(transactionNumber - firstTransactionNumber, (parseTransactionType(tempLine)));
				input.close();
				input = new Scanner(inputFile);
				transactionNumber++;
				
			}
		}
		
		System.out.println("finished");
		input.close();
		database.add(FTransType1);
		database.add(FTransType2);
	}
	
	private void initializeArrayList( File inputFile ) throws FileNotFoundException {
		
		firstTransactionNumber = retrieveFirstTransactionNumber(inputFile);
		finalTransactionNumber = retrieveFinalTransactionNumber(inputFile, firstTransactionNumber);
		totalTransactions = (finalTransactionNumber - firstTransactionNumber);
		database = new ArrayList<ArrayList<String>>(totalTransactions);
		
	}
	
	/**
	 * Parses string containing transaction type.
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
			
		} else {
			
			return "";
			
		}
	}
	
	/**
	 * Prints transaction information to output file.
	 * @param outputFile
	 * @param transNum
	 * @param start
	 * @param end
	 * @param transType1
	 * @param transType2
	 * @param name
	 * @throws FileNotFoundException
	 */
	private void writeTransactionInfo(File outputFile, String computerName) throws FileNotFoundException {
		
		try {
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, false));
			writer.close();
			
			writer = new PrintWriter(new FileOutputStream(outputFile, true));
			
			for ( int i = 0; i < totalTransactions; i++ ) {
				
					writer.println(database.get(0).get(i) + ", " + database.get(1).get(i) + ", " + database.get(2).get(i) + ", " + database.get(3).get(i));
				
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
	 * @throws FileNotFoundException
	 */
	private int retrieveFirstTransactionNumber(File inputFile) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		
		while ( input.hasNext() ) {
			
			String temp = input.next();
			
			if ( temp.startsWith("#")) {
				
				input.close();
				temp = temp.substring(1, temp.length());
				return Integer.parseInt(temp);
				
			}
		}
		
		input.close();
		return -1;
	}
	
	/**
	 * Returns last transaction number in log; used to determine when file reader should stop.
	 * @param inputFile
	 * @param firstTransactionNumber
	 * @return
	 * @throws FileNotFoundException
	 */
	private int retrieveFinalTransactionNumber(File inputFile, int firstTransactionNumber) throws FileNotFoundException {
		
		Scanner input = new Scanner(inputFile);
		String temp = null;
		String transactionNumber = null;
		
		while ( input.hasNextLine() ) {
			
			temp = input.nextLine();
			
			//Potentially faulty method; relies on first 3 numbers of transaction IDs being consistent. First digit will always be consistent however.
			if ( temp.contains("#" + Integer.toString(firstTransactionNumber).substring(0, 3)) && temp.contains("StartTransaction")) {
				
				transactionNumber = temp.substring(87, 94);
				
			}
		}
		
		input.close();
		return Integer.parseInt(transactionNumber);
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
}
