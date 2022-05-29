package com.bankingmanagement.bankingmanagement.loan.database;

import com.bankingmanagement.bankingmanagement.loan.model.Loan;

public interface LoanApplyDao {
      String insertLoanReqTableQuery(Loan loan, String userId);
      String fetchLoanReqTableQuery(String userId);
      String fetchAllPendingLoanQuery();
      String approveLoanQuery(int loanId);
      String rejectLoanQuery(int loanId);
      String fetchAllApprovedLoanQuery();
      String fetchAllRejectedLoanQuery();
}
