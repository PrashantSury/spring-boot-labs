package ledgerly.service;

import ledgerly.repo.Customer;
import ledgerly.web.CustomerResponse;

/** Turns a stored Customer into the DTO a client may see. */
// Task 1 (part 2): map by hand
//
// toResponse(c) -> new CustomerResponse(c.id(), c.name(), mask(c.pan()))
// mask("ABCPR1234F") -> "XXXXXX234F": every character but the last 4 becomes X.
// A pan of 4 characters or fewer is returned as it is.
public final class CustomerMapper {

  private CustomerMapper() {}

  public static CustomerResponse toResponse(Customer c) {
    return null; // TODO task 1
  }

  public static String mask(String pan) {
    return null; // TODO task 1
  }
}
