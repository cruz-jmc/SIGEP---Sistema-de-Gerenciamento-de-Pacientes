package br.edu.upe.sigep.config;

import br.edu.upe.sigep.service.UsuarioDetailsService;
import br.edu.upe.sigep.repository.UsuarioRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;

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
    public CookieCsrfTokenRepository csrfTokenRepository() {
        return CookieCsrfTokenRepository.withHttpOnlyFalse();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, UsuarioRepository usuarios,
            CookieCsrfTokenRepository csrfTokenRepository) throws Exception {

        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("COORDENADOR")
                        .requestMatchers(HttpMethod.GET,
                                "/api/auditorias/usuario/{usuarioId:[+-]?[0-9]+}",
                                "/api/auditorias/tabela/{tabelaNome}",
                                "/api/auditorias/registro/{registroId:[+-]?[0-9]+}",
                                "/api/auditorias/cronologica").hasRole("COORDENADOR")
                        .requestMatchers(HttpMethod.POST,
                                "/api/auditorias").hasRole("COORDENADOR")
                        .requestMatchers(HttpMethod.GET,
                                "/api/pacientes",
                                "/api/pacientes/{id:[+-]?[0-9]+}",
                                "/api/pacientes/ativos",
                                "/api/profissionais",
                                "/api/profissionais/{id:[+-]?[0-9]+}",
                                "/api/profissionais/ativos",
                                "/api/especialidades",
                                "/api/especialidades/{id:[+-]?[0-9]+}",
                                "/api/especialidades/ativas",
                                "/api/solicitacoes",
                                "/api/solicitacoes/pendentes",
                                "/api/solicitacoes/{id:[+-]?[0-9]+}",
                                "/api/solicitacoes/paciente/{pacienteId:[+-]?[0-9]+}",
                                "/api/solicitacoes/status/{status}",
                                "/api/planos-tratamento",
                                "/api/planos-tratamento/{id:[+-]?[0-9]+}",
                                "/api/planos-tratamento/solicitacao/{solicitacaoId:[+-]?[0-9]+}",
                                "/api/planos-tratamento/paciente/{pacienteId:[+-]?[0-9]+}/status/{status}",
                                "/api/agendamentos-sessoes",
                                "/api/agendamentos-sessoes/{id:[+-]?[0-9]+}",
                                "/api/agendamentos-sessoes/plano/{planoId:[+-]?[0-9]+}",
                                "/api/agendamentos-sessoes/plano/{planoId:[+-]?[0-9]+}/presencas/{status}",
                                "/api/escalas",
                                "/api/escalas/profissional/{profissionalId:[+-]?[0-9]+}/ativas",
                                "/api/escalas/{escalaId:[+-]?[0-9]+}",
                                "/api/cotas-mensais/disponibilidade").hasAnyRole("COORDENADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST,
                                "/api/pacientes",
                                "/api/profissionais",
                                "/api/especialidades",
                                "/api/solicitacoes",
                                "/api/agendamentos-sessoes",
                                "/api/escalas").hasAnyRole("COORDENADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.PUT,
                                "/api/pacientes/{id:[+-]?[0-9]+}",
                                "/api/profissionais/{id:[+-]?[0-9]+}",
                                "/api/especialidades/{id:[+-]?[0-9]+}").hasAnyRole("COORDENADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/pacientes/{id:[+-]?[0-9]+}/reativar",
                                "/api/profissionais/{id:[+-]?[0-9]+}/ativo",
                                "/api/especialidades/{id:[+-]?[0-9]+}/ativo",
                                "/api/solicitacoes/{id:[+-]?[0-9]+}/aprovar",
                                "/api/solicitacoes/{id:[+-]?[0-9]+}/rejeitar",
                                "/api/planos-tratamento/{id:[+-]?[0-9]+}/concluir",
                                "/api/planos-tratamento/{id:[+-]?[0-9]+}/cancelar",
                                "/api/agendamentos-sessoes/{id:[+-]?[0-9]+}/presenca",
                                "/api/agendamentos-sessoes/{id:[+-]?[0-9]+}/cancelar",
                                "/api/cotas-mensais/decrementar").hasAnyRole("COORDENADOR", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.DELETE,
                                "/api/pacientes/{id:[+-]?[0-9]+}",
                                "/api/agendamentos-sessoes/{id:[+-]?[0-9]+}").hasAnyRole("COORDENADOR", "FUNCIONARIO")
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
                .csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository))
                .addFilterBefore(new ValidadeSessaoFilter(usuarios), CsrfFilter.class)
                .build();
    }
}
