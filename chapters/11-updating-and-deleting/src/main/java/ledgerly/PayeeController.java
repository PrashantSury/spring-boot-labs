package ledgerly;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Saved payees (beneficiaries). POST and GET are finished. Tasks 1-4 are
 * the update and delete endpoints. Every answer that returns a payee also
 * sends its version as an ETag header: ETag: "1".
 */
@RestController
public class PayeeController {

  private final Map<Long, Payee> payees = new ConcurrentHashMap<>();
  private final AtomicLong ids = new AtomicLong();

  public PayeeController() {
    payees.put(1L, new Payee(1, "Vikram Shah", "50100012345678", "HDFC0001234", "Landlord", 1));
    ids.set(1);
  }

  @PostMapping("/payees")
  public ResponseEntity<Payee> create(@RequestBody PayeeRequest r) {
    if (!complete(r)) {
      return ResponseEntity.badRequest().build();
    }
    Payee p = new Payee(ids.incrementAndGet(), r.name(), r.accountNumber(), r.ifsc(), r.nickname(), 1);
    payees.put(p.id(), p);
    return ResponseEntity.created(URI.create("/payees/" + p.id())).eTag(etag(p)).body(p);
  }

  @GetMapping("/payees/{id}")
  public ResponseEntity<Payee> get(@PathVariable long id) {
    Payee p = payees.get(id);
    return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok().eTag(etag(p)).body(p);
  }

  // Task 1: PUT replaces the WHOLE payee
  //
  //   PUT /payees/1 {"name":"Vikram S","accountNumber":"50100012345678","ifsc":"HDFC0001234"}
  //   -> 200, the new payee, version + 1, and nickname is now null (it was not sent)
  //   unknown id -> 404 (PUT does NOT create here)
  //   name, accountNumber or ifsc missing/blank -> 400 (use complete(r))
  //
  // Task 4 (part 1): if the client sends If-Match: "N" and N is not the current
  // version -> 412 Precondition Failed, and nothing changes. No If-Match: allowed.
  @PutMapping("/payees/{id}")
  public ResponseEntity<Payee> replace(@PathVariable long id, @RequestBody PayeeRequest r,
      @RequestHeader(name = "If-Match", required = false) String ifMatch) {
    return ResponseEntity.status(501).build(); // TODO tasks 1 and 4
  }

  // Task 2: PATCH changes only the fields that are sent
  //
  //   PATCH /payees/1 {"nickname":"Rent"}   -> only nickname changes, version + 1
  //   PATCH /payees/1 {"nickname":null}     -> nickname cleared (the key IS present)
  //   PATCH /payees/1 {}                    -> 200, nothing changes, version stays the same
  //   allowed keys: name, ifsc, nickname
  //   "accountNumber" -> 400 (it cannot change; delete and re-add the payee)
  //   any other key   -> 400
  //   name or ifsc sent as null or blank -> 400
  //   unknown id -> 404 · If-Match mismatch -> 412 (task 4)
  //
  // The body is a Map, so you can tell "key absent" from "key present with null".
  @PatchMapping("/payees/{id}")
  public ResponseEntity<Payee> patch(@PathVariable long id, @RequestBody Map<String, Object> changes,
      @RequestHeader(name = "If-Match", required = false) String ifMatch) {
    return ResponseEntity.status(501).build(); // TODO tasks 2 and 4
  }

  // Task 3: DELETE is idempotent
  //
  //   DELETE /payees/1 -> 204, the payee is gone
  //   DELETE /payees/1 again -> 204 again (same end state: it is gone)
  //   This lab's choice: 204 every time, even for an id that never existed.
  @DeleteMapping("/payees/{id}")
  public ResponseEntity<Void> delete(@PathVariable long id) {
    return ResponseEntity.status(501).build(); // TODO task 3
  }

  // ---------- finished helpers ----------

  static boolean complete(PayeeRequest r) {
    return notBlank(r.name()) && notBlank(r.accountNumber()) && notBlank(r.ifsc());
  }

  private static boolean notBlank(String s) {
    return s != null && !s.isBlank();
  }

  /** The ETag for a payee: its version in quotes, like "3". */
  static String etag(Payee p) {
    return "\"" + p.version() + "\"";
  }

  /** Task 4 (part 2): true when If-Match was sent and does not equal etag(p). */
  static boolean stale(Payee p, String ifMatch) {
    return false; // TODO task 4
  }
}
