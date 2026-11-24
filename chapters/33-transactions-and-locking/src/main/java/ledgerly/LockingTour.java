package ledgerly;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** 10 parallel withdrawals of 100 from 1,000, done two ways. Finished. */
@Component
public class LockingTour implements CommandLineRunner {

  private final AccountRepository accounts;
  private final WithdrawWorker worker;
  private final WithdrawService service;

  public LockingTour(AccountRepository accounts, WithdrawWorker worker, WithdrawService service) {
    this.accounts = accounts;
    this.worker = worker;
    this.service = service;
  }

  @Override
  public void run(String... args) throws Exception {
    race("optimistic + retry", id -> service.withdrawWithRetry(id, new BigDecimal("100")));
    race("pessimistic lock   ", id -> worker.withdrawLocked(id, new BigDecimal("100")));
  }

  interface Call {
    BigDecimal apply(long id);
  }

  private void race(String name, Call call) throws Exception {
    long id = accounts.save(new Account("asha", new BigDecimal("1000.00"))).getId();
    ExecutorService pool = Executors.newFixedThreadPool(10);
    List<Future<BigDecimal>> fs = new ArrayList<>();
    for (int i = 0; i < 10; i++) fs.add(pool.submit(() -> call.apply(id)));
    int ok = 0;
    int failed = 0;
    for (Future<BigDecimal> f : fs) {
      try {
        f.get();
        ok++;
      } catch (Exception e) {
        failed++;
      }
    }
    pool.shutdown();
    Account a = accounts.findById(id).orElseThrow();
    System.out.printf("%s: %d ok, %d failed, final balance %s, version %s%n", name, ok, failed, a.getBalance(), a.getVersion());
  }
}
