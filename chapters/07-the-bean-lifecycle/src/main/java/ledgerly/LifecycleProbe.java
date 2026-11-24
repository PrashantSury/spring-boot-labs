package ledgerly;


/** A bean that reports every step of its life. Every method body is finished. */
// Task 5 (part 1): hook into every lifecycle step
//
//   - postConstruct()    -> @PostConstruct
//   - preDestroy()       -> @PreDestroy
//   - afterPropertiesSet() and destroy() -> implement InitializingBean and DisposableBean
//     (the methods already exist: add the interfaces and @Override)
//   - customInit() / customDestroy() -> named in GatewayConfig (part 2)
//
// The check then reads the order Spring called them in. Predict it first!
public class LifecycleProbe {

  public LifecycleProbe() {
    LifecycleLog.add("probe: constructor");
  }

  void postConstruct() {
    LifecycleLog.add("probe: @PostConstruct");
  }

  public void afterPropertiesSet() {
    LifecycleLog.add("probe: afterPropertiesSet");
  }

  void customInit() {
    LifecycleLog.add("probe: initMethod");
  }

  void preDestroy() {
    LifecycleLog.add("probe: @PreDestroy");
  }

  public void destroy() {
    LifecycleLog.add("probe: destroy");
  }

  void customDestroy() {
    LifecycleLog.add("probe: destroyMethod");
  }
}
