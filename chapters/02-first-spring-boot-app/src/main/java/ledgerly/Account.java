package ledgerly;

/** One bank account. Spring writes a record as a JSON object: number, holder, balance. */
public record Account(String number, String holder, int balance) {}
