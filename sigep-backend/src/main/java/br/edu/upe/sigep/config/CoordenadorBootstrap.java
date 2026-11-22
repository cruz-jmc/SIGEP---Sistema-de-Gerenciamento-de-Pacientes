package br.edu.upe.sigep.config;

import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.validation.PoliticaSenha;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@Configuration
@Profile("dev")
public class CoordenadorBootstrap {

    @Bean
    public ApplicationRunner inicializarCoordenador(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.existsByPerfil(
                    PerfilUsuario.COORDENADOR)) {
                return;
            }

            String nome = System.getenv("SIGEP_ADMIN_NOME");
            String email = System.getenv("SIGEP_ADMIN_EMAIL");
            String senha = System.getenv("SIGEP_ADMIN_SENHA");

            if (nome == null || nome.isBlank()
                    || email == null || email.isBlank()
                    || senha == null || senha.isBlank()) {

                throw new IllegalStateException(
                        "Configure SIGEP_ADMIN_NOME, " +
                                "SIGEP_ADMIN_EMAIL e SIGEP_ADMIN_SENHA."
                );
            }

            if (!PoliticaSenha.valida(senha)) {
                throw new IllegalStateException(
                        "A senha administrativa deve ter " +
                                "pelo menos 12 caracteres e " +
                                "no máximo 72 bytes."
                );
            }

            Usuario coordenador = new Usuario();

            coordenador.setNome(nome.trim());
            coordenador.setEmail(
                    email.trim().toLowerCase(Locale.ROOT)
            );
            coordenador.setSenhaHash(
                    passwordEncoder.encode(senha)
            );
            coordenador.setPerfil(
                    PerfilUsuario.COORDENADOR
            );
            coordenador.setAtivo(true);
            coordenador.setCriadoPor(null);

            usuarioRepository.save(coordenador);

            System.out.println(
                    "Coordenador de desenvolvimento inicializado."
            );
        };
    }
}