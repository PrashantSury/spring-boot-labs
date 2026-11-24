package ledgerly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marks a bean whose public method calls should be counted (task 4). */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Metered {}
