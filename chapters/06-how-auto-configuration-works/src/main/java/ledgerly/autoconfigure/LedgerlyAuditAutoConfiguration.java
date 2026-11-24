package ledgerly.autoconfigure;

import ledgerly.audit.AuditSink;
import ledgerly.audit.ConsoleAuditSink;
import ledgerly.audit.SiemForwarder;
import org.springframework.context.annotation.Bean;

/**
 * Ledgerly's tiny "audit starter". Every Ledgerly app gets an AuditSink
 * without writing one, the same way every Boot app gets a DataSource.
 * The bean bodies are finished. Your tasks are the annotations.
 */
// Task 1: make this an auto-configuration
//   - annotate the class with @AutoConfiguration
//   - list it in src/main/resources/META-INF/spring/
//       org.springframework.boot.autoconfigure.AutoConfiguration.imports
//   Do NOT add @ComponentScan or @Import in LedgerlyApplication: the imports
//   file is how a starter is found.
//
// Task 3: the whole starter can be switched off
//   Only apply this class when ledgerly.audit.enabled is "true", and treat a
//   MISSING key as true (on by default). Use @ConditionalOnProperty.
public class LedgerlyAuditAutoConfiguration {

  // Task 2: back off
  //   If the application already defines ANY AuditSink bean, this default
  //   must not be created. Use @ConditionalOnMissingBean.
  @Bean
  AuditSink auditSink() {
    return new ConsoleAuditSink();
  }

  // Task 4: only when the vendor library is there
  //   Create this bean only if the class ledgerly.siem.SiemClient is on the
  //   classpath. Use @ConditionalOnClass with name = "..." (a String, so this
  //   class still loads when the library is missing).
  @Bean
  SiemForwarder siemForwarder() {
    return new SiemForwarder();
  }
}
