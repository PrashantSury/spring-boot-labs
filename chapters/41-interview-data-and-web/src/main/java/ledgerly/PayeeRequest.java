package ledgerly;


/**
 * Body of POST /payees: {"name":"Kiran Rao","ifsc":"HDFC0001234","accountNumber":"50100012345678"}
 * Task 3 (part 1):
 *   name:          @NotBlank
 *   ifsc:          @Pattern(regexp = "[A-Z]{4}0[A-Z0-9]{6}", message = "must be a valid IFSC code")
 *   accountNumber: @Pattern(regexp = "\\d{9,18}", message = "must be 9 to 18 digits")
 * (and @Valid in PayeeController)
 */
public record PayeeRequest(
    String name,
    String ifsc,
    String accountNumber) {}
