package ledgerly;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The chapter 38 service (finished). Your job here: WHO may call each method (tasks 2 and 3). */
@Service
public class TransferService {

  private final AccountRepository accounts;
  private final TransferRepository transfers;

  public TransferService(AccountRepository accounts, TransferRepository transfers) {
    this.accounts = accounts;
    this.transfers = transfers;
  }

  // Task 3 (part 2): tellers and auditors may read any account; a customer only their own:
  //   @PreAuthorize("hasAnyRole('TELLER','AUDITOR') or @ownership.owns(authentication, #number)")
  @Transactional(readOnly = true)
  public AccountDto account(String number) {
    Account a = accounts.findByNumber(number).orElseThrow(() -> new NoSuchAccount(number));
    return new AccountDto(a.getNumber(), a.getCustomer().getName(), a.getBalance());
  }

  // Task 3 (part 3): load first, then decide (the rule needs the transfer's accounts):
  //   @PostAuthorize("hasAnyRole('TELLER','AUDITOR') or @ownership.involved(authentication, returnObject)")
  @Transactional(readOnly = true)
  public TransferDto find(long id) {
    return transfers.findById(id).map(TransferDto::of).orElseThrow(() -> new NoSuchTransfer(id));
  }

  // Task 2 (part 2): a teller may move money from any account; a customer only from their OWN account:
  //   @PreAuthorize("hasRole('TELLER') or (hasRole('CUSTOMER') and @ownership.owns(authentication, #request.from()))")
  @Transactional
  public TransferResult transfer(String key, TransferRequest request) {
    var existing = transfers.findByIdempotencyKey(key);
    if (existing.isPresent()) {
      if (!existing.get().sameRequestAs(request)) throw new IdempotencyConflict(key);
      return new TransferResult(TransferDto.of(existing.get()), true);
    }
    List<String> order = List.of(request.from(), request.to()).stream().sorted().toList();
    for (String number : order) {
      accounts.lockByNumber(number).orElseThrow(() -> new NoSuchAccount(number));
    }
    Account from = accounts.lockByNumber(request.from()).orElseThrow();
    Account to = accounts.lockByNumber(request.to()).orElseThrow();
    from.debit(request.amount());
    to.credit(request.amount());
    Transfer saved = transfers.save(new Transfer(key, request.from(), request.to(), request.amount()));
    return new TransferResult(TransferDto.of(saved), false);
  }
}
