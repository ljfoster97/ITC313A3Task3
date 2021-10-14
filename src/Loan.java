import java.util.Date;

public class Loan {
    private double interestRate;
    private int loanTerm;
    private double loanAmount;
    private java.util.Date date;

    public Loan(double interestRate, int loanTerm, double loanAmount) {
        this.interestRate = interestRate;
        this.loanTerm = loanTerm;
        this.loanAmount = loanAmount;
    }

    public Loan() {
        this(2.5, 1, 1000);
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public int getLoanTerm() {
        return loanTerm;
    }

    public void setLoanTerm(int loanTerm) {
        this.loanTerm = loanTerm;
    }

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getMonthlyPayment() {
        double monthlyInterestRate = interestRate / 1200;
        double monthlyPayment = loanAmount * monthlyInterestRate / (1 -
                (1 / Math.pow(1 + monthlyInterestRate, loanTerm * 12)));
        return monthlyPayment;
    }

    public double getTotalPayment() {
        double totalPayment = getMonthlyPayment() * loanTerm * 12;
        return totalPayment;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "interestRate=" + interestRate +
                ", term=" + loanTerm +
                ", amount=" + loanAmount +
                ", date=" + date +
                '}';
    }
}
