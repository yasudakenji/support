package io.github.yasudakenji.support.repository;

import io.github.yasudakenji.support.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SetorRepository extends JpaRepository<Setor, UUID> {
}
