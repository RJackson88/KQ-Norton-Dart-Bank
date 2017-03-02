package stg;

/**
 * Created by rickjackson on 3/1/17.
 */
public class InterestCalculator {
    
    public InterestCalculator() {
        
    }
    
    public long calculateSimpleInterest(Account account, Object interval) {
        Double interest = account.getBalance() * (account.getInterest()
                                                         .getAnnualRate()
                                                  * account.getInterest()
                                                           .getInterval());
        return interest.longValue();
    }
    
    public long calculateComplexInterest(Account account, Object interval,
                                         int frequency) {
        // balance * Math.pow(1 + (rate / interval), interval * years);
        Double interest = account.getBalance()
                          * Math.pow((account.getInterest().getAnnualRate()
                                      / account.getInterest()
                                               .getInterval()),
                                     account.getInterest().getInterval()
                                     * account.getInterest().getYears());
        return interest.longValue();
    }
}
