package ledgerly;

import java.util.List;
import org.springframework.web.bind.annotation.RestController;

/**
 * The first endpoints of the Ledgerly accounts API.
 * Tasks 1 to 5 are here. The comment above each method says its task again.
 * Do NOT change the ACCOUNTS list; the checks rely on it.
 */
@RestController
public class AccountController {

  static final List<Account> ACCOUNTS = List.of(
      new Account("1000234567", "Asha Rao", 52_000),
      new Account("1000234568", "Vikram Shah", 8_500),
      new Account("1000234569", "Neha Iyer", 125_000),
      new Account("1000234570", "Imran Khan", 300));

  // Task 1: answer GET /status with plain text
  //
  // Make GET /status answer 200 with exactly the plain text:
  //     Ledgerly is open
  // (Not JSON. Just a String.) The method exists, but no URL is
  // mapped to it yet, so today the answer is 404 Not Found.
  String status() {
    throw new UnsupportedOperationException("TODO task 1: answer GET /status");
  }

  // Task 2: list the accounts as JSON at GET /accounts
  //
  // Answer 200 with every account in ACCOUNTS, in the same order:
  // four accounts, Asha Rao first and Imran Khan last. Each Account
  // is written by Spring as {"number":..., "holder":..., "balance":...}.
  List<Account> accounts() {
    throw new UnsupportedOperationException("TODO task 2: return the accounts");
  }

  // Task 3: mask an account number at GET /accounts/{number}/masked
  //
  // The number comes from the path. Answer with plain text: six
  // stars, then the last four characters of the number.
  //     GET /accounts/1000234567/masked   ->   ******4567
  // Keep the number a String. A number like 0000123456 starts with
  // zeros and an int would lose them.
  String masked(String number) {
    throw new UnsupportedOperationException("TODO task 3: mask the number");
  }

  // Task 4: accounts above a balance at GET /accounts/above?balance=N
  //
  // The limit comes from the query string, as an int. Answer with
  // the accounts whose balance is MORE than N, as a JSON list in
  // ACCOUNTS order.
  //     ?balance=10000  -> Asha Rao, Neha Iyer
  //     ?balance=52000  -> Neha Iyer only (52000 is not more than 52000)
  // A missing or non-numeric balance is a 400 automatically. Don't write that.
  List<Account> above(int balance) {
    throw new UnsupportedOperationException("TODO task 4: filter the accounts");
  }

  // Task 5: the transfer fee at GET /fee?amount=N&express=true
  //
  // amount (required, int). The fee is 1% of the amount (integer
  // division), but never less than 10.
  // express (optional, boolean, default false). When true, the fee is
  // doubled.
  //     /fee?amount=5000                -> 50
  //     /fee?amount=200                 -> 10     (1% would be 2, minimum is 10)
  //     /fee?amount=5000&express=true   -> 100
  // The answer is a plain number.
  int fee(int amount, boolean express) {
    throw new UnsupportedOperationException("TODO task 5: work out the fee");
  }
}
