package ledgerly;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** The logic behind @AccountNumber. */
// Task 4: a custom constraint
//
// isValid(value) is true when value is "ACC-" followed by 1 to 8 digits:
//   ACC-1, ACC-12345678          valid
//   ACC-, ACC-123456789, acc-12, ACC-12a, " ACC-1"   invalid
//   null                         VALID (the convention: null is @NotBlank's
//                                job, so each constraint checks one thing)
public class AccountNumberValidator implements ConstraintValidator<AccountNumber, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return true; // TODO task 4
  }
}
