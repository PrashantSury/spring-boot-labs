package ledgerly;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** GET /customers (staff list) and GET /customers/{id}. Finished: the work is in CustomerQueries. */
@RestController
public class CustomerController {

  private final CustomerQueries queries;

  public CustomerController(CustomerQueries queries) {
    this.queries = queries;
  }

  @GetMapping("/customers")
  public List<Views.CustomerSummary> list() {
    return queries.list();
  }

  @GetMapping("/customers/{id}")
  public Views.CustomerDetail detail(@PathVariable long id) {
    return queries.detail(id);
  }
}
