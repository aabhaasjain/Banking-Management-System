package com.bankingmanagement.bankingmanagement.loan.serviceImplemenation;

import com.bankingmanagement.bankingmanagement.database.DatabaseConnectionDao;
import com.bankingmanagement.bankingmanagement.database.DatabaseConnectionException;
import com.bankingmanagement.bankingmanagement.loan.database.LoanApplyDao;
import com.bankingmanagement.bankingmanagement.loan.exception.LoanException;
import com.bankingmanagement.bankingmanagement.loan.model.EligibilityInfo;
import com.bankingmanagement.bankingmanagement.loan.model.Loan;
import com.bankingmanagement.bankingmanagement.loan.model.LoanInfo;
import com.bankingmanagement.bankingmanagement.loan.service.LoanService;
import com.bankingmanagement.bankingmanagement.signup.exception.NewPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class LoanServiceImpl implements LoanService {

    // Database connection instance.
    @Autowired
    private DatabaseConnectionDao databaseConnectionDAO;

    @Autowired
    private LoanApplyDao loanapplydao;

    @Override
    public String applyLoan(Loan loan,String userId) throws LoanException {
          validateLoanData(loan);
          validateUserId(userId);
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            String insertLoanQuery = loanapplydao.insertLoanReqTableQuery(loan,userId);
            System.out.println("Inserted Successfully"+ insertLoanQuery);
            final int loanRowInserted = statement.executeUpdate(loanapplydao.insertLoanReqTableQuery(loan, userId), Statement.RETURN_GENERATED_KEYS);
            if (loanRowInserted > 0) {
               return "viewallApplication";
            }
            throw new LoanException("Internal Error while loan,");

        } catch (SQLException | DatabaseConnectionException sqlException) {
            // TODO handle database incosientancy while insering data
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }

    }

    @Override
    public List<LoanInfo> getAppliedLoans(String userId) throws LoanException {

         final List<LoanInfo> loans = new ArrayList<>();
        if(userId==null || userId.trim().isEmpty()) {
            throw new LoanException("Please Login Again to fetch details");
        }
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            String fetchAllLoansQuery = loanapplydao.fetchLoanReqTableQuery(userId);
            final ResultSet allLoan = statement.executeQuery(fetchAllLoansQuery);
            if (allLoan == null) {
                return null;
            }
            while (allLoan.next()) {
                LoanInfo l=new LoanInfo(allLoan.getInt("idLoanRequest"),allLoan.getString("CustomerID"),
                        allLoan.getString("CustomerFirstName"),
                        allLoan.getString("CustomerLastName"),allLoan.getString("CustomerAddress1"),
                        allLoan.getString("CustomerAddress2"),allLoan.getString("City"),
                        allLoan.getString("Zipcode"),allLoan.getString("CustomerEmail"),
                        allLoan.getString("PhoneNumber"),allLoan.getString("SIN"),
                        allLoan.getString("SALARY"),allLoan.getString("AGE"),
                        allLoan.getString("LoanAmount"),
                        allLoan.getString("LOANTYPE"),allLoan.getBoolean("LoanStatus"));
                loans.add(l);
            }
            return loans;
        } catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }

    }

    @Override
    public boolean deleteLoanRequest(int loanId) {
        return false;
    }

    @Override
    public double checkEligibilityAndInterest(EligibilityInfo info, String userId) throws LoanException {
        validateUserId(userId);
        if(validateLoanEligibilityData(info))
        {
            return calculateInterestRates(info);
        }
        return 0;
    }

    public double calculateInterestRates(EligibilityInfo info) {
        int age =Integer.parseInt(info.getAge());
        double amount =Double.parseDouble(info.getLoanAmount());
        String loanType =info.getLoanType();
        double salary =Double.parseDouble(info.getSalary());
        double interest=getLoanInterestByType(loanType);

        if(age>55)
        {
            if (interest>13)
            {
                interest=interest/3;
            }
        }
        else if( age>18 )
        {
            if(interest>13)
            {
                if(amount>100000)
                {
                 if(salary>25000)
                 {
                     interest=interest-3;
                 }
                }
            }
        }

        return interest;
    }

    public double getLoanInterestByType(String loanType) {
        double interestPercentage=0;
        if(loanType.equalsIgnoreCase("PersonalLoan"))
        {
            interestPercentage=15;
        }
        else if(loanType.equalsIgnoreCase("HomeLoan"))
        {
            interestPercentage=13;
        }
        else if(loanType.equalsIgnoreCase("EducationLoan"))
        {
            interestPercentage=14;
        }
        else if(loanType.equalsIgnoreCase("GoldLoan"))
        {
            interestPercentage=5.5;
        }
        else if(loanType.equalsIgnoreCase("VehicleLoan"))
        {
            interestPercentage=10.5;
        }
        else if(loanType.equalsIgnoreCase("AgriculturalLoan"))
        {
            interestPercentage=9.8;
        }
        else if(loanType.equalsIgnoreCase("LoanAgainstInsurancePolicies"))
        {
            interestPercentage=4.5;
        }
        else if(loanType.equalsIgnoreCase("LoanAgainstBankFDs"))
        {
            interestPercentage=4.5;
        }
        else if(loanType.equalsIgnoreCase("LoanAgainstMutualFundsOrShares"))
        {
            interestPercentage=6.0;
        }

        return interestPercentage;
    }

    @Override
    public List<LoanInfo> getAllPendingLoans() throws LoanException {
         List<LoanInfo> pendingLoans = new ArrayList<>();
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            String fetchAllPendingLoanQuery = loanapplydao.fetchAllPendingLoanQuery();
            final ResultSet allLoan = statement.executeQuery(fetchAllPendingLoanQuery);
            if (allLoan == null) {
                return null;
            }
            while (allLoan.next()) {
                LoanInfo l=new LoanInfo(allLoan.getInt("idLoanRequest"),allLoan.getString("CustomerID"),
                        allLoan.getString("CustomerFirstName"),
                        allLoan.getString("CustomerLastName"),allLoan.getString("CustomerAddress1"),
                        allLoan.getString("CustomerAddress2"),allLoan.getString("City"),
                        allLoan.getString("Zipcode"),allLoan.getString("CustomerEmail"),
                        allLoan.getString("PhoneNumber"),allLoan.getString("SIN"),
                        allLoan.getString("SALARY"),allLoan.getString("AGE"),
                        allLoan.getString("LoanAmount"),
                        allLoan.getString("LOANTYPE"),allLoan.getBoolean("LoanStatus"));
                pendingLoans.add(l);
            }
            return pendingLoans;
        } catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }
    }
    @Override
    public List<LoanInfo> getAllApprovedLoans() throws LoanException {
        List<LoanInfo> approvedLoans = new ArrayList<>();
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            String fetchAllApprovedLoanQuery = loanapplydao.fetchAllApprovedLoanQuery();
            final ResultSet allLoan = statement.executeQuery(fetchAllApprovedLoanQuery);
            if (allLoan == null) {
                return null;
            }
            while (allLoan.next()) {
                LoanInfo l=new LoanInfo(allLoan.getInt("idLoanRequest"),allLoan.getString("CustomerID"),
                        allLoan.getString("CustomerFirstName"),
                        allLoan.getString("CustomerLastName"),allLoan.getString("CustomerAddress1"),
                        allLoan.getString("CustomerAddress2"),allLoan.getString("City"),
                        allLoan.getString("Zipcode"),allLoan.getString("CustomerEmail"),
                        allLoan.getString("PhoneNumber"),allLoan.getString("SIN"),
                        allLoan.getString("SALARY"),allLoan.getString("AGE"),
                        allLoan.getString("LoanAmount"),
                        allLoan.getString("LOANTYPE"),allLoan.getBoolean("LoanStatus"));
                approvedLoans.add(l);
            }
            return approvedLoans;
        } catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }
    }
    @Override
    public List<LoanInfo> getAllRejectedLoans() throws LoanException {
        List<LoanInfo> rejectedLoans = new ArrayList<>();
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            String fetchAllRejectedLoanQuery = loanapplydao.fetchAllRejectedLoanQuery();
            final ResultSet allLoan = statement.executeQuery(fetchAllRejectedLoanQuery);
            if (allLoan == null) {
                return null;
            }
            while (allLoan.next()) {
                LoanInfo l=new LoanInfo(allLoan.getInt("idLoanRequest"),allLoan.getString("CustomerID"),
                        allLoan.getString("CustomerFirstName"),
                        allLoan.getString("CustomerLastName"),allLoan.getString("CustomerAddress1"),
                        allLoan.getString("CustomerAddress2"),allLoan.getString("City"),
                        allLoan.getString("Zipcode"),allLoan.getString("CustomerEmail"),
                        allLoan.getString("PhoneNumber"),allLoan.getString("SIN"),
                        allLoan.getString("SALARY"),allLoan.getString("AGE"),
                        allLoan.getString("LoanAmount"),
                        allLoan.getString("LOANTYPE"),allLoan.getBoolean("LoanStatus"));
                rejectedLoans.add(l);
            }
            return rejectedLoans;
        } catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }
    }
    @Override
    public boolean approveLoan(int id) throws LoanException {
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            final int totalRowsUpdated = statement.executeUpdate(loanapplydao.approveLoanQuery(id));
            if (totalRowsUpdated>0){
                return true;
            }
            throw new LoanException("Internal Error while loan,");
        }
        catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }
    }
    @Override
    public boolean rejectLoan(int id) throws LoanException {
        try (final Connection connection = databaseConnectionDAO.getConnection();
             final Statement statement = connection.createStatement()) {
            final int totalRowsUpdated = statement.executeUpdate(loanapplydao.rejectLoanQuery(id));
            if (totalRowsUpdated>0){
                return true;
            }
            throw new LoanException("Internal Error while loan,");
        }
        catch (SQLException | DatabaseConnectionException sqlException) {
            sqlException.printStackTrace();
            throw new LoanException("Internal Error while loan,");
        }
    }
    private boolean validateLoanEligibilityData(EligibilityInfo info) throws LoanException  {
        String sin = info.getSin();
        String salary =info.getSalary();
        String age =info.getAge();
        String amount =info.getLoanAmount();
        String loanType =info.getLoanType();

        if(sin==null || sin.trim().isEmpty()){
            throw new LoanException("sin is empty  ");
        }

        else if(salary==null || salary.trim().isEmpty()|| salary.equalsIgnoreCase("0")){
            throw new LoanException("Salary is empty  ");
        }
        else if(age==null || age.trim().isEmpty()||age.equalsIgnoreCase("0")){
            throw new LoanException("Age is empty ");
        }
        else if (Integer.parseInt(age)<18 || Integer.parseInt(age)>60)
        {
            throw new LoanException("Our bank doesn't offer loan to your age");
        }
        else if(amount==null || amount.trim().isEmpty()|| amount.equalsIgnoreCase("0")){
            throw new LoanException("Amount is empty  ");
        }
        else if(Long.parseLong(amount)>1000000000 || Long.parseLong(amount)<1000 )
        {
            throw new LoanException("Sorry we dont offer loan for this amount");
        }
        else if(loanType==null || loanType.trim().isEmpty()||loanType.equalsIgnoreCase("loanType")){
            throw new LoanException("Please select loanType  ");
        }
        return true;
    }

    private void validateUserId(String userId) throws LoanException {
        if(userId==null || userId.trim().isEmpty() || !Pattern.matches("[A-Za-z ]+", userId)){
            throw new LoanException("userId is Invalid Please Login");
        }
    }

    private void validateLoanData(Loan loan) throws LoanException {
        String firstName = loan.getFirstName();

        if(firstName==null || firstName.trim().isEmpty() || !Pattern.matches("[A-Za-z ]+", firstName)){
            throw new LoanException("FirstName is empty or Invalid ");
        }
        String lastName = loan.getLastName();

        if(lastName==null || lastName.trim().isEmpty() || !Pattern.matches("[A-Za-z ]+", lastName)){
            throw new LoanException("LastName is empty or Invalid ");
        }

        String address1 = loan.getAddress1();

        if(address1==null || address1.trim().isEmpty()){
            throw new LoanException("Address1 is empty ");
        }

        String address2 = loan.getAddress2();

        if(address2==null || address2.trim().isEmpty()){
            throw new LoanException("Address2 is empty ");
        }

        String email = loan.getEmail();

        if(email==null || email.trim().isEmpty() || !Pattern.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$",
                email)){
            throw new LoanException("Email is empty or Invalid ");
        }

        String city = loan.getCity();

        if(city==null || city.trim().isEmpty() || !Pattern.matches("[A-Za-z ]+", city)){
            throw new LoanException("City is empty or Invalid ");
        }

        String zipCode = loan.getZipCode();

        if(zipCode==null || zipCode.trim().isEmpty() || !Pattern.matches("^\\d{5}(?:[-\\s]\\d{4})?$", zipCode)){
            throw new LoanException("Zipcode is empty or Invalid ");
        }

        String contactNumber = loan.getContactNumber();

        if(contactNumber==null || contactNumber.trim().isEmpty()){
            throw new LoanException("ContactNumber is empty  ");
        }

        String sin = loan.getSin();
        if(sin==null || sin.trim().isEmpty()){
            throw new LoanException("sin is empty  ");
        }
        String age =loan.getAge();
        if(age==null || age.trim().isEmpty()||age.equalsIgnoreCase("0")){
            throw new LoanException("Age is empty ");
        }
        String salary =loan.getSalary();
        if(salary==null || sin.trim().isEmpty()|| salary.equalsIgnoreCase("0")){
            throw new LoanException("Salary is empty  ");
        }
        String amount =loan.getLoanAmount();
        if(amount==null || amount.trim().isEmpty()|| amount.equalsIgnoreCase("0")){
            throw new LoanException("Amount is empty  ");
        }
        String loanType =loan.getLoanType();
        if(loanType==null || loanType.trim().isEmpty()||loanType.equalsIgnoreCase("loanType")){
            throw new LoanException("Please select loanType  ");
        }
    }
}