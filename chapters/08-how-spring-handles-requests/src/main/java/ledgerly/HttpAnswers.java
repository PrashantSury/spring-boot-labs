package ledgerly;


/** Which status Spring MVC answers for which exception, before your code runs. */
// Task 5: the built-in answers
//
// statusFor(e) returns the HTTP status Spring's DefaultHandlerExceptionResolver
// gives each of these exceptions:
//   HttpRequestMethodNotSupportedException   path exists, wrong method
//   HttpMediaTypeNotSupportedException       body Content-Type not accepted
//   HttpMediaTypeNotAcceptableException      cannot produce what Accept asks
//   MissingServletRequestParameterException  a required ?param is missing
//   TypeMismatchException (incl. MethodArgumentTypeMismatchException)  ?amount=abc
//   NoHandlerFoundException, NoResourceFoundException  nothing mapped there
// Anything else: 500. The checks compare your table with real requests.
public final class HttpAnswers {

  private HttpAnswers() {}

  public static int statusFor(Exception e) {
    return 0; // TODO task 5
  }
}
