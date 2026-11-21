package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.UsuarioCadastroRequestDTO;
import br.edu.upe.sigep.dto.response.UsuarioResponseDTO;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.enums.PerfilUsuario;
import br.edu.upe.sigep.event.ConviteCriadoEvent;
import br.edu.upe.sigep.exception.EmailJaCadastradoException;
import br.edu.upe.sigep.exception.OperacaoNaoPermitidaException;
import br.edu.upe.sigep.mapper.UsuarioMapper;
import br.edu.upe.sigep.repository.UsuarioRepository;
import br.edu.upe.sigep.service.CadastroFuncionarioResultado;
import br.edu.upe.sigep.service.ConviteAtivacaoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsuarioServiceImplTest {

    private UsuarioRepository repository;
    private UsuarioMapper mapper;
    private ConviteAtivacaoService conviteService;
    private ApplicationEventPublisher eventPublisher;
    private UsuarioServiceImpl service;

    @BeforeEach
    void preparar() {
        repository = mock(UsuarioRepository.class);
        mapper = mock(UsuarioMapper.class);
        conviteService = mock(ConviteAtivacaoService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);

        service = new UsuarioServiceImpl(
                repository,
                mapper,
                conviteService,
                eventPublisher
        );
    }

    private Usuario coordenadorAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPerfil(PerfilUsuario.COORDENADOR);
        usuario.setAtivo(true);
        return usuario;
    }

    private UsuarioCadastroRequestDTO dadosValidos() {
        return new UsuarioCadastroRequestDTO(
                "Funcionario Teste",
                "funcionario@sigep.example"
        );
    }

    @Test
    void deveCadastrarFuncionarioInativoComPrimeiroAcesso() {
        when(repository.existsByEmail(anyString()))
                .thenReturn(false);

        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(10L);
                    return usuario;
                });

        when(conviteService.criarConvite(any(Usuario.class)))
                .thenReturn("token-teste");

        service.cadastrarFuncionario(
                dadosValidos(),
                coordenadorAtivo()
        );

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(repository).save(captor.capture());

        Usuario salvo = captor.getValue();

        assertEquals(PerfilUsuario.FUNCIONARIO, salvo.getPerfil());
        assertFalse(salvo.isAtivo());
        assertTrue(salvo.isPrimeiroAcesso());
        assertNull(salvo.getSenhaHash());
        assertEquals(1L, salvo.getCriadoPor().getId());
    }

    @Test
    void deveNormalizarNomeEEmail() {
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(conviteService.criarConvite(any(Usuario.class)))
                .thenReturn("token-teste");

        service.cadastrarFuncionario(
                new UsuarioCadastroRequestDTO(
                        "  Maria Silva  ",
                        "  MARIA@SIGEP.EXAMPLE  "
                ),
                coordenadorAtivo()
        );

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(repository).save(captor.capture());

        assertEquals("Maria Silva", captor.getValue().getNome());
        assertEquals(
                "maria@sigep.example",
                captor.getValue().getEmail()
        );

        verify(repository).existsByEmail("maria@sigep.example");
    }

    @Test
    void deveRecusarEmailDuplicado() {
        when(repository.existsByEmail("funcionario@sigep.example"))
                .thenReturn(true);

        assertThrows(
                EmailJaCadastradoException.class,
                () -> service.cadastrarFuncionario(
                        dadosValidos(),
                        coordenadorAtivo()
                )
        );

        verify(repository, never()).save(any());
        verifyNoInteractions(conviteService, eventPublisher);
    }

    @Test
    void deveRecusarCadastroSemCoordenador() {
        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.cadastrarFuncionario(
                        dadosValidos(),
                        null
                )
        );

        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void deveRecusarCadastroPorFuncionario() {
        Usuario funcionario = new Usuario();
        funcionario.setPerfil(PerfilUsuario.FUNCIONARIO);
        funcionario.setAtivo(true);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.cadastrarFuncionario(
                        dadosValidos(),
                        funcionario
                )
        );

        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void deveRecusarCoordenadorInativo() {
        Usuario coordenador = coordenadorAtivo();
        coordenador.setAtivo(false);

        assertThrows(
                OperacaoNaoPermitidaException.class,
                () -> service.cadastrarFuncionario(
                        dadosValidos(),
                        coordenador
                )
        );

        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void deveRecusarDadosNulos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrarFuncionario(
                        null,
                        coordenadorAtivo()
                )
        );

        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void deveRecusarNomeEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrarFuncionario(
                        new UsuarioCadastroRequestDTO(
                                "   ",
                                "teste@sigep.example"
                        ),
                        coordenadorAtivo()
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveRecusarEmailEmBranco() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.cadastrarFuncionario(
                        new UsuarioCadastroRequestDTO(
                                "Funcionario",
                                "   "
                        ),
                        coordenadorAtivo()
                )
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveCriarConviteEPublicarEvento() {
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(10L);
                    return usuario;
                });

        when(conviteService.criarConvite(any(Usuario.class)))
                .thenReturn("token-teste");

        LocalDateTime criadoEm = LocalDateTime.of(2026, 1, 15, 10, 0);
        UsuarioResponseDTO esperado = new UsuarioResponseDTO(
                10L, "Funcionario Teste", "funcionario@sigep.example",
                PerfilUsuario.FUNCIONARIO, false, true, 1L, criadoEm
        );
        when(mapper.toResponseDTO(any(Usuario.class))).thenReturn(esperado);

        CadastroFuncionarioResultado resultado =
                service.cadastrarFuncionario(dadosValidos(), coordenadorAtivo());

        assertEquals(new CadastroFuncionarioResultado(esperado, "token-teste"), resultado);
        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        InOrder ordem = inOrder(repository, conviteService, eventPublisher);
        ordem.verify(repository).save(salvo.capture());
        ordem.verify(conviteService).criarConvite(same(salvo.getValue()));
        ordem.verify(eventPublisher).publishEvent(new ConviteCriadoEvent(
                "Funcionario Teste", "funcionario@sigep.example", "token-teste"
        ));
    }

    @Test
    void naoDevePublicarEventoSeCriacaoDoConviteFalhar() {
        when(repository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(conviteService.criarConvite(any(Usuario.class)))
                .thenThrow(new IllegalStateException("Falha no convite"));

        assertThrows(
                IllegalStateException.class,
                () -> service.cadastrarFuncionario(
                        dadosValidos(),
                        coordenadorAtivo()
                )
        );

        verifyNoInteractions(eventPublisher);
    }

    @Test
    void deveBuscarUsuarioExistente() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);

        when(repository.findById(5L))
                .thenReturn(Optional.of(usuario));

        UsuarioResponseDTO esperado = dtoConsulta(5L, "Pessoa A");
        when(mapper.toResponseDTO(usuario)).thenReturn(esperado);

        assertEquals(esperado, service.buscarPorId(5L));

        verify(mapper).toResponseDTO(usuario);
    }

    @Test
    void deveRecusarBuscaPorIdInexistente() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.buscarPorId(999L)
        );
    }

    @Test
    void deveListarTodosOsUsuarios() {
        Usuario primeiro = new Usuario();
        primeiro.setId(1L);

        Usuario segundo = new Usuario();
        segundo.setId(2L);

        when(repository.findAll())
                .thenReturn(List.of(primeiro, segundo));

        UsuarioResponseDTO primeiroDto = dtoConsulta(1L, "Pessoa A");
        UsuarioResponseDTO segundoDto = dtoConsulta(2L, "Pessoa B");
        when(mapper.toResponseDTO(primeiro)).thenReturn(primeiroDto);
        when(mapper.toResponseDTO(segundo)).thenReturn(segundoDto);

        assertEquals(List.of(primeiroDto, segundoDto), service.listarTodos());

        verify(mapper).toResponseDTO(primeiro);
        verify(mapper).toResponseDTO(segundo);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoExistiremUsuarios() {
        when(repository.findAll()).thenReturn(List.of());

        assertTrue(service.listarTodos().isEmpty());
    }

    @Test
    void deveRecusarNomeNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrarFuncionario(
                new UsuarioCadastroRequestDTO(null, "teste@sigep.example"), coordenadorAtivo()
        ));
        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void deveRecusarEmailNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.cadastrarFuncionario(
                new UsuarioCadastroRequestDTO("Funcionario", null), coordenadorAtivo()
        ));
        verifyNoInteractions(repository, conviteService, eventPublisher);
    }

    @Test
    void naoDeveCriarConviteNemPublicarEventoSePersistenciaFalhar() {
        IllegalStateException falha = new IllegalStateException("Falha ao salvar usuário");
        when(repository.save(any(Usuario.class))).thenThrow(falha);

        assertSame(falha, assertThrows(IllegalStateException.class,
                () -> service.cadastrarFuncionario(dadosValidos(), coordenadorAtivo())));
        verifyNoInteractions(mapper, conviteService, eventPublisher);
    }

    private UsuarioResponseDTO dtoConsulta(long id, String nome) {
        return new UsuarioResponseDTO(
                id, nome, "teste" + id + "@sigep.example", PerfilUsuario.FUNCIONARIO,
                true, false, 10L, LocalDateTime.of(2026, 1, 15, 10, 0)
        );
    }
}
