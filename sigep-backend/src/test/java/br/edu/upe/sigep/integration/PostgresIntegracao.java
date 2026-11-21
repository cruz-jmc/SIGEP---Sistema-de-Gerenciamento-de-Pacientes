package br.edu.upe.sigep.integration;

import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresIntegracao {

    private static final String URL = "jdbc:postgresql://127.0.0.1:55433/sigep_integration";
    private static final String USUARIO = "sigep_integration";

    @Bean
    DataSource dataSource(Environment ambiente) throws SQLException {
        if (!List.of(ambiente.getActiveProfiles()).equals(List.of("integration"))) {
            throw new IllegalStateException("A suíte exige exclusivamente o perfil integration.");
        }

        String url = ambiente.getRequiredProperty("SIGEP_IT_DB_URL");
        String usuario = ambiente.getRequiredProperty("SIGEP_IT_DB_USER");
        String senha = ambiente.getRequiredProperty("SIGEP_IT_DB_PASSWORD");
        if (!URL.equals(url) || !USUARIO.equals(usuario) || senha.isBlank()
                || !url.equals(ambiente.getRequiredProperty("spring.datasource.url"))
                || !usuario.equals(ambiente.getRequiredProperty("spring.datasource.username"))
                || !senha.equals(ambiente.getRequiredProperty("spring.datasource.password"))) {
            throw new IllegalStateException("Destino ou credenciais de integração inválidos.");
        }

        for (String chave : List.of("spring.flyway.url", "spring.flyway.user",
                "spring.flyway.password", "spring.datasource.jndi-name")) {
            if (ambiente.containsProperty(chave)) {
                throw new IllegalStateException("Configuração incompatível com o DataSource exclusivo: " + chave);
            }
        }
        if (!ambiente.getRequiredProperty("spring.flyway.enabled", Boolean.class)
                || !"validate".equals(ambiente.getRequiredProperty("spring.jpa.hibernate.ddl-auto"))) {
            throw new IllegalStateException("A integração exige Flyway ativo e Hibernate em modo validate.");
        }

        DriverManagerDataSource banco = new DriverManagerDataSource(url, usuario, senha);
        try (var conexao = banco.getConnection();
             var consulta = conexao.createStatement();
             var resultado = consulta.executeQuery("""
                     select current_database(), current_user, current_setting('cluster_name')
                     """)) {
            if (!resultado.next() || !"sigep_integration".equals(resultado.getString(1))
                    || !USUARIO.equals(resultado.getString(2))
                    || !"sigep-integration".equals(resultado.getString(3))) {
                throw new IllegalStateException("O PostgreSQL não pertence ao ambiente descartável de integração.");
            }
        }
        return banco;
    }
}
