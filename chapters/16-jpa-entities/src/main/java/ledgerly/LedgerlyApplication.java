package ledgerly;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour of the Account entity. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    EntityManager em = ctx.getBean(EntityManagerFactory.class).createEntityManager();
    em.getTransaction().begin();
    Account asha = new Account("ACC-1", "Asha Rao", AccountType.SAVINGS, new BigDecimal("52000.00"));
    System.out.println("before persist   id=" + asha.getId());
    em.persist(asha);
    System.out.println("after persist    id=" + asha.getId());
    asha.deposit(new BigDecimal("0.10"));
    asha.deposit(new BigDecimal("0.20"));
    em.getTransaction().commit();
    em.clear();
    Account loaded = em.find(Account.class, asha.getId());
    System.out.println("loaded           " + loaded);
    System.out.println("same object?     " + (loaded == asha) + ", equal? " + loaded.equals(asha));
    System.out.println("row in the table " + java.util.Arrays.toString((Object[]) em.createNativeQuery(
        "select number, type, balance from accounts").getSingleResult()));
    System.out.println("0.10 + 0.20 as double: " + (0.10 + 0.20));
    em.close();
    ctx.close();
  }
}
