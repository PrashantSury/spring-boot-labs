package ledgerly;

import java.util.List;

/** What the mobile feed returns. Finished. No totals: a Slice does not count. */
public record SliceResponse(List<TxnView> content, int page, boolean hasNext) {}
