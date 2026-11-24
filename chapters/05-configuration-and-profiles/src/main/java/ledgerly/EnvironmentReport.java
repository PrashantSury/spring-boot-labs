package ledgerly;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/** Answers "where did this value come from?". */
// Task 5: property precedence
//
// The Environment is a list of property sources, highest priority first:
// command-line arguments, Java system properties, OS environment variables,
// application-{profile}.properties, application.properties, ...
// The FIRST source that has a key wins.
//
// whereFrom(key) returns the NAME of the property source that supplies the
// key: walk env.getPropertySources() in order and return the first
// source.getName() whose source containsProperty(key). If none has it,
// return "not set".
// Skip the source named "configurationProperties": Spring Boot puts it first
// as a view over ALL the others, so it would always win.
@Component
public class EnvironmentReport {

  private final ConfigurableEnvironment env;

  public EnvironmentReport(ConfigurableEnvironment env) {
    this.env = env;
  }

  public String whereFrom(String key) {
    return null; // TODO task 5
  }

  public String value(String key) {
    return env.getProperty(key, "not set");
  }
}
