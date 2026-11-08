package io.github.yasudakenji.support.repository;

import io.github.yasudakenji.support.model.LeituraHorimetro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeituraHorimetroRepository extends JpaRepository<LeituraHorimetro, UUID> {

    List<LeituraHorimetro> findByEquipamento_IdOrderByDataHoraLeituraDesc(UUID equipamentoId);

    Optional<LeituraHorimetro> findFirstByEquipamento_IdOrderByDataHoraLeituraDesc(UUID equipamentoId);

    @Query("select max(l.valorHorimetro) from LeituraHorimetro l where l.equipamento.id = :equipamentoId")
    Optional<BigDecimal> buscarMaiorHorimetro(@Param("equipamentoId") UUID equipamentoId);
}
