package ledgerly.repo;

import java.util.List;

/**
 * What the bank stores about a customer. Finished; NOT one of your tasks.
 * It holds secrets (pin, internalNotes): it must never be sent to a client.
 */
public record Customer(long id, String name, String pan, String pin, String internalNotes, List<Long> transactions) {}
