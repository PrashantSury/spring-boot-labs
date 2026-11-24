package ledgerly.web;

/** The body of POST /customers: {"name":"Ravi Kumar","pan":"BXKPK4321L","pin":"1234"}. Finished. */
public record OpenCustomerRequest(String name, String pan, String pin) {}
