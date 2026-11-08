package io.github.yasudakenji.support.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.yasudakenji.support.model.Usuario;
import io.github.yasudakenji.support.model.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioDTO {

    @NotBlank
    @Size (max = 50)
    private String nome;

    @NotBlank
    @Email
    @Size (max = 254)
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @NotNull
    private Perfil perfil;

    public Usuario toEntity(String email, String senhaHash) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(senhaHash);
        usuario.setPerfil(perfil);
        return usuario;
    }
}
