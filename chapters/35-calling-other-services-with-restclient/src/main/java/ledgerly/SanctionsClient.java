package ledgerly;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Talks to the external sanctions partner. */
@Component
public class SanctionsClient {

  static final int MAX_ATTEMPTS = 3;

  private final RestClient client;

  /** Finished: Boot's RestClient.Builder already carries the timeouts from application.properties (task 3). */
  public SanctionsClient(RestClient.Builder builder, @Value("${sanctions.base-url}") String baseUrl) {
    this.client = builder.baseUrl(baseUrl).build();
  }

  /** Finished EXAMPLE: GET /health as plain text. */
  public String ping() {
    return client.get().uri("/health").retrieve().body(String.class);
  }

  // Task 1: GET /screen?name={name} and read the JSON into a ScreenResult:
  //   client.get().uri("/screen?name={name}", payee).retrieve().body(ScreenResult.class)
  //   (the {name} template variable encodes spaces for you; don't glue the name into the string).
  // Task 2: before .body(...), turn error statuses into OUR exceptions with .onStatus(...):
  //   400       -> throw new PayeeRejected(payee)
  //   any 5xx   -> throw new SanctionsUnavailable("partner answered " + status code, true)
  // Task 3: a timeout arrives as ResourceAccessException: catch it and
  //   throw new SanctionsUnavailable("partner timed out", false)   (don't retry a partner that is already slow)
  // Task 4: retry: up to MAX_ATTEMPTS calls in total, but only while the SanctionsUnavailable is retryable().
  //   PayeeRejected is never retried. When the last attempt fails, throw its exception.
  public ScreenResult screen(String payee) {
    throw new UnsupportedOperationException("Task 1: SanctionsClient.screen");
  }


  // Task 4: report a finished transfer: POST /reports with a JSON body.
  //   1. Send the header Idempotency-Key: <transferId>, so the partner can drop a duplicate.
  //   2. A 5xx becomes new SanctionsUnavailable("partner answered " + code, false): NEVER retry this POST yourself.
  public void report(String transferId, String payee) {
    client.post().uri("/reports")
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of("transferId", transferId, "payee", payee))
        .retrieve()
        .toBodilessEntity();
  }
}
