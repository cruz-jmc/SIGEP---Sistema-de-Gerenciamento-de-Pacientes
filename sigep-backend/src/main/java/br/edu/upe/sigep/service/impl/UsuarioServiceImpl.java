package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.exception.OperacaoNaoPermitidaException;
import br.edu.upe.sigep.mapper.UsuarioMapper;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.ConviteAtivacaoService;
import br.edu.upe.sigep.service.UsuarioService;
import br.edu.upe.sigep.event.ConviteCriadoEvent;
import org.springframework.context.ApplicationEventPublisher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final ConviteAtivacaoService conviteAtivacaoService;
    private final ApplicationEventPublisher eventPublisher;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioMapper usuarioMapper,
            ConviteAtivacaoService conviteAtivacaoService, ApplicationEventPublisher eventPublisher) {

        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
        this.conviteAtivacaoService = conviteAtivacaoService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {

        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Usuário não encontrado: " + id
                        )
                );

        return usuarioMapper.toResponseDTO(usuario);
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {

        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioResponseDTO> respostas = new ArrayList<>();

        for (Usuario usuario : usuarios) {

            UsuarioResponseDTO dto =
                    usuarioMapper.toResponseDTO(usuario);

            respostas.add(dto);
        }

        return respostas;
    }

    @Override
    @Transactional
    public CadastroFuncionarioResultado cadastrarFuncionario(
            UsuarioCadastroRequestDTO dados,
            Usuario coordenador) {

        if (coordenador == null
                || coordenador.getPerfil() != PerfilUsuario.COORDENADOR
                || !coordenador.isAtivo()) {

            throw new OperacaoNaoPermitidaException(
                    "Você não possui permissão para cadastrar funcionários."
            );
        }

        if (dados == null
                || dados.nome() == null
                || dados.email() == null) {

            throw new IllegalArgumentException(
                    "Nome e e-mail são obrigatórios."
            );
        }

        String nome = dados.nome().trim();

        String email = dados.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (nome.isBlank() || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Nome e e-mail são obrigatórios."
            );
        }

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException(
                    "Já existe um usuário cadastrado com esse e-mail."
            );
        }

        Usuario funcionario = new Usuario();

        funcionario.setNome(nome);
        funcionario.setEmail(email);
        funcionario.setPerfil(PerfilUsuario.FUNCIONARIO);
        funcionario.setAtivo(false);
        funcionario.setPrimeiroAcesso(true);
        funcionario.setSenhaHash(null);
        funcionario.setCriadoPor(coordenador);

        Usuario salvo = usuarioRepository.save(funcionario);

        String token = conviteAtivacaoService.criarConvite(salvo);

        eventPublisher.publishEvent(
                new ConviteCriadoEvent(
                        salvo.getNome(),
                        salvo.getEmail(),
                        token
                )
        );

        return new CadastroFuncionarioResultado(
                usuarioMapper.toResponseDTO(salvo),
                token
        );
    }
}