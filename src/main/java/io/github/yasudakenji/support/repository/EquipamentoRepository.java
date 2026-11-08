package io.github.yasudakenji.support.repository;

import io.github.yasudakenji.support.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EquipamentoRepository extends JpaRepository<Equipamento, UUID> {
    List<Equipamento> findBySetor_Id(UUID setorId);

    long countBySetor_Id(UUID setorId);
}
