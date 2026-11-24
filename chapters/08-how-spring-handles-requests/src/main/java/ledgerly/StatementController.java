package ledgerly;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** A mini statement, as JSON or as CSV. */
// Task 2: two methods on ONE path, chosen by the Accept header
//
// GET /accounts/{number}/statement
//   Accept: application/json -> asJson(...)  (the JSON list below)
//   Accept: text/csv         -> asCsv(...)   (the text below)
//   Accept: application/xml  -> 406 Not Acceptable (nothing can produce it)
//
// Map BOTH methods to the same path, and give each a `produces` value.
// The method bodies are finished.
@RestController
public class StatementController {

  public record Line(String date, long amount) {}

  static final List<Line> LINES = List.of(new Line("2026-11-01", 50_000), new Line("2026-11-03", -1_200));

  public List<Line> asJson(@PathVariable String number) {
    return LINES;
  }

  public String asCsv(@PathVariable String number) {
    StringBuilder sb = new StringBuilder("date,amount\n");
    LINES.forEach(l -> sb.append(l.date()).append(',').append(l.amount()).append('\n'));
    return sb.toString();
  }
}
