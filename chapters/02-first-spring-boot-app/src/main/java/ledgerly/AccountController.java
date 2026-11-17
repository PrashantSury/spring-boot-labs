package ledgerly;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
  @GetMapping("/status")
  String status() {
    return "Ledgerly is open";
  }

  // Task 2: list the accounts as JSON at GET /accounts
  //
  // Answer 200 with every account in ACCOUNTS, in the same order:
  // four accounts, Asha Rao first and Imran Khan last. Each Account
  // is written by Spring as {"number":..., "holder":..., "balance":...}.
  @GetMapping ("/accounts")
  List<Account> accounts() {
    return ACCOUNTS;
  }

  // Task 3: mask an account number at GET /accounts/{number}/masked
  //
  // The number comes from the path. Answer with plain text: six
  // stars, then the last four characters of the number.
  //     GET /accounts/1000234567/masked   ->   ******4567
  // Keep the number a String. A number like 0000123456 starts with
  // zeros and an int would lose them.
  @GetMapping("/accounts/{number}/masked")
  String masked(@PathVariable("number") String number) {
    return "******" + lastFour(number);
  }
  private String lastFour(String s) {
    return s.length() <= 4 ? s : s.substring(s.length() - 4);
  }

/*   Task 4: accounts above a balance at GET /accounts/above?balance=N

   The limit comes from the query string, as an int. Answer with
   the accounts whose balance is MORE than N, as a JSON list in
   ACCOUNTS order.
       ?balance=10000  -> Asha Rao, Neha Iyer
       ?balance=52000  -> Neha Iyer only (52000 is not more than 52000)
   A missing or non-numeric balance is a 400 automatically. Don't write that.*/

  @GetMapping("/accounts/above")
  List<Account> above(@RequestParam int balance) {

    List<Account> accounts = new ArrayList<>();
    for(Account acc:ACCOUNTS){
     if( acc.balance() > balance ){
       accounts.add(acc);
     }
    }
    return accounts;
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
  @GetMapping("/fee")
  int fee(@RequestParam int amount, @RequestParam(required = false) boolean express ) {

       int fee =Math.min(amount / 100,10);
       if(express){
         return fee * 2;
       }
       return fee;
  }
}
