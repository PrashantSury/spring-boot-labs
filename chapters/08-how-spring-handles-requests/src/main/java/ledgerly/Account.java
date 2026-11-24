package ledgerly;

/** One bank account. */
public record Account(String number, String holder, long balance, boolean dormant) {}
