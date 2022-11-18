package org.home.productivity.traverse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security config
 * <p>
 * See:
 * https://spring.io/blog/2022/02/21/spring-security-without-the-websecurityconfigureradapter
 */
@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.warn(
                "H2 database console is unsecured for development purposes. THIS CONFIGURATION SHOULD NOT MAKE IT INTO A QA OR PROD ENVIRONMENT!!!");
        // @formatter:off
        // See: https://www.baeldung.com/spring-cors
        // H2 console required frameOptions be disabled, see: https://springframework.guru/using-the-h2-database-console-in-spring-boot-with-spring-security/
        http.cors()
            .and()
            .authorizeHttpRequests()
            .requestMatchers("/", "/status", "/v3/api-docs/**").permitAll()
            .requestMatchers("/h2-console/**", "/swagger-ui/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/actuator/**", "/admin/**").hasAnyRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .httpBasic()
            .and()
            .csrf().disable()
            .headers().frameOptions().disable()
            .and()
            .exceptionHandling().accessDeniedPage("/accessDenied.html");
        return http.build();
        // @formatter:on
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        log.warn(
                "NoOp password encoder is being used with in memory authentication. THIS CONFIGURATION SHOULD NOT MAKE IT INTO A QA OR PROD ENVIRONMENT!!!");
        // @formatter:off
            var user = User.builder()
                            .username("user")
                            .password("{noop}user")
                            .roles("USER")
                            .build();
            var admin = User.builder()
                            .username("admin")
                            .password("{noop}admin")
                            .roles("USER", "ADMIN")
                            .build();
        // @formatter:on
        return new InMemoryUserDetailsManager(user, admin);
    }

}
