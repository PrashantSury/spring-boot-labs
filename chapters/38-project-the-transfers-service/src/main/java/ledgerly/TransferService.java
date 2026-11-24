package ledgerly;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The heart of the service. */
@Service
public class TransferService {

  private final AccountRepository accounts;
  private final TransferRepository transfers;

  public TransferService(AccountRepository accounts, TransferRepository transfers) {
    this.accounts = accounts;
    this.transfers = transfers;
  }

  /** Finished. */
  @Transactional(readOnly = true)
  public AccountDto account(String number) {
    Account a = accounts.findByNumber(number).orElseThrow(() -> new NoSuchAccount(number));
    return new AccountDto(a.getNumber(), a.getCustomer().getName(), a.getBalance());
  }

  // Task 4 (part 1): GET /transfers/{id}: TransferDto.of(...) or throw new NoSuchTransfer(id).
  @Transactional(readOnly = true)
  public TransferDto find(long id) {
    throw new UnsupportedOperationException("Task 4: TransferService.find");
  }

  // Task 2: move the money, all or nothing (this method is one transaction):
  //   1. Lock BOTH accounts with accounts.lockByNumber(...), ALWAYS in the same order (sort the two numbers first),
  //      so A->B and B->A running at the same time can't deadlock. Unknown number -> new NoSuchAccount(number).
  //   2. from.debit(amount), then to.credit(amount). (debit throws InsufficientFunds; credit throws AccountFrozen,
  //      and then the debit must be undone: it is, because the exception rolls the whole transaction back.)
  //   3. Save new Transfer(key, from, to, amount) and return new TransferResult(TransferDto.of(saved), false).
  // Task 3: double-submit. FIRST look the key up with transfers.findByIdempotencyKey(key):
  //   found and sameRequestAs(request) -> return new TransferResult(TransferDto.of(existing), true), move NO money
  //   found but a different request    -> throw new IdempotencyConflict(key)
  @Transactional
  public TransferResult transfer(String key, TransferRequest request) {
    throw new UnsupportedOperationException("Task 2: TransferService.transfer");
  }
}
