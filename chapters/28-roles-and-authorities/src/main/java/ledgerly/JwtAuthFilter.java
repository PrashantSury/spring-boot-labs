package ledgerly;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * The chapter 27 filter, now reading the authorities FROM THE TOKEN (no user lookup).
 * Finished, except that it uses TokenService.authorities (task 2).
 */
public class JwtAuthFilter extends OncePerRequestFilter {

  private final TokenService tokens;

  public JwtAuthFilter(TokenService tokens) {
    this.tokens = tokens;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    String header = req.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      Map<String, Object> claims;
      try {
        claims = tokens.read(header.substring(7));
      } catch (JwtException e) {
        res.setStatus(401);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"invalid token: " + e.getMessage() + "\"}");
        return;
      }
      var authorities = TokenService.authorities(claims).stream().map(SimpleGrantedAuthority::new).toList();
      SecurityContextHolder.getContext().setAuthentication(
          UsernamePasswordAuthenticationToken.authenticated(claims.get("sub"), null, authorities));
    }
    chain.doFilter(req, res);
  }
}
