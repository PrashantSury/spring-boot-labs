package ledgerly;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * A finished demo. NOT one of your tasks. It shows @Bean for a class you
 * cannot annotate (NumberFormat is from the JDK). Read it, don't change it.
 */
@Configuration
public class DemoConfig {

  @Bean
  NumberFormat rupeeFormat() {
    return NumberFormat.getCurrencyInstance(Locale.of("en", "IN"));
  }

  @Bean("branchCodes")
  List<String> codes() {
    return List.of("BLR01", "DEL01", "MUM01");
  }
}
