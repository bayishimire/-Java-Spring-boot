package com.hospital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/uploads/**", "/error").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/receptionist/**").hasAnyRole("RECEPTIONIST", "ADMIN")
                .requestMatchers("/nurse/**").hasAnyRole("NURSE", "ADMIN")
                .requestMatchers("/lab/**").hasAnyRole("LAB_TECHNICIAN", "ADMIN")
                .requestMatchers("/pharmacist/**").hasAnyRole("PHARMACIST", "ADMIN")
                .requestMatchers("/billing/**").hasAnyRole("BILLING_STAFF", "ADMIN")
                .requestMatchers("/patient/**").hasAnyRole("PATIENT", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(roleSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            String redirectUrl = "/";
            if (role.equals("ROLE_ADMIN")) redirectUrl = "/admin/dashboard";
            else if (role.equals("ROLE_DOCTOR")) redirectUrl = "/doctor/dashboard";
            else if (role.equals("ROLE_RECEPTIONIST")) redirectUrl = "/receptionist/dashboard";
            else if (role.equals("ROLE_NURSE")) redirectUrl = "/nurse/dashboard";
            else if (role.equals("ROLE_LAB_TECHNICIAN")) redirectUrl = "/lab/dashboard";
            else if (role.equals("ROLE_PHARMACIST")) redirectUrl = "/pharmacist/dashboard";
            else if (role.equals("ROLE_BILLING_STAFF")) redirectUrl = "/billing/dashboard";
            else if (role.equals("ROLE_PATIENT")) redirectUrl = "/patient/dashboard";
            
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
