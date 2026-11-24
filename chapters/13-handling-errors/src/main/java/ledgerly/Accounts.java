package ledgerly;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** In-memory balances. Finished; NOT one of your tasks. */
@Service
public class Accounts {

  private final Map<String, Long> balances = new ConcurrentHashMap<>(Map.of("ACC-1", 52_000L, "ACC-2", 1_000L));

  public long balance(String number) {
    Long b = balances.get(number);
    if (b == null) {
      throw new AccountNotFoundException(number);
    }
    return b;
  }

  public void transfer(String from, String to, long amount) {
    long have = balance(from);
    balance(to);
    if (amount > have) {
      throw new InsufficientFundsException(from, amount - have);
    }
    balances.merge(from, -amount, Long::sum);
    balances.merge(to, amount, Long::sum);
  }
}
