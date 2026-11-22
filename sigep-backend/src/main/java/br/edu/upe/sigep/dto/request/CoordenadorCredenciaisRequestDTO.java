package br.edu.upe.sigep.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoordenadorCredenciaisRequestDTO(
        @NotBlank @Size(max = 150) String nome,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String senhaAtual,
        @NotBlank @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String novaSenha
) {
    @Override
    public String toString() {
        return "CoordenadorCredenciaisRequestDTO[]";
    }
}
