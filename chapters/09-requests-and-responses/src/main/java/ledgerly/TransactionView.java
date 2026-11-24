package ledgerly;

import java.time.LocalDate;

/** One transaction as the mobile app sees it. */
// Task 4: shape the JSON with Jackson annotations
//
// The mobile app expects exactly these fields:
//     {"txn_id":"T1","date":"2026-11-01","amount":50000}
//   - id must be written as "txn_id"
//   - internalRef (the core-banking reference) must NOT appear at all
//   - date stays an ISO date string (Jackson already does that)
public record TransactionView(
    String id,
    LocalDate date,
    long amount,
    String internalRef) {}
