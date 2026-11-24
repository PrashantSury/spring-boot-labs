package ledgerly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** A finished demo. NOT one of your tasks. Counts calls by method name. */
@Component
public class CallCounter {

  private final Map<String, Integer> counts = new ConcurrentHashMap<>();

  public void record(String method) {
    counts.merge(method, 1, Integer::sum);
  }

  public int count(String method) {
    return counts.getOrDefault(method, 0);
  }
}
