package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Paciente;
import br.edu.upe.sigep.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PacienteService {
    private final PacienteRepository pacienteRepository;

    @Transactional
    public Paciente cadastrar(Paciente paciente) {
        validar(paciente);
        if (pacienteRepository.existsByCpf(paciente.getCpf())) throw new IllegalArgumentException("CPF já cadastrado.");
        if (pacienteRepository.existsByCartaoSus(paciente.getCartaoSus())) throw new IllegalArgumentException("Cartão SUS já cadastrado.");
        paciente.setAtivo(true);
        paciente.setCriadoEm(LocalDateTime.now());
        return pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true) public List<Paciente> listar() { return pacienteRepository.findAll(); }
    @Transactional(readOnly = true) public List<Paciente> listarAtivos() { return pacienteRepository.findByAtivoTrue(); }
    @Transactional(readOnly = true) public Optional<Paciente> buscarPorId(Long id) { return pacienteRepository.findById(id); }
    @Transactional(readOnly = true) public Optional<Paciente> buscarPorCpf(String cpf) { return pacienteRepository.findByCpf(cpf); }

    @Transactional
    public Paciente atualizar(Paciente paciente) {
        if (paciente == null || paciente.getId() == null || !pacienteRepository.existsById(paciente.getId())) throw new IllegalArgumentException("Paciente não encontrado.");
        validar(paciente);
        if (pacienteRepository.existsByCpfAndIdNot(paciente.getCpf(), paciente.getId())) throw new IllegalArgumentException("CPF já cadastrado.");
        if (pacienteRepository.existsByCartaoSusAndIdNot(paciente.getCartaoSus(), paciente.getId())) throw new IllegalArgumentException("Cartão SUS já cadastrado.");
        return pacienteRepository.save(paciente);
    }

    @Transactional
    public Paciente alterarAtivo(Long id, boolean ativo) {
        Paciente paciente = pacienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        paciente.setAtivo(ativo);
        return pacienteRepository.save(paciente);
    }

    private void validar(Paciente p) {
        if (p == null || vazio(p.getNomeCompleto()) || vazio(p.getCpf()) || p.getDataNascimento() == null || vazio(p.getCartaoSus())
                || vazio(p.getTelefone()) || vazio(p.getEndereco()) || vazio(p.getUbsOrigem())) throw new IllegalArgumentException("Dados obrigatórios do paciente não informados.");
    }
    private boolean vazio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
