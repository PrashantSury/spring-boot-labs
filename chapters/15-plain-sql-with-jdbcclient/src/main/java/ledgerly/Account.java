package ledgerly;

/** One row of the account table. Column names match the component names, so JdbcClient maps it. */
public record Account(long id, String number, String owner, long balance) {}
