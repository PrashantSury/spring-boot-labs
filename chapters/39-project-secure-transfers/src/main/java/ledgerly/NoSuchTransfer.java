package ledgerly;

/** Finished. */
public class NoSuchTransfer extends RuntimeException {

  public NoSuchTransfer(long id) {
    super("no transfer %s".formatted(id));
  }
}
