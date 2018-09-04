
public class Transaction {

	private int transactionNumber;
	
	private String startTime = "-1";
	private String endTime = "-1";
	private String transactionType1 = "-1";
	private String transactionType2 = "-1";
	private String isPrepaid = "";
	
	public Transaction( int transactionNumber ) { this.transactionNumber = transactionNumber; }
	
	public int getTransactionNumber() { return transactionNumber; }
	
	public String getStartTime() { return startTime; }
	
	public String getEndTime() { return endTime; }
	
	public String getTransactionType1() { return transactionType1; }
	
	public String getTransactionType2() { return transactionType2; }
	
	public String getIsPrepaid() { return isPrepaid; }
	
	public void setTransactionNumber(int transactionNumber) { this.transactionNumber = transactionNumber; }
	
	public void setStartTime(String startTime) { this.startTime = startTime; }
	
	public void setEndTime(String endTime) { this.endTime = endTime; }
	
	public void setTransactionType1(String transactionType) { this.transactionType1 = transactionType; }
	
	public void setTransactionType2(String transactionType) { this.transactionType2 = transactionType; }
	
	public void setIsPrepaid(String isPrepaid) { this.isPrepaid = isPrepaid; }
	
	
	
	
}
