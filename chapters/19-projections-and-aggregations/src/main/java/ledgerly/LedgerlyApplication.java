package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour of the reports. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    TxnRepository repo = ctx.getBean(TxnRepository.class);
    repo.saveAll(SampleData.txns());
    System.out.println("== lines (projection)");
    repo.findByAccountNumberOrderByAmountDesc("ACC-1").forEach(l -> System.out.println(l.getDescription() + " " + l.getAmount()));
    System.out.println("== by category       " + repo.spendByCategory("ACC-1"));
    System.out.println("== total ACC-1       " + repo.totalSpent("ACC-1"));
    System.out.println("== total ACC-9       " + repo.totalSpent("ACC-9"));
    System.out.println("== monthly");
    repo.monthlyTotals("ACC-1").forEach(m -> System.out.println(m.getPeriod() + " " + m.getTotal()));
    ctx.close();
  }
}
