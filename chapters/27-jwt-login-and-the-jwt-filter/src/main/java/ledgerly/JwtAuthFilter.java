package ledgerly;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Task 2: turns "Authorization: Bearer <jwt>" into a logged-in user for THIS request.
 * <ul>
 *   <li>No Authorization header, or not starting with "Bearer ": just continue the chain
 *       (the request stays anonymous; the rules decide later).
 *   <li>Bearer token: codec.verify(token). On JwtException answer 401 YOURSELF with
 *       application/json {"error":"invalid token: <message>"} and stop (no chain.doFilter):
 *       an exception thrown here would never reach a @RestControllerAdvice.
 *   <li>Valid: load the UserDetails for claims "sub" (roles come from the user store, not the token),
 *       build UsernamePasswordAuthenticationToken.authenticated(user, null, user.getAuthorities()),
 *       put it in SecurityContextHolder.getContext().setAuthentication(...), continue the chain.
 * </ul>
 * Created in SecurityConfig, NOT a @Component (Boot would also register it as a plain servlet filter).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtCodec codec;
  private final UserDetailsService users;

  public JwtAuthFilter(JwtCodec codec, UserDetailsService users) {
    this.codec = codec;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    chain.doFilter(req, res);
  }
}
