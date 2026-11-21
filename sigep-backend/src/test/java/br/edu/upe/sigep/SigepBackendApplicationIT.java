package br.edu.upe.sigep;

import br.edu.upe.sigep.integration.IntegracaoUsuarios;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SigepBackendApplicationIT extends IntegracaoUsuarios {

    @Autowired
    private Flyway flyway;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void contextLoads() {
        assertTrue(entityManagerFactory.isOpen());
        assertEquals("sigep_integration", jdbc.queryForObject("select current_database()", String.class));
        assertSame(jdbc.getDataSource(), flyway.getConfiguration().getDataSource());
        var versoesAplicadas = Arrays.stream(flyway.info().applied())
                .filter(migracao -> migracao.getVersion() != null)
                .map(migracao -> migracao.getVersion().getVersion())
                .toList();
        assertTrue(versoesAplicadas.containsAll(Set.of("1", "2", "3")));
        flyway.validate();
    }
}
