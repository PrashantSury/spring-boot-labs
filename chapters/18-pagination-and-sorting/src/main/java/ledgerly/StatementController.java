package ledgerly;

import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** A customer's statement, one page at a time. */
@RestController
public class StatementController {

  static final int MAX_SIZE = 100;

  private final TxnRepository repo;

  public StatementController(TxnRepository repo) {
    this.repo = repo;
  }

  // Task 2: GET /accounts/{number}/transactions?page=0&size=20&sort=bookedAt,desc
  //   Defaults: page 0, size 20, sort "bookedAt,desc". A size above MAX_SIZE is cut to MAX_SIZE.
  //   Build PageRequest.of(page, size, sortFrom(sort)), call the repository, and map to
  //   PageResponse(content as TxnView, page.getNumber(), page.getSize(), totalElements, totalPages).
  @GetMapping("/accounts/{number}/transactions")
  public PageResponse statement(@PathVariable String number,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "bookedAt,desc") String sort) {
    throw new UnsupportedOperationException("Task 2: build a PageRequest and return a PageResponse");
  }

  // Task 3: turn "amount,asc" into Sort.by(ASC, "amount").
  //   "amount" alone means ascending. Only bookedAt and amount are allowed: anything else
  //   (e.g. "description" or "id; drop table") -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ...).
  //   Add a second sort on "id" (same direction) so rows with equal values keep a stable order across pages.
  static Sort sortFrom(String sort) {
    return Sort.unsorted();
  }

  // Task 4 (part 2): GET /accounts/{number}/feed?page=0  (mobile infinite scroll, size 10, newest first)
  //   Use findSliceByAccountNumber and return SliceResponse(content, page number, hasNext()).
  @GetMapping("/accounts/{number}/feed")
  public SliceResponse feed(@PathVariable String number, @RequestParam(defaultValue = "0") int page) {
    throw new UnsupportedOperationException("Task 4: return a SliceResponse");
  }
}
