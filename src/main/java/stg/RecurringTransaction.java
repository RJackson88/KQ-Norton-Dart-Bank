package stg;

/**
 * Created by rickjackson on 3/1/17.
 */
public class RecurringTransaction extends Transaction {
    private int frequency;
    private long amount;
    private String type;
    
    public RecurringTransaction() {
        
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public long getAmount() {
        return amount;
    }
    
    public void setAmount(long amount) {
        this.amount = amount;
    }
}
