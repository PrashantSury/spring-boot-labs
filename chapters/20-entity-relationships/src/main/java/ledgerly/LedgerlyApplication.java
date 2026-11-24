package ledgerly;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tools.jackson.databind.json.JsonMapper;

/** The application, with a short tour of the relationship. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    EntityManager em = ctx.getBean(EntityManagerFactory.class).createEntityManager();
    em.getTransaction().begin();
    Customer asha = new Customer("Asha Rao");
    em.persist(asha);
    Account savings = new Account("ACC-1", 52_000);
    Account current = new Account("ACC-2", 1_000);
    asha.addAccount(savings);
    asha.addAccount(current);
    em.persist(savings);
    em.persist(current);
    em.getTransaction().commit();
    em.clear();
    System.out.println("-- load ACC-2 only");
    Account acc = em.find(Account.class, current.getId());
    System.out.println("-- now ask for its customer's name");
    System.out.println("owner of ACC-2: " + acc.getCustomer().getName());
    System.out.println("-- load the customer and its accounts");
    Customer c = em.find(Customer.class, asha.getId());
    System.out.println("accounts: " + c.getAccounts().stream().map(Account::getNumber).toList());
    System.out.println("JSON: " + JsonMapper.builder().build().writeValueAsString(c));
    em.close();
    ctx.close();
  }
}
