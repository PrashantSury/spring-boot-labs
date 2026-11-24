package ledgerly;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** POST /transfers (header Idempotency-Key) and GET /transfers/{id}. */
@RestController
public class TransferController {

  private final TransferService service;

  public TransferController(TransferService service) {
    this.service = service;
  }

  // Task 1 (part 2): add @Valid before @RequestBody.
  // Task 4 (part 2): a NEW transfer -> 201 Created + Location: /transfers/{id} + the TransferDto body.
  //                  a REPLAYED one (result.replayed()) -> 200 OK + the original TransferDto, no Location.
  @PostMapping("/transfers")
  public ResponseEntity<TransferDto> create(@RequestHeader("Idempotency-Key") String key,
      @RequestBody TransferRequest request) {
    TransferResult result = service.transfer(key, request);
    return ResponseEntity.ok(result.transfer());
  }

  @GetMapping("/transfers/{id}")
  public TransferDto get(@PathVariable long id) {
    return service.find(id);
  }
}
