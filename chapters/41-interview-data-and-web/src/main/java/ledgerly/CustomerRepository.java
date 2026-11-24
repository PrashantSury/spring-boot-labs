package ledgerly;

import org.springframework.data.jpa.repository.JpaRepository;

/** Task 1 (part 2) lives here. */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  // Task 1 (part 2): one query for customers AND their accounts (no N+1):
  //   @Query("select distinct c from Customer c left join fetch c.accounts order by c.id")
  //   List<Customer> findAllWithAccounts();
}
