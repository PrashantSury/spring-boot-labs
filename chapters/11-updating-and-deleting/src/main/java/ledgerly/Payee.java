package ledgerly;

/** A saved beneficiary. version goes up by 1 on every change. */
public record Payee(long id, String name, String accountNumber, String ifsc, String nickname, long version) {}
