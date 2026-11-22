package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.CoordenadorCredenciaisRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioAtualizacaoRequestDTO;
import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.exception.OperacaoNaoPermitidaException;
import br.edu.upe.sigep.mapper.UsuarioMapper;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.UsuarioService;
import br.edu.upe.sigep.validation.PoliticaSenha;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static br.edu.upe.sigep.enums.PerfilUsuario.COORDENADOR;
import static br.edu.upe.sigep.enums.PerfilUsuario.FUNCIONARIO;

@Service
@Transactional(readOnly = true)
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarios;
    private final UsuarioMapper mapper;
    private final PasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository usuarios, UsuarioMapper mapper, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.mapper = mapper;
        this.encoder = encoder;
    }

    @Override
    public UsuarioResponseDTO buscarPorId(Long id) {
        return usuarios.findById(id).map(mapper::toResponseDTO)
                .orElseThrow(() -> usuarioInexistente(id));
    }

    @Override
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarios.findAll().stream().map(mapper::toResponseDTO).toList();
    }

    @Override
    @Transactional
    public UsuarioResponseDTO cadastrarFuncionario(UsuarioCadastroRequestDTO dados, Long coordenadorId) {
        Usuario coordenador = exigirCoordenador(usuarios.findById(coordenadorId)
                .orElseThrow(() -> usuarioInexistente(coordenadorId)));
        Usuario funcionario = new Usuario();
        preencherIdentificacao(funcionario, dados.nome(), dados.email());
        funcionario.setSenhaHash(codificarSenha(dados.senha()));
        funcionario.setPerfil(FUNCIONARIO);
        funcionario.setAtivo(true);
        funcionario.setCriadoPor(coordenador);
        return mapper.toResponseDTO(salvar(funcionario));
    }

    @Override
    @Transactional
    public UsuarioResponseDTO atualizarFuncionario(Long id, UsuarioAtualizacaoRequestDTO dados) {
        Usuario funcionario = buscarFuncionarioParaAlteracao(id);
        String emailAnterior = funcionario.getEmail();
        preencherIdentificacao(funcionario, dados.nome(), dados.email());
        if (!emailAnterior.equals(funcionario.getEmail())) {
            funcionario.setRevisaoCredenciais(funcionario.getRevisaoCredenciais() + 1);
        }
        return mapper.toResponseDTO(salvar(funcionario));
    }

    @Override
    @Transactional
    public void desativarFuncionario(Long id) {
        buscarFuncionarioParaAlteracao(id).setAtivo(false);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO atualizarCredenciais(Long coordenadorId, CoordenadorCredenciaisRequestDTO dados) {
        Usuario coordenador = exigirCoordenador(buscarParaAlteracao(coordenadorId));
        if (dados.senhaAtual() == null || !PoliticaSenha.cabeNoBCrypt(dados.senhaAtual())
                || !encoder.matches(dados.senhaAtual(), coordenador.getSenhaHash())) {
            throw new OperacaoNaoPermitidaException("A senha atual está incorreta.");
        }

        preencherIdentificacao(coordenador, dados.nome(), dados.email());
        coordenador.setSenhaHash(codificarSenha(dados.novaSenha()));
        coordenador.setRevisaoCredenciais(coordenador.getRevisaoCredenciais() + 1);
        return mapper.toResponseDTO(salvar(coordenador));
    }

    private void preencherIdentificacao(Usuario usuario, String nome, String email) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Nome e e-mail são obrigatórios.");
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (usuarios.buscarPorEmailNormalizado(emailNormalizado)
                .filter(outro -> !Objects.equals(outro.getId(), usuario.getId())).isPresent()) {
            throw emailDuplicado();
        }

        usuario.setNome(nome.trim());
        usuario.setEmail(emailNormalizado);
    }

    private String codificarSenha(String senha) {
        if (!PoliticaSenha.valida(senha)) {
            throw new IllegalArgumentException("A senha deve ter pelo menos 12 caracteres e no máximo 72 bytes UTF-8.");
        }
        return encoder.encode(senha);
    }

    private Usuario salvar(Usuario usuario) {
        try {
            return usuarios.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getCause() instanceof ConstraintViolationException violacao
                    && ("uk_usuario_email".equals(violacao.getConstraintName())
                    || "uq_usuario_email_normalizado".equals(violacao.getConstraintName()))) {
                throw emailDuplicado();
            }
            throw ex;
        }
    }

    private Usuario buscarParaAlteracao(Long id) {
        return usuarios.buscarParaAlteracao(id).orElseThrow(() -> usuarioInexistente(id));
    }

    private Usuario buscarFuncionarioParaAlteracao(Long id) {
        Usuario usuario = buscarParaAlteracao(id);
        if (usuario.getPerfil() != FUNCIONARIO) {
            throw new OperacaoNaoPermitidaException("Esta operação é exclusiva para funcionários.");
        }
        return usuario;
    }

    private Usuario exigirCoordenador(Usuario usuario) {
        if (usuario.getPerfil() != COORDENADOR || !usuario.isAtivo()) {
            throw new OperacaoNaoPermitidaException("Esta operação exige um coordenador ativo.");
        }
        return usuario;
    }

    private IllegalArgumentException usuarioInexistente(Long id) {
        return new IllegalArgumentException("Usuário não encontrado: " + id);
    }

    private EmailJaCadastradoException emailDuplicado() {
        return new EmailJaCadastradoException("Já existe um usuário cadastrado com esse e-mail.");
    }
}
