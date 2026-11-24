package ledgerly;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Reports on transactions. Each stub method throws; replace the default body. */
public interface TxnRepository extends JpaRepository<Txn, Long> {

  // Task 1: interface projection. A derived query that returns List<TxnLine> instead of List<Txn>:
  //   only description and amount are selected (not the 2 KB rawPayload). Biggest amount first.
  default List<TxnLine> findByAccountNumberOrderByAmountDesc(String accountNumber) {
    throw new UnsupportedOperationException("Task 1: return the TxnLine projection from a derived query");
  }

  // Task 2: spend per category for one account, biggest total first, as CategoryTotal records.
  //   JPQL constructor expression: select new ledgerly.CategoryTotal(t.category, sum(t.amount), count(t))
  //   ... group by t.category order by sum(t.amount) desc
  default List<CategoryTotal> spendByCategory(String accountNumber) {
    throw new UnsupportedOperationException("Task 2: @Query with select new ... group by");
  }

  // Task 3: total spent by one account. An account with no rows must give 0, not an error.
  //   SUM over zero rows is NULL in SQL, and NULL cannot become a primitive long. Use coalesce(..., 0).
  default long totalSpent(String accountNumber) {
    throw new UnsupportedOperationException("Task 3: @Query with sum and coalesce");
  }

  // Task 4: month-by-month totals with a NATIVE query (real SQL, table/column names):
  //   select formatdatetime(booked_at, 'yyyy-MM') as period, sum(amount) as total from txn
  //   where account_number = :acc group by formatdatetime(booked_at, 'yyyy-MM') order by period
  //   nativeQuery = true. The aliases period/total match MonthTotal's getters.
  default List<MonthTotal> monthlyTotals(String accountNumber) {
    throw new UnsupportedOperationException("Task 4: a native query with nativeQuery = true");
  }
}
