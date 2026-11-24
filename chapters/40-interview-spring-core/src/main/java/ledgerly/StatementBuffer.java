package ledgerly;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** A scratch buffer for ONE statement. Prototype: every lookup gives a new one. Finished. */
@Component
@Scope("prototype")
public class StatementBuffer {

  private static final AtomicInteger created = new AtomicInteger();

  private final int number = created.incrementAndGet();
  private final List<String> lines = new ArrayList<>();

  public void add(String line) {
    lines.add(line);
  }

  public List<String> lines() {
    return List.copyOf(lines);
  }

  public int number() {
    return number;
  }
}
