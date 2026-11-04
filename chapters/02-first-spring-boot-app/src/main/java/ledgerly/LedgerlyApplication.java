package ledgerly;

import org.springframework.boot.SpringApplication;

/**
 * Ledgerly starts here. This file has task 6.
 */
// Task 6 (Interview): make this the Spring Boot application class
//
// Today the app cannot start: this class is not yet a configuration,
// so nothing is set up and no web server bean exists. Give
// LedgerlyApplication what a Spring Boot application class needs, so it is:
//
//   - a configuration     (a source of beans for the context),
//   - auto-configured     (defaults picked from the classpath, such as
//                          a web server because Spring MVC is there),
//   - component-scanning  (it finds AccountController in package
//                          ledgerly and below).
//
// ONE annotation gives you all three. Be ready to name the three
// things it switches on, and what each one does.
public class LedgerlyApplication {

  public static void main(String[] args) {
    SpringApplication.run(LedgerlyApplication.class, args);
  }
}
