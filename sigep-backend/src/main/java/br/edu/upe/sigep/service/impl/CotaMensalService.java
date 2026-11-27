package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.dto.request.CotaMensalRequestDTO;
import br.edu.upe.sigep.dto.response.CotaMensalResponseDTO;
import br.edu.upe.sigep.entity.CotaMensal;
import br.edu.upe.sigep.entity.Especialidade;
import br.edu.upe.sigep.entity.Profissional;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.mapper.CotaMensalMapper;
import br.edu.upe.sigep.repository.CotaMensalRepository;
import br.edu.upe.sigep.repository.EspecialidadeRepository;
import br.edu.upe.sigep.repository.ProfissionalRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CotaMensalService {

    private final CotaMensalRepository cotaMensalRepository;
    private final ProfissionalRepository profissionalRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final UsuarioRepository usuarioRepository;
    private final CotaMensalMapper mapper;

    @Transactional
    public CotaMensalResponseDTO cadastrar(CotaMensalRequestDTO dto) {
        validarMes(dto.getMesReferencia());

        Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada com ID: " + dto.getEspecialidadeId()));

        if (!profissional.isAtivo() || !especialidade.isAtivo()) {
            throw new IllegalStateException("Profissional e especialidade devem estar ativos para cadastrar cota.");
        }

        if (!profissional.getEspecialidade().getId().equals(especialidade.getId())) {
            throw new IllegalArgumentException("O profissional informado não pertence à especialidade selecionada.");
        }

        if (cotaMensalRepository.existsByProfissionalIdAndEspecialidadeIdAndMesReferenciaAndAnoReferencia(
                dto.getProfissionalId(), dto.getEspecialidadeId(), dto.getMesReferencia(), dto.getAnoReferencia())) {
            throw new IllegalStateException("Já existe uma cota cadastrada para este profissional, especialidade e período.");
        }

        Usuario usuarioLogado = obterUsuarioAutenticado();
        CotaMensal novaCota = mapper.toEntity(dto, profissional, especialidade, usuarioLogado);
        return mapper.toResponseDTO(cotaMensalRepository.save(novaCota));
    }

    @Transactional(readOnly = true)
    public List<CotaMensalResponseDTO> consultar(Long profissionalId, Long especialidadeId, Short mesReferencia, Short anoReferencia) {
        if (mesReferencia != null) {
            validarMes(mesReferencia);
        }
        return cotaMensalRepository.consultarPorFiltros(profissionalId, especialidadeId, mesReferencia, anoReferencia)
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CotaMensalResponseDTO buscarPorId(Long id) {
        CotaMensal cota = cotaMensalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cota mensal não encontrada com ID: " + id));
        return mapper.toResponseDTO(cota);
    }

    @Transactional
    public CotaMensalResponseDTO atualizar(Long id, CotaMensalRequestDTO dto) {
        validarMes(dto.getMesReferencia());

        CotaMensal cota = cotaMensalRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cota mensal não encontrada com ID: " + id));

        Profissional profissional = profissionalRepository.findById(dto.getProfissionalId())
                .orElseThrow(() -> new EntityNotFoundException("Profissional não encontrado com ID: " + dto.getProfissionalId()));

        Especialidade especialidade = especialidadeRepository.findById(dto.getEspecialidadeId())
                .orElseThrow(() -> new EntityNotFoundException("Especialidade não encontrada com ID: " + dto.getEspecialidadeId()));

        if (!profissional.getEspecialidade().getId().equals(especialidade.getId())) {
            throw new IllegalArgumentException("O profissional informado não pertence à especialidade selecionada.");
        }

        cota.setProfissional(profissional);
        cota.setEspecialidade(especialidade);
        cota.setMesReferencia(dto.getMesReferencia());
        cota.setAnoReferencia(dto.getAnoReferencia());
        cota.setQtdCotaLimite(dto.getQtdCotaLimite());

        return mapper.toResponseDTO(cotaMensalRepository.save(cota));
    }

    @Transactional(readOnly = true)
    public boolean temCotaDisponivel(Long profissionalId, Long especialidadeId, LocalDate data) {
        short mes = (short) data.getMonthValue();
        short ano = (short) data.getYear();
        validarMes(mes);

        return cotaMensalRepository
                .findByProfissionalIdAndEspecialidadeIdAndMesReferenciaAndAnoReferencia(profissionalId, especialidadeId, mes, ano)
                .map(cota -> cota.getQtdCotaLimite() > 0)
                .orElse(false);
    }

    @Transactional
    public void decrementarCota(Long profissionalId, Long especialidadeId, LocalDate data) {
        short mes = (short) data.getMonthValue();
        short ano = (short) data.getYear();
        validarMes(mes);

        CotaMensal cota = cotaMensalRepository
                .buscarComLockParaAtualizacao(profissionalId, especialidadeId, mes, ano)
                .orElseThrow(() -> new EntityNotFoundException("Cota mensal não encontrada para o profissional, especialidade e período informados."));

        if (cota.getQtdCotaLimite() <= 0) {
            throw new IllegalStateException("Não há vagas disponíveis nesta cota mensal.");
        }

        cota.setQtdCotaLimite(cota.getQtdCotaLimite() - 1);
        cotaMensalRepository.save(cota);
    }

    @Transactional
    public void incrementarCota(Long profissionalId, Long especialidadeId, LocalDate data) {
        short mes = (short) data.getMonthValue();
        short ano = (short) data.getYear();

        CotaMensal cota = cotaMensalRepository
                .buscarComLockParaAtualizacao(profissionalId, especialidadeId, mes, ano)
                .orElseThrow(() -> new EntityNotFoundException("Cota mensal não encontrada para estorno."));

        cota.setQtdCotaLimite(cota.getQtdCotaLimite() + 1);
        cotaMensalRepository.save(cota);
    }

    /**
     * @deprecated Mantido apenas para compatibilidade com testes legados.
     *             Utilize {@link #temCotaDisponivel(Long, Long, LocalDate)}.
     */
    @Deprecated
    @Transactional(readOnly = true)
    public boolean temCotaDisponivel(Long especialidadeId, LocalDate data) {
        short mes = (short) data.getMonthValue();
        short ano = (short) data.getYear();
        validarMes(mes);

        return cotaMensalRepository.consultarPorFiltros(null, especialidadeId, mes, ano)
                .stream()
                .anyMatch(cota -> cota.getQtdCotaLimite() > 0);
    }

    /**
     * @deprecated Mantido apenas para compatibilidade com testes legados.
     *             Utilize {@link #decrementarCota(Long, Long, LocalDate)}.
     */
    @Deprecated
    @Transactional
    public void decrementarCota(Long especialidadeId, LocalDate data) {
        short mes = (short) data.getMonthValue();
        short ano = (short) data.getYear();
        validarMes(mes);

        CotaMensal cota = cotaMensalRepository.consultarPorFiltros(null, especialidadeId, mes, ano)
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cota mensal não encontrada para esta especialidade e período."));

        decrementarCota(cota.getProfissional().getId(), especialidadeId, data);
    }

    private void validarMes(short mes) {
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("O mês de referência deve estar entre 1 e 12.");
        }
    }

    private Usuario obterUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        return usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EntityNotFoundException("Usuário autenticado não encontrado: " + auth.getName()));
    }
}