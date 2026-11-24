package ledgerly.service;

/** Thrown by the service when a PAN is already registered. Finished. */
public class DuplicateCustomerException extends RuntimeException {
  public DuplicateCustomerException(String pan) {
    super("a customer with PAN " + pan + " already exists");
  }
}
