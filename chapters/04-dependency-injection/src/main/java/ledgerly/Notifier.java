package ledgerly;

/** Sends a message to a customer on one channel. */
public interface Notifier {
  String send(String message);
}
