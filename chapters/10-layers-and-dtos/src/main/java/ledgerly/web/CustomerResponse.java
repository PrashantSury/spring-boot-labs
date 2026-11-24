package ledgerly.web;

/**
 * What a client may see about a customer.
 */
// Task 1 (part 1): the response DTO
//
// A client may see exactly three things: id, name and maskedPan.
// Add the three components: long id, String name, String maskedPan.
// Nothing else: no pin, no internalNotes, no raw pan.
public record CustomerResponse() {}
