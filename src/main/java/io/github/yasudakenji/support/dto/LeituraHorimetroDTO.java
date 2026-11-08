package io.github.yasudakenji.support.dto;

import io.github.yasudakenji.support.model.LeituraHorimetro;
import io.github.yasudakenji.support.model.Equipamento;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class LeituraHorimetroDTO {

    private UUID id;

    @NotNull
    private UUID equipamentoId;

    @NotNull
    @PastOrPresent
    private LocalDateTime dataHoraLeitura;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    private BigDecimal valorHorimetro;

    public LeituraHorimetro toEntity(Equipamento equipamento) {
        LeituraHorimetro entity = new LeituraHorimetro();
        entity.setEquipamento(equipamento);
        entity.setDataHoraLeitura(dataHoraLeitura);
        entity.setValorHorimetro(valorHorimetro);
        return entity;
    }

    public LeituraHorimetroDTO toDTO(LeituraHorimetro entity) {
        LeituraHorimetroDTO dto = new LeituraHorimetroDTO();
        dto.setId(entity.getId());
        dto.setEquipamentoId(entity.getEquipamento().getId());
        dto.setDataHoraLeitura(entity.getDataHoraLeitura());
        dto.setValorHorimetro(entity.getValorHorimetro());
        return dto;
    }
}
