package ledgerly;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/** Customers. */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  // Task 1: every customer WITH its accounts, in ONE select:
  //   @Query("select distinct c from Customer c left join fetch c.accounts order by c.name")
  //   (left: customers without accounts stay in; order by name for a stable list)
  default List<Customer> findAllWithAccounts() {
    throw new UnsupportedOperationException("Task 1: @Query with left join fetch");
  }
}
