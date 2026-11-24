package ledgerly;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Account maintenance. Every method works with the persistence context, not against it. */
@Service
public class AccountService {

  private final AccountRepository accounts;

  public AccountService(AccountRepository accounts) {
    this.accounts = accounts;
  }

  // Task 1: dirty checking. Inside a transaction a loaded entity is MANAGED: change it and
  //   Hibernate writes an UPDATE at commit by itself. Make this method @Transactional.
  //   Do NOT add accounts.save(...): the point is that it is not needed.
  public void rename(String number, String newOwner) {
    find(number).setOwner(newOwner);
  }

  // Task 2: the first-level cache. In ONE persistence context, the same id gives the SAME object
  //   (and the second findById sends no SQL). Run both loads in one read-only transaction:
  //   @Transactional(readOnly = true)
  public boolean loadTwiceIsSameObject(long id) {
    Account first = accounts.findById(id).orElseThrow();
    Account second = accounts.findById(id).orElseThrow();
    return first == second;
  }

  // Task 3: a DETACHED entity (loaded in an earlier, finished transaction, e.g. sent to a form and
  //   back) is not tracked any more. Changing it does nothing until you merge it:
  //   return accounts.save(detached); (save() calls merge() for an entity that has an id)
  //   merge returns a NEW managed copy; the object you passed in stays detached.
  @Transactional
  public Account updateDetached(Account detached) {
    return detached;
  }

  // Task 4: flush. Changing the number to one that is taken fails on the unique constraint,
  //   but only when Hibernate FLUSHES, which by default is at commit, AFTER this method returned:
  //   the try/catch below never sees it. Call accounts.flush() inside the try.
  @Transactional
  public void changeNumber(String oldNumber, String newNumber) {
    Account a = find(oldNumber);
    try {
      a.setNumber(newNumber);
    } catch (DataIntegrityViolationException e) {
      throw new DuplicateAccountException(newNumber);
    }
  }

  private Account find(String number) {
    return accounts.findByNumber(number).orElseThrow();
  }
}
