package stg;

/**
 * Created by rickjackson on 3/1/17.
 */
public class Interest {
    private long principal;
    private double annualRate;
    private long balance;
    private int interval;
    private int years;
    
    public Interest() {
        
    }
    
    public Interest(double annualRate, int interval, int years) {
        this.annualRate = annualRate;
        this.interval = interval;
        this.years = years;
    }
    
    public double getAnnualRate() {
        return annualRate;
    }
    
    void setAnnualRate(double annualRate) {
        this.annualRate = annualRate;
    }
    
    public int getInterval() {
        return interval;
    }
    
    void setInterval(int interval) {
        this.interval = interval;
    }
    
    public int getYears() {
        return years;
    }
    
    void setYears(int years) {
        this.years = years;
    }
}
