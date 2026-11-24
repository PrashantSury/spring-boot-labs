package ledgerly;

/** The body of POST and PUT: every field of a payee except id and version. */
public record PayeeRequest(String name, String accountNumber, String ifsc, String nickname) {}
