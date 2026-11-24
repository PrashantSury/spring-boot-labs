package ledgerly;


/** The body of POST /transfers. */
// Task 1: put the rules on the record
//   from         not blank, and (task 4) @AccountNumber
//   amount       positive, at most 200000
//   remark       optional, at most 50 characters
//   notifyEmail  optional, but if present a valid e-mail address
//
// Task 3 (part 2): beneficiary must be present, AND its own rules must run.
//   Constraints inside Beneficiary are only checked if this field is @Valid.
public record TransferRequest(
    String from,
    long amount,
    String remark,
    String notifyEmail,
    Beneficiary beneficiary) {}
