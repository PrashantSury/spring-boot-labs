package ledgerly;

import org.springframework.stereotype.Service;

/**
 * Task 4 (part 1): pays ONE payment in its OWN transaction.
 * Move the body of TransferService.payOne here and make this method @Transactional.
 * Because TransferService calls it through the injected bean (a proxy), the transaction starts.
 */
@Service
public class PaymentWorker {


}
