package io.github.yasudakenji.support.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.yasudakenji.support.model.PlanoManutencao;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.enums.SituacaoHistoricoInicial;
import io.github.yasudakenji.support.model.enums.StatusRevisao;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PlanoManutencaoDTO {

    private UUID id;

    @NotNull
    private UUID equipamentoId;

    @NotBlank
    @Size(max = 50)
    private String nomePlano;

    @NotNull
    @Positive
    @Digits(integer = 8, fraction = 2)
    private BigDecimal intervaloHoras;

    @NotBlank
    @Size(max = 500)
    private String descricao;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private SituacaoHistoricoInicial situacaoHistoricoInicial;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal horimetroAtual;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal horimetroUltimaRevisao;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal proximoHorimetro;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal horasRestantes;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private StatusRevisao statusRevisao;

    public PlanoManutencao toEntity(Equipamento equipamento) {
        PlanoManutencao entity = new PlanoManutencao();
        entity.setEquipamento(equipamento);
        entity.setNomePlano(nomePlano);
        entity.setIntervaloHoras(intervaloHoras);
        entity.setDescricao(descricao);
        return entity;
    }

    public PlanoManutencaoDTO toDTO(PlanoManutencao entity) {
        PlanoManutencaoDTO dto = new PlanoManutencaoDTO();
        dto.setId(entity.getId());
        dto.setEquipamentoId(entity.getEquipamento().getId());
        dto.setNomePlano(entity.getNomePlano());
        dto.setIntervaloHoras(entity.getIntervaloHoras());
        dto.setDescricao(entity.getDescricao());
        dto.setSituacaoHistoricoInicial(entity.getSituacaoHistoricoInicial());
        return dto;
    }
}
