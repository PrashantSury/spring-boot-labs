package ledgerly;

/** "no payee 99", "no customer 7". Finished. */
public class NoSuchThing extends RuntimeException {

  public NoSuchThing(String what, long id) {
    super("no " + what + " " + id);
  }
}
