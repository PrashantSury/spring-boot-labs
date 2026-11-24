package ledgerly.app;

import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Reads the same report that --debug prints ("CONDITIONS EVALUATION REPORT").
 */
// Task 5: read the conditions report
//
// ConditionEvaluationReport.get(ctx.getBeanFactory()) gives the report.
// report.getConditionAndOutcomesBySource() is a Map from a "source" to its
// outcomes. A source is a class name, or class#method for a @Bean method:
//     "ledgerly.autoconfigure.LedgerlyAuditAutoConfiguration"
//     "ledgerly.autoconfigure.LedgerlyAuditAutoConfiguration#auditSink"
//
// outcome(ctx, source):
//   - "matched"        when the source is in the map and isFullMatch()
//   - "did not match"  when it is in the map and not a full match
//   - "not evaluated"  when it is not in the map
public final class ConditionsReader {

  private ConditionsReader() {}

  public static String outcome(ConfigurableApplicationContext ctx, String source) {
    return null; // TODO task 5
  }
}
