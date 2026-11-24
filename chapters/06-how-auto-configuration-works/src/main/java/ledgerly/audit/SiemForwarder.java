package ledgerly.audit;

import ledgerly.siem.SiemClient;

/** Forwards audit events to the security team's SIEM. Needs the SIEM client library. */
public class SiemForwarder {

  private final SiemClient client = new SiemClient();

  public String forward(String event) {
    return client.send(event);
  }
}
