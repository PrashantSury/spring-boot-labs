package ledgerly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** The application, with a short tour of AccountDao. Finished; NOT one of your tasks. */
@SpringBootApplication
public class LedgerlyApplication {

  public static void main(String[] args) {
    var ctx = SpringApplication.run(LedgerlyApplication.class, args);
    AccountDao dao = ctx.getBean(AccountDao.class);
    System.out.println("all accounts          " + dao.findAll());
    System.out.println("ACC-2                 " + dao.findByNumber("ACC-2"));
    System.out.println("ACC-9                 " + dao.findByNumber("ACC-9"));
    System.out.println("owner like 'Asha'     " + dao.searchByOwner("Asha").stream().map(Account::number).toList());
    System.out.println("owner like injection  " + dao.searchByOwner("' OR 1=1 --").stream().map(Account::number).toList());
    System.out.println("new id for ACC-4      " + dao.insert("ACC-4", "Meera Iyer", 0));
    System.out.println("deposit 500 to ACC-4  " + dao.deposit("ACC-4", 500));
    System.out.println("deposit 500 to ACC-9  " + dao.deposit("ACC-9", 500));
    System.out.println("withdraw 2000 ACC-2   " + dao.withdraw("ACC-2", 2000));
    System.out.println("withdraw 600 ACC-2    " + dao.withdraw("ACC-2", 600));
    System.out.println("ACC-2 after           " + dao.findByNumber("ACC-2").map(Account::balance).orElse(-1L));
    ctx.close();
  }
}
