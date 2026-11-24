# Chapter 7: The bean lifecycle

By the end of this chapter you can say every step of a bean's life in order, run code when a bean is ready and when it is shut down, do the same for a class you cannot annotate, and explain how a `BeanPostProcessor` gives you a proxy, and why `this.method()` skips it.

## Why you need this

Ledgerly's `RateCache` loads today's exchange rates in its constructor. In production it crashes with a `NullPointerException`: the rate source is injected **after** the constructor runs. A week later the support team finds 300 customer sessions still "open" after a deploy, because nothing closed them on shutdown.

Both bugs are lifecycle bugs. Spring calls your code at fixed moments; you need to know which ones.

## What you will learn

- **The steps:** constructor → injection → `@PostConstruct` → `afterPropertiesSet` → `initMethod` → *in use* → `@PreDestroy` → `destroy` → `destroyMethod`.
- **Three ways to hook in:** annotations (`@PostConstruct`/`@PreDestroy`), interfaces (`InitializingBean`/`DisposableBean`), and `@Bean(initMethod, destroyMethod)` for classes you don't own.
- **`BeanPostProcessor`:** sees every bean after initialisation and may hand back a proxy instead.
- **Self-invocation:** a call to `this.line()` goes to the real object, never through the proxy.
- **Prototypes:** Spring creates them but never destroys them.

## Real output

`LifecycleTour` starts the context, uses some beans and closes it. Real output from a finished lab:

```
USD rate                  83.25
printer class             StatementPrinter$$SpringCGLIB$$0
printer is a proxy?       true
after statement(): statement=1 line=0
after line():      statement=1 line=1
-- closing the context --
Lifecycle log:
  rates loaded: 3
  gateway connected
  probe: constructor
  probe: @PostConstruct
  probe: afterPropertiesSet
  probe: initMethod
  probe: @PreDestroy
  probe: destroy
  probe: destroyMethod
  gateway disconnected
  sessions closed: 2
```

Two things to notice. The `probe` lines are the full lifecycle, in order. And `statement()` calls `line()` twice inside itself, yet the counter says `line=0`: those inner calls never went through the proxy.

On the stub the same tour prints `USD rate null`, `printer is a proxy? false`, and the log has only `probe: constructor`.

## How a proxy appears

```java
public Object postProcessAfterInitialization(Object bean, String beanName) {
  if (!bean.getClass().isAnnotationPresent(Metered.class)) return bean;   // most beans: unchanged
  ProxyFactory factory = new ProxyFactory(bean);
  factory.setProxyTargetClass(true);                                       // a subclass proxy (CGLIB)
  factory.addAdvice((MethodInterceptor) inv -> { counter.record(inv.getMethod().getName()); return inv.proceed(); });
  return factory.getProxy();                                               // THIS is what gets injected
}
```

That is the same trick Spring uses for `@Transactional`, `@Async` and `@Cacheable` (chapters 21, 32, 34).

## Try it

```bash
mvn -q -pl chapters/07-the-bean-lifecycle compile exec:java "-Dexec.mainClass=ledgerly.LifecycleTour"
mvn -pl chapters/07-the-bean-lifecycle test -Popen
```

## Your turn

You edit `RateCache.java`, `SessionRegistry.java`, `GatewayConfig.java`, `MeteringPostProcessor.java`, `LifecycleProbe.java` and `interview-answer.md`. Each has a `// Task N` comment.

1. **`@PostConstruct`.** `RateCache.load()` runs once, after injection.
2. **`@PreDestroy`.** `SessionRegistry.closeAll()` runs on shutdown.
3. **`initMethod`/`destroyMethod`.** The vendor `PaymentGatewayClient` connects and disconnects through its `@Bean`.
4. **`BeanPostProcessor`.** Wrap `@Metered` beans in a counting proxy; leave the others alone.
5. **The full order.** `LifecycleProbe` uses all six hooks; the check reads the order.
6. **(Interview)** Write your answer in `interview-answer.md`.

**Traps**

- Don't call `load()` from the constructor: `source` is still `null` there.
- Task 2: `closeAll()` must not run early. Spring calls `@PreDestroy` only on `close()`.
- Task 4: return the proxy from **after** initialisation, and return `bean` unchanged for everything else.
- Task 4: the "line=0" check is correct. Don't "fix" it by counting inside `StatementPrinter`.
- Task 5: predict the order before you run the check.

**Interview question (task 6).** Walk through a singleton bean's life. Where do `@PostConstruct` and `@PreDestroy` fit, and why not set up in the constructor? What is a `BeanPostProcessor` and how does it make a proxy; why does self-invocation skip it? Does Spring destroy prototype beans? The questions are repeated at the top of `interview-answer.md`.
