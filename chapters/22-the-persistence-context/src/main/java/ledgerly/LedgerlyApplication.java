package ledgerly;

import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    AccountRepository repo = ctx.getBean(AccountRepository.class);
    AccountService service = ctx.getBean(AccountService.class);
    repo.saveAll(List.of(new Account("ACC-1", "Asha Rao"), new Account("ACC-2", "Vikram Shah")));
    System.out.println("== 1. rename ACC-1 (no save call)");
    service.rename("ACC-1", "Asha R. Kulkarni");
    System.out.println("owner now: " + repo.findByNumber("ACC-1").orElseThrow().getOwner());
    System.out.println("== 2. load id 1 twice");
    System.out.println("same object? " + service.loadTwiceIsSameObject(1));
    System.out.println("== 3. change a detached copy");
    Account detached = repo.findByNumber("ACC-2").orElseThrow();
    detached.setOwner("Vikram S. Shah");
    Account managed = service.updateDetached(detached);
    System.out.println("returned the same object? " + (managed == detached));
    System.out.println("owner now: " + repo.findByNumber("ACC-2").orElseThrow().getOwner());
    System.out.println("== 4. change ACC-2 to ACC-1");
    try {
      service.changeNumber("ACC-2", "ACC-1");
    } catch (RuntimeException e) {
      System.out.println("caught " + e.getClass().getSimpleName() + ": " + e.getMessage().split(";")[0]);
    }
    ctx.close();
  }
}
