package ledgerly;

/** The partner said the request itself is wrong (HTTP 400). Never retried. Finished. */
public class PayeeRejected extends RuntimeException {

  public PayeeRejected(String payee) {
    super("partner rejected payee '" + payee + "'");
  }
}
