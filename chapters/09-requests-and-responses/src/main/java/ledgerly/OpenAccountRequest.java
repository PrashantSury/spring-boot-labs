package ledgerly;


/** The JSON body of POST /accounts: {"holder":"Asha Rao","openingBalance":5000}. */
// Task 1 (part 2): old mobile apps also send "nickname" and "referral".
// Those must be ignored, not rejected. Spring's JSON converter already
// ignores unknown fields by default, but a team may switch that on globally
// (FAIL_ON_UNKNOWN_PROPERTIES). Make THIS record safe either way:
// @JsonIgnoreProperties(ignoreUnknown = true).
public record OpenAccountRequest(String holder, long openingBalance) {}
