import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class Reader {
	
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
		
		clearOutputFile(outputFile);
		String computerName = retrieveStoreInfo(inputFile);
		int transactionNumber = retrieveFirstTransactionNumber(inputFile);
		int finalTransactionNumber = retrieveFinalTransactionNumber(inputFile, transactionNumber); 
		
		while ( transactionNumber <= finalTransactionNumber ) {
			
			Scanner input = new Scanner(inputFile);
			String startTime = null;
			String endTime = null;
			String FTransType1 = null;
			String FTransType2 = null;
				
			while ( (startTime == null || endTime == null || FTransType1 == null || FTransType2 == null ) ) {
				
				String tempTime = input.next();
				String tempLine = input.nextLine();
				
				if ( (tempLine.contains("StartTransaction") && tempLine.contains("#" + transactionNumber))) {
					
					startTime = tempTime;

				} else if ( (tempLine.contains("EndTransaction") && tempLine.contains("#" + transactionNumber))) {

					endTime = tempTime;
				
				} else if ( tempLine.contains("FTransType=") && tempLine.contains("" + transactionNumber) ) {
					
					if ( FTransType1 == null ) { 
						
						FTransType1 = parseTransactionType(tempLine);
					
					} else {
						
						FTransType2 = parseTransactionType(tempLine);
						
					}
				
				} else if ( endTime == null && FTransType2 == null && !input.hasNextLine() ) {
					
					endTime = "N/A";
					FTransType2 = "N/A";
				
				} else if ( FTransType2 == null && !input.hasNextLine() ) {
					
					FTransType2 = "N/A";
					
				} else if ( transactionNumber > finalTransactionNumber ) {
					
					input.close();
					return; 
					
				}
			}
			
			input.close();
			writeTransactionInfo(outputFile, Integer.toString(transactionNumber), startTime, endTime, FTransType1, FTransType2, computerName);
			transactionNumber++;
			
			}
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
	private void writeTransactionInfo(File outputFile, String transNum, String start, String end, String transType1, String transType2, String name) throws FileNotFoundException {
		
		try {
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, true));
			writer.write(transNum + ", " + start + ", " + end + ", " + transType1 + ", " + transType2 + ", " + name);
			writer.println();
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
	
	/**
	 * Clears output File
	 * @param outputFile
	 */
	private void clearOutputFile(File outputFile) {
		
		try {
			
			PrintWriter writer = new PrintWriter(new FileOutputStream(outputFile, false));
			writer.close();
			
		} catch (FileNotFoundException e) {

			e.printStackTrace();
			
		}
	}
}
