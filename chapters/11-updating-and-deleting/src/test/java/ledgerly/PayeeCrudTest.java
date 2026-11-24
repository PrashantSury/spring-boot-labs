package ledgerly;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Task 5: YOUR OWN MockMvc tests for create, read, update and delete.
 *
 * Write at least 4 @Test methods, each with a @DisplayName:
 *   - POST then GET: 201, then 200 with the same name
 *   - PUT: nickname becomes null when it is not sent, version goes up
 *   - PATCH: only the sent field changes
 *   - DELETE twice: 204 and 204, then GET is 404
 * Use mvc.perform(post(...)/get(...)/put(...)/patch(...)/delete(...))
 *     .andExpect(status().isCreated()) ... and jsonPath("$.nickname")...
 * These run with Chapter11CheckTest, so they must pass.
 */
class PayeeCrudTest {

  private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new PayeeController()).build();

  // TODO task 5: your tests here
}
