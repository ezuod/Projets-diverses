package pdl.backend;

import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class RedirectConfiguration {

  @Bean
  public FilterRegistrationBean<OncePerRequestFilter> spaRedirectFiler() {
    FilterRegistrationBean<OncePerRequestFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(createRedirectFilter());
    registration.addUrlPatterns("/*");
    registration.setName("frontendRedirectFiler");
    registration.setOrder(1);
    return registration;
  }

  private OncePerRequestFilter createRedirectFilter() {
    return new OncePerRequestFilter() {
      // Forwards all routes except '/index.html', '/200.html', '/favicon.ico', '/sw.js' '/images/',
      // '/images/**'
      private final String REGEX =
          "(?!/actuator|/images|/_nuxt|/static|/index\\.html|/200\\.html|/favicon\\.ico|/sw\\.js).*$";
      private Pattern pattern = Pattern.compile(REGEX);

      @Override
      protected void doFilterInternal(
          HttpServletRequest req, HttpServletResponse res, FilterChain chain)
          throws ServletException, IOException {
        if (pattern.matcher(req.getRequestURI()).matches() && !req.getRequestURI().equals("/")) {
          // Delegate/Forward to `/` if `pattern` matches and it is not `/`
          // Required for the history mode in frontend routing
          RequestDispatcher rd = req.getRequestDispatcher("/");
          rd.forward(req, res);
        } else {
          chain.doFilter(req, res);
        }
      }
    };
  }
}
