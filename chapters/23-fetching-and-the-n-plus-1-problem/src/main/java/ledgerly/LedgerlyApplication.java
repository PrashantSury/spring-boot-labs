package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.support.TransactionTemplate;

/** The application, with a short tour of the SQL each approach sends. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    CustomerRepository customers = ctx.getBean(CustomerRepository.class);
    AccountRepository accounts = ctx.getBean(AccountRepository.class);
    SampleData.load(customers, accounts);
    TransactionTemplate tx = ctx.getBean(TransactionTemplate.class);

    System.out.println("== A. accounts over 20000 with their owner (task 2)");
    tx.executeWithoutResult(s -> accounts.findByBalanceGreaterThanOrderByNumber(20_000)
        .forEach(a -> System.out.println(a.getNumber() + " " + a.getCustomer().getName())));

    System.out.println("== B. customers with accounts, join fetch (task 1)");
    customers.findAllWithAccounts().forEach(c -> System.out.println(c.getName() + " " + c.getAccounts().size()));

    System.out.println("== C. ownerSummaries (tasks 3 + 4)");
    ctx.getBean(StatementService.class).ownerSummaries().forEach(System.out::println);
    ctx.close();
  }
}
