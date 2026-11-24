package ledgerly;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Accepts a transfer. */
// Task 3: only accept JSON
//
// POST /transfers with a JSON body {"from":"ACC-1","to":"ACC-2","amount":500}
// answers 200 and the text "accepted 500 from ACC-1 to ACC-2".
// Map the method, and add consumes = "application/json", so a text/plain
// body is refused with 415 Unsupported Media Type before your code runs.
@RestController
public class TransferController {

  public record TransferRequest(String from, String to, long amount) {}

  public String transfer(@RequestBody TransferRequest t) {
    return "accepted " + t.amount() + " from " + t.from() + " to " + t.to();
  }
}
