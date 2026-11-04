package ledgerly;

import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A finished demo. NOT one of your tasks. It is a prototype: a new
 * token every time you ask. Compare it with your Receipt in task 4.
 */
@Component
@Scope("prototype")
public class OtpToken {

  private final String value = UUID.randomUUID().toString().substring(0, 6);

  public String value() {
    return value;
  }
}
