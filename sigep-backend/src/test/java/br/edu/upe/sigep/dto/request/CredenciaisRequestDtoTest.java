package br.edu.upe.sigep.dto.request;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class CredenciaisRequestDtoTest {

    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void deveLerSenhaInicialSemExporEmTextoOuJson() {
        var cadastro = json.readValue("""
                {"nome":"Pessoa","email":"pessoa@sigep.example","senha":"segredo-inicial-teste"}
                """, UsuarioCadastroRequestDTO.class);

        assertThat(cadastro.senha()).isEqualTo("segredo-inicial-teste");
        assertThat(cadastro.toString()).doesNotContain("senha", cadastro.senha());
        assertThat(json.writeValueAsString(cadastro))
                .contains("\"nome\"", "\"email\"")
                .doesNotContain("\"senha\"", cadastro.senha());
    }

    @Test
    void deveLerSenhasDaTrocaSemExporEmTextoOuJson() {
        var credenciais = json.readValue("""
                {"nome":"Coordenação","email":"pessoa@sigep.example",
                 "senhaAtual":"segredo-atual-teste","novaSenha":"segredo-novo-teste"}
                """, CoordenadorCredenciaisRequestDTO.class);

        assertThat(credenciais.senhaAtual()).isEqualTo("segredo-atual-teste");
        assertThat(credenciais.novaSenha()).isEqualTo("segredo-novo-teste");
        assertThat(credenciais.toString())
                .doesNotContain("senhaAtual", "novaSenha", credenciais.senhaAtual(), credenciais.novaSenha());
        assertThat(json.writeValueAsString(credenciais))
                .contains("\"nome\"", "\"email\"")
                .doesNotContain("\"senhaAtual\"", "\"novaSenha\"", credenciais.senhaAtual(), credenciais.novaSenha());
    }
}
