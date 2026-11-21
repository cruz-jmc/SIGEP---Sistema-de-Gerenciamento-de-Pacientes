package br.edu.upe.sigep.integration;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

@ActiveProfiles("integration")
@Import(PostgresIntegracao.class)
public abstract class IntegracaoUsuarios {

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected UsuarioRepository usuarios;

    @Autowired
    protected TransactionTemplate transacoes;

    @MockitoBean
    protected JavaMailSender mailSender;

    @BeforeEach
    void prepararBanco() {
        transacoes.executeWithoutResult(status -> {
            jdbc.update("delete from convite_ativacao");
            jdbc.update("delete from usuario where perfil = 'FUNCIONARIO'");
            jdbc.update("delete from usuario where perfil = 'COORDENADOR'");
        });
    }

    protected Usuario criarCoordenador() {
        Usuario coordenador = new Usuario();
        coordenador.setNome("Coordenação de integração");
        coordenador.setEmail("coordenacao@sigep.example");
        coordenador.setPerfil(PerfilUsuario.COORDENADOR);
        coordenador.setAtivo(true);
        coordenador.setPrimeiroAcesso(false);
        return usuarios.save(coordenador);
    }
}
