package ledgerly.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** HTTP only: read the request, call the service, choose the status. */
// Task 3: a thin controller
//
//   - take a CustomerService through the constructor (NOT the repository)
//   - POST /customers        -> service.open(...): 201 + Location /customers/{id} + the DTO
//                                IllegalArgumentException -> 400, DuplicateCustomerException -> 409
//   - GET /customers/{id}    -> 200 + DTO, or 404
//   - GET /customers/{id}/summary (task 4) -> 200 + StatementSummary, or 404
// No business rules here: no regex, no sums.
@RestController
public class CustomerController {


  @PostMapping("/customers")
  public ResponseEntity<CustomerResponse> open(@RequestBody OpenCustomerRequest request) {
    return ResponseEntity.status(501).build(); // TODO task 3
  }

  @GetMapping("/customers/{id}")
  public ResponseEntity<CustomerResponse> find(@PathVariable long id) {
    return ResponseEntity.status(501).build(); // TODO task 3
  }

  @GetMapping("/customers/{id}/summary")
  public ResponseEntity<StatementSummary> summary(@PathVariable long id) {
    return ResponseEntity.status(501).build(); // TODO task 4
  }
}
