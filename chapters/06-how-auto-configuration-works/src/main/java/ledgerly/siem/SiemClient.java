package ledgerly.siem;

/**
 * Pretend this class comes from a vendor jar. Some Ledgerly apps have the
 * jar on the classpath, some don't. The checks hide it to test task 4.
 */
public class SiemClient {

  public String send(String event) {
    return "siem: " + event;
  }
}
