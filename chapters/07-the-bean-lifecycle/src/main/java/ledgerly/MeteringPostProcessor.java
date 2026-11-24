package ledgerly;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/** Wraps every @Metered bean so that its method calls are counted. */
// Task 4: a BeanPostProcessor that returns a proxy
//
// Spring calls postProcessAfterInitialization for EVERY bean, after its init
// callbacks. Whatever you return is what gets injected everywhere.
//
// If the bean's class is annotated @Metered:
//   - build a proxy with ProxyFactory: new ProxyFactory(bean), setProxyTargetClass(true)
//   - addAdvice a MethodInterceptor that calls counter().record(method name)
//     and then invocation.proceed()
//   - return factory.getProxy()
// Otherwise return the bean unchanged.
@Component
public class MeteringPostProcessor implements BeanPostProcessor {

  // An ObjectProvider, because a post-processor is created before normal beans.
  private final ObjectProvider<CallCounter> counter;

  public MeteringPostProcessor(ObjectProvider<CallCounter> counter) {
    this.counter = counter;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    return bean; // TODO task 4
  }
}
