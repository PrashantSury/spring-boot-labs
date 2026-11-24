package ledgerly;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * A singleton that needs a FRESH prototype StatementBuffer for every statement.
 *
 * Task 1: the stub injects ONE buffer in the constructor, so it is created once, when this singleton is built,
 *   and every statement after the first also contains the lines of all earlier statements.
 *   Inject ObjectProvider<StatementBuffer> instead and call getObject() inside print(...).
 */
@Service
public class StatementPrinter {

  private final StatementBuffer buffer;

  public StatementPrinter(StatementBuffer buffer) {
    this.buffer = buffer;
  }

  /** Returns "ACC-1 #<buffer number>: line1 | line2". */
  public String print(String account, List<String> lines) {
    lines.forEach(buffer::add);
    return account + " #" + buffer.number() + ": " + String.join(" | ", buffer.lines());
  }
}
