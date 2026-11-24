package ledgerly;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

/** Transactions. Spring Data adds LIMIT/OFFSET and ORDER BY from the Pageable you pass. */
public interface TxnRepository extends JpaRepository<Txn, Long> {

  // Task 1: one page of an account's transactions. Derived query + a Pageable parameter.
  //   A Page also runs a COUNT query, so it knows totalElements and totalPages.
  default Page<Txn> findByAccountNumber(String accountNumber, Pageable pageable) {
    throw new UnsupportedOperationException("Task 1: derived query returning Page<Txn>");
  }

  // Task 4 (part 1): the same, as a Slice: NO count query, it only knows whether a next page exists.
  default Slice<Txn> findSliceByAccountNumber(String accountNumber, Pageable pageable) {
    throw new UnsupportedOperationException("Task 4: derived query returning Slice<Txn>");
  }
}
