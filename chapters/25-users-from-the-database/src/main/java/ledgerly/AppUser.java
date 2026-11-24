package ledgerly;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A login. passwordHash holds a BCrypt hash, never the password. Finished; NOT one of your tasks. */
@Entity
@Table(name = "app_user")
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 40)
  private String username;

  @Column(nullable = false, length = 100)
  private String passwordHash;

  @Column(nullable = false, length = 20)
  private String role;

  @Column(nullable = false)
  private boolean enabled = true;

  protected AppUser() {}

  public AppUser(String username, String passwordHash, String role, boolean enabled) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.enabled = enabled;
  }

  public Long getId() {
    return id;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public String getRole() {
    return role;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
