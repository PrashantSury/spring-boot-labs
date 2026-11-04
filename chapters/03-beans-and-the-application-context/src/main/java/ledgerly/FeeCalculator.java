package ledgerly;

/**
 * Works out the fee for a transfer: 1% of the amount, at least 10.
 * The logic is finished. Your job in task 1 is to make Spring own it.
 */
// Task 1: make FeeCalculator a bean
//
// Today this is a plain Java class. Spring does not know it exists,
// so the application context has no bean of this type.
// Mark the class so that component scanning finds it and puts one
// instance in the context. Its default bean name will be
// "feeCalculator" (the class name with a lower-case first letter).
public class FeeCalculator {

  public int feeFor(int amount) {
    return Math.max(amount / 100, 10);
  }
}
