package ledgerly;

/** replayed = true when the Idempotency-Key was seen before and the ORIGINAL transfer is returned. Finished. */
public record TransferResult(TransferDto transfer, boolean replayed) {}
