package ledgerly;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Exchange rates. */
// Task 4: choose a method by query parameter and by header
//
//   GET /rates                     -> all()       {"EUR":90.1,"GBP":105.4,"USD":83.25}
//   GET /rates?currency=USD        -> one(...)    83.25
//   GET /branches                  -> branchesV1()  "BLR01,DEL01"
//   GET /branches  + header X-Api-Version: 2  -> branchesV2()  "[BLR01] [DEL01]"
//
// Today one() is not mapped, so /rates?currency=USD answers all rates.
// Map one() to /rates with `params = "currency"`, and add
// `params = "!currency"` to all(). Then map branchesV2 to /branches with
// `headers = "X-Api-Version=2"` (branchesV1 is already mapped).
// Mapping both to plain "/rates" fails at startup: "Ambiguous mapping".
@RestController
public class RateController {

  static final Map<String, Double> RATES = new TreeMap<>(Map.of("USD", 83.25, "EUR", 90.10, "GBP", 105.40));

  @GetMapping("/rates")
  public Map<String, Double> all() {
    return RATES;
  }

  public Double one(@RequestParam String currency) {
    return RATES.get(currency);
  }

  @GetMapping("/branches")
  public String branchesV1() {
    return "BLR01,DEL01";
  }

  public String branchesV2() {
    return "[BLR01] [DEL01]";
  }
}
