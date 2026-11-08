package io.github.yasudakenji.support.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.Setor;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class EquipamentoDTO {

    private UUID id;

    private UUID setorId;

    @NotBlank
    @Size(max = 50)
    private String nome;

    @NotBlank
    @Size(max = 50)
    private String modelo;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDate dataCadastro;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    private BigDecimal horimetroAtual;

    public Equipamento toEntity(Setor setor) {
        Equipamento entity = new Equipamento();

        entity.setDataCadastro(LocalDate.now());
        entity.setNome(nome);
        entity.setModelo(modelo);
        entity.setHorimetroAtual(horimetroAtual);
        entity.setSetor(setor);

        return entity;
    }


    public EquipamentoDTO toDTO(Equipamento entity) {
        EquipamentoDTO dto = new EquipamentoDTO();

        dto.setId(entity.getId());
        dto.setDataCadastro(entity.getDataCadastro());
        dto.setHorimetroAtual(entity.getHorimetroAtual());
        dto.setModelo(entity.getModelo());
        dto.setNome(entity.getNome());
        Setor setor = entity.getSetor();
        if (setor != null) {
            dto.setSetorId(setor.getId());
        }

        return dto;
    }

}
