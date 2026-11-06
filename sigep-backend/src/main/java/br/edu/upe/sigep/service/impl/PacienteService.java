package br.edu.upe.sigep.service.impl;

import br.edu.upe.sigep.entity.Paciente;
import br.edu.upe.sigep.entity.Usuario;
import br.edu.upe.sigep.repository.PacienteRepository;
import br.edu.upe.sigep.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Paciente cadastrar(Paciente paciente) {
        validar(paciente);
        if (paciente.getId() != null) throw new IllegalArgumentException("O ID não deve ser informado no cadastro.");
        if (pacienteRepository.existsByCpf(paciente.getCpf())) throw new IllegalArgumentException("CPF já cadastrado.");
        if (pacienteRepository.existsByCartaoSus(paciente.getCartaoSus())) throw new IllegalArgumentException("Cartão SUS já cadastrado.");
        paciente.setAtivo(true);
        paciente.setCriadoEm(LocalDateTime.now());
        paciente.setCadastradoPor(resolverCadastrador(paciente.getCadastradoPor()));
        return pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true) public List<Paciente> listar() { return pacienteRepository.findAll(); }
    @Transactional(readOnly = true) public List<Paciente> listarAtivos() { return pacienteRepository.findByAtivoTrue(); }
    @Transactional(readOnly = true) public Optional<Paciente> buscarPorId(Long id) { return pacienteRepository.findById(id); }
    @Transactional(readOnly = true) public Optional<Paciente> buscarPorCpf(String cpf) { return pacienteRepository.findByCpf(cpf); }

    @Transactional
    public Paciente atualizar(Paciente paciente) {
        if (paciente == null || paciente.getId() == null) throw new IllegalArgumentException("Paciente não encontrado.");
        Paciente existente = pacienteRepository.findById(paciente.getId())
                .orElseThrow(() -> new IllegalArgumentException("Paciente não encontrado."));
        validar(paciente);
        if (pacienteRepository.existsByCpfAndIdNot(paciente.getCpf(), paciente.getId())) throw new IllegalArgumentException("CPF já cadastrado.");
        if (pacienteRepository.existsByCartaoSusAndIdNot(paciente.getCartaoSus(), paciente.getId())) throw new IllegalArgumentException("Cartão SUS já cadastrado.");
        existente.setNomeCompleto(paciente.getNomeCompleto());
        existente.setCpf(paciente.getCpf());
        existente.setDataNascimento(paciente.getDataNascimento());
        existente.setCartaoSus(paciente.getCartaoSus());
        existente.setTelefone(paciente.getTelefone());
        existente.setEndereco(paciente.getEndereco());
        existente.setUbsOrigem(paciente.getUbsOrigem());
        return pacienteRepository.save(existente);
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
        validarTamanho(p.getNomeCompleto(), 200, "NomeCompleto");
        validarTamanho(p.getCpf(), 11, "Cpf");
        validarTamanho(p.getCartaoSus(), 15, "CartaoSus");
        validarTamanho(p.getTelefone(), 20, "Telefone");
        validarTamanho(p.getEndereco(), 300, "Endereco");
        validarTamanho(p.getUbsOrigem(), 200, "UbsOrigem");
    }
    private Usuario resolverCadastrador(Usuario informado) {
        if (informado == null) return null;
        if (informado.getId() == null) throw new IllegalArgumentException("Informe o ID do usuário cadastrador.");
        return usuarioRepository.findById(informado.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuário cadastrador não encontrado."));
    }

    private void validarTamanho(String valor, int limite, String campo) {
        if (valor.length() > limite) throw new IllegalArgumentException(campo + " excede o limite de " + limite + " caracteres.");
    }

    private boolean vazio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
