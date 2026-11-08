package io.github.yasudakenji.support.repository;

import io.github.yasudakenji.support.model.PlanoManutencao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, UUID> {
    @Override
    @EntityGraph(attributePaths = "equipamento")
    List<PlanoManutencao> findAll();

    @EntityGraph(attributePaths = "equipamento")
    List<PlanoManutencao> findByEquipamento_Id(UUID equipamentoId);
}
