package ledgerly.audit;

/** The starter's default sink. Finished; NOT one of your tasks. */
public class ConsoleAuditSink implements AuditSink {

  @Override
  public String write(String event) {
    return "console: " + event;
  }
}
