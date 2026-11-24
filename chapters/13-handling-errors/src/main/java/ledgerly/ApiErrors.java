package ledgerly;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * One error shape for the whole API: RFC 9457 problem details.
 *
 * <p>Task 3 (part 1): extend ResponseEntityExceptionHandler so Spring's own errors
 * (400 bad JSON, 405 wrong method, 415 wrong content type) keep their status and
 * also come out as ProblemDetail.
 */
@RestControllerAdvice
public class ApiErrors {

  // Task 1: unknown account -> 404
  //   type   https://ledgerly.dev/errors/account-not-found
  //   title  Account not found
  //   detail the exception message ("No account ACC-9")

  // Task 2: insufficient funds -> 422 (the request is valid JSON, the business says no)
  //   title  Insufficient funds
  //   detail the exception message
  //   extra property "shortBy" = e.shortBy()   (pd.setProperty)

  // Task 3 (part 2): validation errors -> 400 with title "Invalid request"
  //   and a property "errors": a map field -> message, e.g. {"amount":"must be greater than 0"}
  //   Override handleMethodArgumentNotValid from the base class (do NOT add a second
  //   @ExceptionHandler for MethodArgumentNotValidException: the base class already has one,
  //   and two handlers for the same exception fail at startup).

  // Task 4: the safety net. Any other exception -> 500, title "Internal error",
  //   detail "Something went wrong. Quote this to support." and NOTHING from the exception
  //   (no class name, no message, no stack trace) in the body.
  //   Because the class extends ResponseEntityExceptionHandler, 405 and 415 still win.
}
