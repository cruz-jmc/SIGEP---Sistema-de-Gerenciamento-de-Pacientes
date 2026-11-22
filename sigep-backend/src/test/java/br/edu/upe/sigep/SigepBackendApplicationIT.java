package br.edu.upe.sigep;

import br.edu.upe.sigep.integration.IntegracaoUsuarios;
import jakarta.persistence.EntityManagerFactory;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        assertEquals(List.of("1", "2", "4"), versoesAplicadas);
        flyway.validate();
    }

    @Test
    void migraBancoDaDevelopPreservandoUsuariosEChavesEstrangeiras() {
        String schema = "upgrade_" + UUID.randomUUID().toString().replace("-", "");
        var configuracao = Flyway.configure().dataSource(jdbc.getDataSource())
                .locations("classpath:db/migration").schemas(schema);
        configuracao.target("2").load().migrate();
        configuracao.load().validate();

        // O nome do schema foi gerado acima, exclusivamente para este teste.
        String tabela = schema + ".usuario";
        String hash = encoder.encode(SENHA);
        jdbc.update("""
                insert into %s (id, nome, email, senha_hash, perfil, ativo, primeiro_acesso, criado_por, criado_em)
                values (13, 'Coordenação', 'COORD@sigep.example', ?, 'COORDENADOR', true, false, null, now()),
                       (14, 'Funcionário', 'Funcionario@sigep.example', ?, 'FUNCIONARIO', false, true, 13, now())
                """.formatted(tabela), hash, hash);

        var flywayAtualizado = configuracao.target("4").load();
        assertEquals(1, flywayAtualizado.migrate().migrationsExecuted);
        flywayAtualizado.validate();

        assertEquals(3, jdbc.queryForObject("select count(*) from " + tabela, Integer.class));
        assertEquals(13L, jdbc.queryForObject("select criado_por from " + tabela + " where id = 14", Long.class));
        assertEquals("Funcionario@sigep.example",
                jdbc.queryForObject("select email from " + tabela + " where id = 14", String.class));
        assertEquals(hash, jdbc.queryForObject("select senha_hash from " + tabela + " where id = 14", String.class));
        assertEquals(false, jdbc.queryForObject("select ativo from " + tabela + " where id = 14", Boolean.class));
        assertEquals(true, jdbc.queryForObject("select primeiro_acesso from " + tabela + " where id = 14", Boolean.class));
        assertEquals(0L, jdbc.queryForObject("select revisao_credenciais from " + tabela + " where id = 14", Long.class));
    }

    @ParameterizedTest
    @CsvSource({
            "COORDENADOR, outro@sigep.example, existe mais de um usuário com perfil COORDENADOR",
            "FUNCIONARIO, LEGADO@sigep.example, existem usuários com e-mails duplicados após lower(email)"
    })
    void rejeitaConflitosAntesDeAlterarDadosOuEsquema(String perfil, String email, String mensagem) {
        String schema = "conflito_" + UUID.randomUUID().toString().replace("-", "");
        var configuracao = Flyway.configure().dataSource(jdbc.getDataSource())
                .locations("classpath:db/migration").schemas(schema);
        configuracao.target("2").load().migrate();

        String tabela = schema + ".usuario";
        jdbc.update("""
                insert into %s (nome, email, senha_hash, perfil, ativo, primeiro_acesso, criado_em)
                values ('Coordenação', 'legado@sigep.example', ?, 'COORDENADOR', true, false, now()),
                       ('Outro usuário', ?, ?, ?, false, true, now())
                """.formatted(tabela), encoder.encode(SENHA), email, encoder.encode(SENHA), perfil);
        var falha = assertThrows(FlywayException.class, () -> configuracao.target("4").load().migrate());
        assertTrue(falha.getMessage().contains(mensagem));
        assertEquals(3, jdbc.queryForObject("select count(*) from " + tabela, Integer.class));
        assertEquals(0, jdbc.queryForObject("""
                select count(*) from information_schema.columns
                where table_schema = ? and table_name = 'usuario' and column_name = 'revisao_credenciais'
                """, Integer.class, schema));
    }
}
