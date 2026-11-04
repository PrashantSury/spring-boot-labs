package ledgerly;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A finished demo controller. It is NOT part of your tasks.
 * It is here so you can try the chapter's examples with curl once
 * task 6 is done. Read it, don't change it.
 */
@RestController
public class BranchController {

  record Branch(String code, String city) {}

  @GetMapping("/version")
  String version() {
    return "Ledgerly 0.1";
  }

  @GetMapping("/branches")
  List<Branch> branches() {
    return List.of(new Branch("BLR01", "Bengaluru"), new Branch("DEL01", "New Delhi"));
  }

  @GetMapping("/branches/{code}")
  String branch(@PathVariable String code) {
    return "Branch " + code.toUpperCase();
  }

  @GetMapping("/double")
  int twice(@RequestParam int n) {
    return n * 2;
  }

  @GetMapping("/boom")
  String boom() {
    throw new UnsupportedOperationException("TODO: this method is not written yet");
  }
}
