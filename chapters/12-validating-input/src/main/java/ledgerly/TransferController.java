package ledgerly;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Transfers and statements. The bodies are finished; the validation is yours. */
@RestController
public class TransferController {

  // Task 1 (part 2): ask Spring to validate the body.
  //   Without one annotation on the parameter, every rule on TransferRequest
  //   is ignored. With it, a broken body answers 400 before this method runs.
  @PostMapping("/transfers")
  public ResponseEntity<String> transfer(@RequestBody TransferRequest t) {
    return ResponseEntity.status(201).body("sent " + t.amount() + " from " + t.from() + " to " + t.beneficiary().name());
  }

  // Task 5: validate a path variable and a query parameter
  //   number: @AccountNumber
  //   months: between 1 and 12 (default 3)
  //   Put the constraints straight on the parameters. Spring MVC validates
  //   them itself and answers 400 (a HandlerMethodValidationException).
  //   Do NOT add @Validated to the class (see the trap in the README).
  @GetMapping("/accounts/{number}/statement")
  public String statement(@PathVariable String number,
      @RequestParam(defaultValue = "3") int months) {
    return "statement for " + number + ", last " + months + " months";
  }
}
