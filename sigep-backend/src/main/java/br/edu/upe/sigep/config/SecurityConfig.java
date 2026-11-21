package br.edu.upe.sigep.config;

import br.edu.upe.sigep.service.UsuarioDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationProvider authenticationProvider(
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarioDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth

                        // Disponível antes do login
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/csrf"
                        ).permitAll()

                        // Ativação de conta mediante convite válido
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/ativacao"
                        ).permitAll()

                        // Consulta da sessão autenticada
                        .requestMatchers(
                                "/api/auth/me"
                        ).authenticated()

                        // Cadastro e consulta de usuários:
                        // somente coordenador
                        .requestMatchers(
                                "/api/usuarios",
                                "/api/usuarios/**"
                        ).hasRole("COORDENADOR")

                        // Demais endpoints bloqueados por padrão
                        .anyRequest().denyAll()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("email")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                CookieCsrfTokenRepository
                                        .withHttpOnlyFalse()
                        )
                )
                .build();
    }
}