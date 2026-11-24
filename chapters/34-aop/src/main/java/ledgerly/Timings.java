package ledgerly;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Component;

/** Collects how often each method ran and how long it took in total. Finished. */
@Component
public class Timings {

  private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();
  private final Map<String, LongAdder> nanos = new ConcurrentHashMap<>();

  public void record(String method, long elapsedNanos) {
    counts.computeIfAbsent(method, k -> new LongAdder()).increment();
    nanos.computeIfAbsent(method, k -> new LongAdder()).add(elapsedNanos);
  }

  public long count(String method) {
    LongAdder c = counts.get(method);
    return c == null ? 0 : c.sum();
  }

  /** method -> calls, sorted by name. */
  public Map<String, Long> counts() {
    Map<String, Long> out = new TreeMap<>();
    counts.forEach((k, v) -> out.put(k, v.sum()));
    return out;
  }

  public void clear() {
    counts.clear();
    nanos.clear();
  }
}
