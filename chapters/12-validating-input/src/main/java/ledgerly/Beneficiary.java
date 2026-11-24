package ledgerly;


/** Who receives the money (part of a transfer). */
// Task 3 (part 1): constrain the beneficiary
//   name: not blank
//   ifsc: must match [A-Z]{4}0[A-Z0-9]{6}   (for example HDFC0001234)
public record Beneficiary(
    String name,
    String ifsc) {}
