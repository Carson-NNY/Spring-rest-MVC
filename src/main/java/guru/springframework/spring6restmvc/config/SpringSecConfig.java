package guru.springframework.spring6restmvc.config;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.swing.*;

// setup as the oauth2 resource server
@Profile("!test") // tell Spring Boot to ignore this config when testing
@Configuration
public class SpringSecConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

        return http.build();
    }

    // here also adjusted for OpenAPI configuration
    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> {
                    authorize.requestMatchers("/v3/api-docs**", "/swagger-ui/**",  "/swagger-ui.html").permitAll()
                            .anyRequest().authenticated();
                })
                .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> {
                    httpSecurityOAuth2ResourceServerConfigurer.jwt(Customizer.withDefaults());
                });

        return http.build();
    }

}