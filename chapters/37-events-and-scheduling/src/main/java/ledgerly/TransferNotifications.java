package ledgerly;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Sends "money received" SMS for booked transfers. */
@Component
public class TransferNotifications {

  private final Notifier notifier;

  public TransferNotifications(Notifier notifier) {
    this.notifier = notifier;
  }

  // Task 2: a plain @EventListener runs immediately, INSIDE the transfer's transaction: if the ledger then
  //   rejects the transfer, the customer has already got an SMS for money that never arrived.
  //   Replace it with @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT).
  // Task 3 (part 2): the SMS gateway takes 300 ms; don't make the customer wait for it. Add @Async.
  //   Name the async threads "notify-..." in application.properties: spring.task.execution.thread-name-prefix=notify-
  @EventListener
  public void on(TransferCompleted e) {
    notifier.send(e.to() + " received " + e.amount() + " from " + e.from());
  }
}
