package ledgerly;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** problem+json for every error. Spring's own errors (400 validation, 405, 415) come from the base class. */
@RestControllerAdvice
public class ApiErrors extends ResponseEntityExceptionHandler {

  // Task 2 (part 3): NoSuchThing -> 404 problem (title "Not found", detail = message);
  //                  DuplicatePayee -> 409 problem (title "Duplicate payee", detail = message).

  /** Finished: a 400 for an invalid body lists every bad field in "errors". */
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "check the errors");
    pd.setTitle("Invalid request");
    pd.setProperty("errors", errors);
    return ResponseEntity.badRequest().body(pd);
  }
}
