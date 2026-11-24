package ledgerly;

/** One payment in a batch. Finished. */
public record Payment(String from, String to, long amount) {}
