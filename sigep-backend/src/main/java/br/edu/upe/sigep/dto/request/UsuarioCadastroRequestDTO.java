package br.edu.upe.sigep.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioCadastroRequestDTO(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        @Size(max = 255)
        String email,

        @NotBlank(message = "A senha inicial é obrigatória")
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senha

) {
    @Override
    public String toString() {
        return "UsuarioCadastroRequestDTO[]";
    }
}
