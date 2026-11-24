package ledgerly;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** One problem+json shape for every error (chapter 13). Finished. */
@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {

  @ExceptionHandler({NoSuchAccount.class, NoSuchTransfer.class})
  public ProblemDetail notFound(RuntimeException e) {
    return problem(HttpStatus.NOT_FOUND, "Not found", e.getMessage());
  }

  @ExceptionHandler({InsufficientFunds.class, AccountFrozen.class})
  public ProblemDetail refused(RuntimeException e) {
    return problem(HttpStatus.UNPROCESSABLE_CONTENT, "Transfer refused", e.getMessage());
  }

  @ExceptionHandler(IdempotencyConflict.class)
  public ProblemDetail conflict(IdempotencyConflict e) {
    return problem(HttpStatus.CONFLICT, "Idempotency conflict", e.getMessage());
  }

  /** A stub method you haven't written yet: 501 with the task in the detail. */
  @ExceptionHandler(UnsupportedOperationException.class)
  public ProblemDetail notYet(UnsupportedOperationException e) {
    return problem(HttpStatus.NOT_IMPLEMENTED, "Not implemented yet", e.getMessage());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
    ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Invalid request", "check the errors");
    pd.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(pd);
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
    pd.setTitle(title);
    return pd;
  }
}
