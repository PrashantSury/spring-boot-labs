package ledgerly.service;

import java.util.List;
import java.util.Optional;
import ledgerly.repo.CustomerRepository;
import ledgerly.web.CustomerResponse;
import ledgerly.web.OpenCustomerRequest;
import ledgerly.web.StatementSummary;
import org.springframework.stereotype.Service;

/** The business rules. No HTTP here: no ResponseEntity, no status codes. */
@Service
public class CustomerService {

  private final CustomerRepository repository;

  public CustomerService(CustomerRepository repository) {
    this.repository = repository;
  }

  // Task 2: open a customer (the rules live HERE, not in the controller)
  //
  //   - name: trimmed; blank or null -> IllegalArgumentException("name is required")
  //   - pan: upper-cased, must match [A-Z]{5}[0-9]{4}[A-Z]
  //       otherwise IllegalArgumentException("invalid PAN")
  //   - pan already registered -> DuplicateCustomerException(pan)
  //   - pin: exactly 4 digits, otherwise IllegalArgumentException("pin must be 4 digits")
  //   - save with internalNotes "" and no transactions, return the DTO (CustomerMapper)
  public CustomerResponse open(OpenCustomerRequest request) {
    return null; // TODO task 2
  }

  public Optional<CustomerResponse> find(long id) {
    return Optional.empty(); // TODO task 3 (use the repository and CustomerMapper)
  }

  // Task 4: a summary computed in the service
  //
  // For the customer's transactions: how many, the sum of the positive ones
  // (totalIn) and the sum of the negative ones as a POSITIVE number (totalOut).
  // Asha (id 101): 50000, -1200, -800 -> StatementSummary(101, 3, 50000, 2000)
  // Unknown id -> Optional.empty()
  public Optional<StatementSummary> summary(long id) {
    return Optional.empty(); // TODO task 4
  }
}
