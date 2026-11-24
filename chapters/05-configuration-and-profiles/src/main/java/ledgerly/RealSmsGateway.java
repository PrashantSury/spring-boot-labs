package ledgerly;

import org.springframework.stereotype.Component;

/** Would call the SMS vendor. Only in production. */
// Task 4 (part 2): this bean must exist ONLY when the prod profile is active.
@Component
public class RealSmsGateway implements SmsGateway {

  @Override
  public String send(String phone, String text) {
    return "VENDOR sms to " + phone + ": " + text;
  }
}
