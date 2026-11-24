package ledgerly;

import org.springframework.stereotype.Component;

/** Prints instead of sending. Safe for laptops and tests. */
// Task 4 (part 1): this bean must exist in EVERY profile except prod
// (and when no profile is active). Use @Profile with "not prod".
@Component
public class FakeSmsGateway implements SmsGateway {

  @Override
  public String send(String phone, String text) {
    return "FAKE sms to " + phone + ": " + text;
  }
}
