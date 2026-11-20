# Interview answer (task 6)

Question: Spring beans are singletons by default.

1. What does "singleton" mean for a Spring bean, and is it the same as the singleton pattern?
2. Does a singleton bean make your code thread-safe? What do you do when a bean has a field that changes?
3. What is the difference between marking a class with `@Component` and writing a `@Bean` method?

Write 4 to 8 sentences below, in your own words, as you would say it in an interview.

1. Singleton mean there is only one copy per application context and it is different from gang of four design pattern.
2. No. It is not make the code thread safe. we can make the deep copies of the mutable fields.
3. We can annotate @Componet with our own created class and we need some configuration in the JDk's class then we annotate the @Bean to the method in the Configuration class.