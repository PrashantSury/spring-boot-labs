package ledgerly.audit;

/** Where audit events go. The starter gives a default; an app may bring its own. */
public interface AuditSink {
  String write(String event);
}
