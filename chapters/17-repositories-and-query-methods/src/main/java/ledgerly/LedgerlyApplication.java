package ledgerly;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour of AccountRepository. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    AccountRepository repo = ctx.getBean(AccountRepository.class);
    repo.saveAll(List.of(
        new Account("ACC-1", "Asha Rao", 7, new BigDecimal("52000.00")),
        new Account("ACC-2", "Asha Rao", 7, new BigDecimal("900.00")),
        new Account("ACC-3", "Asha Rao", 7, new BigDecimal("15000.00")),
        new Account("ACC-4", "Vikram Shah", 8, new BigDecimal("80000.00"))));
    System.out.println("repository class      " + repo.getClass().getSimpleName() + " (a proxy Spring Data wrote)");
    System.out.println("count()               " + repo.count());
    System.out.println("findByNumber(ACC-3)   " + repo.findByNumber("ACC-3"));
    System.out.println("findByNumber(ACC-9)   " + repo.findByNumber("ACC-9"));
    System.out.println("cust 7, > 1000        " + repo.findByCustomerIdAndBalanceGreaterThanOrderByBalanceDesc(7, new BigDecimal("1000")));
    System.out.println("existsByNumber(ACC-4) " + repo.existsByNumber("ACC-4"));
    System.out.println("searchOwner(\"asha\")   " + repo.searchOwner("asha").stream().map(Account::getNumber).toList());
    System.out.println("freeze customer 7     " + repo.freezeAllOfCustomer(7) + " rows");
    System.out.println("countByStatus(FROZEN) " + repo.countByStatus(AccountStatus.FROZEN));
    ctx.close();
  }
}
