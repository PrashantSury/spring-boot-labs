package ledgerly.web;

/** GET /customers/{id}/summary answers this. Finished. */
public record StatementSummary(long id, int transactions, long totalIn, long totalOut) {}
