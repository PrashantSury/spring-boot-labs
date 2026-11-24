package ledgerly;

/** The result of one transfer. */
public record Transfer(String from, String to, long amount, long fee) {}
