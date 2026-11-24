package ledgerly;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    AccountRepository accounts = ctx.getBean(AccountRepository.class);
    CustomerRepository customers = ctx.getBean(CustomerRepository.class);
    PayeeRepository payees = ctx.getBean(PayeeRepository.class);
    TransferService service = ctx.getBean(TransferService.class);
    accounts.saveAll(List.of(new Account("ACC-1", 10_000), new Account("ACC-2", 500)));

    System.out.println("== cascade");
    Customer asha = new Customer("Asha Rao");
    asha.addPayee(new Payee("Mom", "ACC-2"));
    asha.addPayee(new Payee("Landlord", "ACC-7"));
    customers.save(asha);
    System.out.println("payees after saving the customer: " + payees.count());

    System.out.println("== transfer 3000 ACC-1 -> ACC-9 (no such account)");
    try {
      service.transfer("ACC-1", "ACC-9", 3000);
    } catch (AccountNotFoundException e) {
      System.out.println("failed: " + e.getMessage());
    }
    System.out.println("ACC-1 balance: " + accounts.findByNumber("ACC-1").orElseThrow().getBalance());

    System.out.println("== transferWithLimit 6000 (checked exception)");
    try {
      service.transferWithLimit("ACC-1", "ACC-2", 6000);
    } catch (DailyLimitExceededException e) {
      System.out.println("failed: " + e.getMessage());
    }
    System.out.println("ACC-1 balance: " + accounts.findByNumber("ACC-1").orElseThrow().getBalance());
    ctx.close();
  }
}
