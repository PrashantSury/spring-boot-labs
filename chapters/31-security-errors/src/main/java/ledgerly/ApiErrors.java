package ledgerly;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** The chapter 13 style advice. It only sees exceptions thrown INSIDE controllers (and what they call). */
@RestControllerAdvice
public class ApiErrors {

  @ExceptionHandler(NoSuchElementException.class)
  ProblemDetail notFound(NoSuchElementException e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
  }

  // Task 4: this catch-all also catches @PreAuthorize denials (AccessDeniedException) thrown from the
  //   service and turns a 403 into a 500. Add a more specific handler that RE-THROWS AccessDeniedException,
  //   so the security filter (and your Task 2 handler) answers 403:
  //   @ExceptionHandler(AccessDeniedException.class) void denied(AccessDeniedException e) { throw e; }
  @ExceptionHandler(Exception.class)
  ProblemDetail any(Exception e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
  }
}
