package ledgerly;

/** Sends a text message. Real in prod, fake everywhere else. */
public interface SmsGateway {
  String send(String phone, String text);
}
