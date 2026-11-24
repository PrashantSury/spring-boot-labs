package ledgerly;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/** Body of POST /transfers. Finished (chapter 12). */
public record TransferRequest(@NotBlank String from, @NotBlank String to, @Positive long amount) {}
