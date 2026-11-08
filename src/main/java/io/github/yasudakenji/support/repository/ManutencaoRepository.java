package io.github.yasudakenji.support.repository;

import io.github.yasudakenji.support.model.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManutencaoRepository extends JpaRepository<Manutencao, UUID> {
    List<Manutencao> findByPlanoManutencao_Id(UUID planoManutencaoId);

    Optional<Manutencao> findFirstByPlanoManutencao_IdOrderByDataManutencaoDescHorimetroNaExecucaoDesc(
            UUID planoManutencaoId);

    @Query("select max(m.horimetroNaExecucao) from Manutencao m where m.planoManutencao.equipamento.id = :equipamentoId")
    Optional<BigDecimal> buscarMaiorHorimetro(@Param("equipamentoId") UUID equipamentoId);

    // Busca a data mais recente de cada plano. Em caso de empate, usa o maior horímetro.
    @Query("""
            select m.planoManutencao.id as planoId, max(m.horimetroNaExecucao) as horimetro
            from Manutencao m
            where m.planoManutencao.id in :planoIds
              and m.dataManutencao = (
                  select max(anterior.dataManutencao)
                  from Manutencao anterior
                  where anterior.planoManutencao.id = m.planoManutencao.id
              )
            group by m.planoManutencao.id
            """)
    List<UltimaRevisao> buscarUltimasRevisoes(@Param("planoIds") List<UUID> planoIds);

    interface UltimaRevisao {
        UUID getPlanoId();
        BigDecimal getHorimetro();
    }
}
