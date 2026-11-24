package ledgerly;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** POST /transfers (header Idempotency-Key) and GET /transfers/{id}. Finished (chapter 38). */
@RestController
public class TransferController {

  private final TransferService service;

  public TransferController(TransferService service) {
    this.service = service;
  }

  @PostMapping("/transfers")
  public ResponseEntity<TransferDto> create(@RequestHeader("Idempotency-Key") String key,
      @Valid @RequestBody TransferRequest request) {
    TransferResult result = service.transfer(key, request);
    if (result.replayed()) return ResponseEntity.ok(result.transfer());
    return ResponseEntity.created(URI.create("/transfers/" + result.transfer().id())).body(result.transfer());
  }

  @GetMapping("/transfers/{id}")
  public TransferDto get(@PathVariable long id) {
    return service.find(id);
  }
}
