package ledgerly;

import java.util.List;

/** What the statement endpoint returns. Finished. Page numbers start at 0. */
public record PageResponse(List<TxnView> content, int page, int size, long totalElements, int totalPages) {}
