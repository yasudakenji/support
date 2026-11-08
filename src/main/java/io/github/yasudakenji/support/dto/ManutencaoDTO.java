package io.github.yasudakenji.support.dto;

import io.github.yasudakenji.support.model.Manutencao;
import io.github.yasudakenji.support.model.PlanoManutencao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class ManutencaoDTO {

    private UUID id;

    @NotNull
    private UUID planoManutencaoId;

    @NotNull
    @PastOrPresent
    private LocalDate dataManutencao;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    private BigDecimal horimetroNaExecucao;

    @Size(max = 1000)
    private String descricaoExecucao;

    public Manutencao toEntity(PlanoManutencao planoManutencao) {
        Manutencao entity = new Manutencao();
        entity.setPlanoManutencao(planoManutencao);
        entity.setDataManutencao(dataManutencao);
        entity.setHorimetroNaExecucao(horimetroNaExecucao);
        entity.setDescricaoExecucao(descricaoExecucao);
        return entity;
    }

    public ManutencaoDTO toDTO(Manutencao entity) {
        ManutencaoDTO dto = new ManutencaoDTO();
        dto.setId(entity.getId());
        dto.setPlanoManutencaoId(entity.getPlanoManutencao().getId());
        dto.setDataManutencao(entity.getDataManutencao());
        dto.setHorimetroNaExecucao(entity.getHorimetroNaExecucao());
        dto.setDescricaoExecucao(entity.getDescricaoExecucao());
        return dto;
    }
}
