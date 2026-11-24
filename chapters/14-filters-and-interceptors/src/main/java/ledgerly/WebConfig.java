package ledgerly;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Wires the filters and interceptors. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final String opsKey;

  public WebConfig(@Value("${ledgerly.ops-key}") String opsKey) {
    this.opsKey = opsKey;
  }

  /** Finished: the access log, order 2. */
  @Bean
  public FilterRegistrationBean<AccessLogFilter> accessLog() {
    FilterRegistrationBean<AccessLogFilter> reg = new FilterRegistrationBean<>(new AccessLogFilter());
    reg.addUrlPatterns("/*");
    reg.setOrder(2);
    return reg;
  }

  // Task 4: register CorrelationIdFilter for "/*" so it runs BEFORE (outside) the access log.
  //   Lower order = runs first. Copy the bean above and pick the order.

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // Task 2: ApiKeyInterceptor(opsKey) for "/ops/**" only.
    // Task 3: HandlerNameInterceptor for every path.
  }
}
