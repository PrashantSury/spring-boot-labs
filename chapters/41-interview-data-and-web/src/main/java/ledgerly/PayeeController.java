package ledgerly;

import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Payees: add, read, list. */
@RestController
public class PayeeController {

  private final PayeeRepository payees;

  public PayeeController(PayeeRepository payees) {
    this.payees = payees;
  }

  // Task 2 (part 1): a NEW payee -> 201 Created + Location: /payees/{id};
  //   an accountNumber that already exists -> throw new DuplicatePayee(accountNumber) (409, see ApiErrors).
  // Task 3 (part 2): @Valid on the body.
  @PostMapping("/payees")
  public ResponseEntity<Views.PayeeView> add(@RequestBody PayeeRequest r) {
    Payee saved = payees.save(new Payee(r.name(), r.ifsc(), r.accountNumber()));
    return ResponseEntity.ok(Views.PayeeView.of(saved));
  }

  // Task 2 (part 2): unknown id -> throw new NoSuchThing("payee", id) (404, see ApiErrors).
  @GetMapping("/payees/{id}")
  public Views.PayeeView get(@PathVariable long id) {
    return Views.PayeeView.of(payees.findById(id).orElseThrow());
  }

  // Task 3 (part 3): never let a client ask for a million rows: size must be at most 100:
  //   @RequestParam(defaultValue = "20") @Max(100) int size   (Spring validates controller parameters: 400)
  @GetMapping("/payees")
  public List<Views.PayeeView> list(@RequestParam(defaultValue = "20") int size) {
    return payees.findAll(PageRequest.of(0, size, Sort.by("id"))).map(Views.PayeeView::of).getContent();
  }
}
