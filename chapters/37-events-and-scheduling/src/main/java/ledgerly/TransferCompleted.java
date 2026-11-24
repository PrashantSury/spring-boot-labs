package ledgerly;

import java.math.BigDecimal;

/** Event: a transfer was booked. Finished. */
public record TransferCompleted(long id, String from, String to, BigDecimal amount) {}
