package br.edu.upe.sigep.service;

import br.edu.upe.sigep.entity.ConviteAtivacao;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.exception.ConviteInvalidoException;
import br.edu.upe.sigep.repository.ConviteAtivacaoRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class ConviteAtivacaoService {

    private final ConviteAtivacaoRepository conviteRepository;
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ConviteAtivacaoService(
            ConviteAtivacaoRepository conviteRepository,
            TokenService tokenService,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.conviteRepository = conviteRepository;
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String criarConvite(Usuario usuario) {

        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException(
                    "O usuário deve estar cadastrado para receber um convite."
            );
        }

        String token = tokenService.gerarToken();
        String tokenHash = tokenService.gerarHash(token);

        LocalDateTime agora = LocalDateTime.now();

        ConviteAtivacao convite = new ConviteAtivacao();

        convite.setUsuario(usuario);
        convite.setTokenHash(tokenHash);
        convite.setCriadoEm(agora);
        convite.setExpiraEm(agora.plusHours(24));

        conviteRepository.save(convite);

        return token;
    }

    @Transactional
    public void ativarConta(String token, String senha) {

        validarDadosAtivacao(token, senha);

        String tokenHash = tokenService.gerarHash(token);

        ConviteAtivacao convite = conviteRepository
                .buscarPorTokenHashComBloqueio(tokenHash)
                .orElseThrow(() -> new ConviteInvalidoException(
                        "Convite inválido ou expirado."
                ));

        LocalDateTime agora = LocalDateTime.now();

        if (convite.getUtilizadoEm() != null
                || !convite.getExpiraEm().isAfter(agora)) {

            throw new ConviteInvalidoException(
                    "Convite inválido ou expirado."
            );
        }

        Usuario usuario = convite.getUsuario();

        if (usuario.isAtivo()
                || !usuario.isPrimeiroAcesso()
                || usuario.getPerfil() != PerfilUsuario.FUNCIONARIO) {

            throw new ConviteInvalidoException(
                    "Convite inválido ou expirado."
            );
        }

        String senhaHash = passwordEncoder.encode(senha);

        usuario.setSenhaHash(senhaHash);
        usuario.setAtivo(true);
        usuario.setPrimeiroAcesso(false);

        convite.setUtilizadoEm(agora);

        usuarioRepository.save(usuario);
        conviteRepository.save(convite);
    }

    private void validarDadosAtivacao(String token, String senha) {

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "O token de ativação é obrigatório."
            );
        }

        if (senha == null || senha.isBlank()) {
            throw new IllegalArgumentException(
                    "A senha é obrigatória."
            );
        }

        int tamanhoEmBytes = senha.getBytes(
                StandardCharsets.UTF_8
        ).length;

        if (senha.length() < 12 || tamanhoEmBytes > 72) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 12 caracteres " +
                            "e no máximo 72 bytes em UTF-8."
            );
        }
    }
}