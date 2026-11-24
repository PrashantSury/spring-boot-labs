package ledgerly;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

/** A clock the tests can move forward ("15 minutes later..."). Finished; NOT one of your tasks. */
public class LabClock extends Clock {

  private volatile Instant now = Instant.now();

  public void advance(Duration d) {
    now = now.plus(d);
  }

  @Override
  public Instant instant() {
    return now;
  }

  @Override
  public ZoneId getZone() {
    return ZoneOffset.UTC;
  }

  @Override
  public Clock withZone(ZoneId zone) {
    return this;
  }
}
