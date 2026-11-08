package io.github.yasudakenji.support.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.yasudakenji.support.model.Setor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SetorDTO {

    private UUID id;

    @NotBlank
    @Size(max = 50)
    private String nome;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long totalEquipamentos;

    public Setor toEntity() {
        Setor entity = new Setor();
        entity.setNome(nome);
        return entity;
    }

    public SetorDTO toDTO(Setor entity, long totalEquipamentos) {
        SetorDTO dto = new SetorDTO();
        dto.setId(entity.getId());
        dto.setNome(entity.getNome());
        dto.setTotalEquipamentos(totalEquipamentos);
        return dto;
    }
}
