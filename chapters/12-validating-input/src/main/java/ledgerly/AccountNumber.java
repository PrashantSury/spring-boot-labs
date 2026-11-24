package ledgerly;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** A Ledgerly account number: "ACC-" and 1 to 8 digits. Checked by AccountNumberValidator. */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccountNumberValidator.class)
public @interface AccountNumber {
  String message() default "must look like ACC-12345";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
