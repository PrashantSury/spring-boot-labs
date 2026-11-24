package ledgerly;

import java.time.LocalDate;

/** GET /accounts/{number}/balance answers this. Jackson writes the date as "2026-11-24". */
public record Balance(String number, long balance, LocalDate asOf) {}
