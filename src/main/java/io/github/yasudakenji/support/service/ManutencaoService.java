package io.github.yasudakenji.support.service;

import io.github.yasudakenji.support.dto.ManutencaoDTO;

import java.util.List;
import java.util.UUID;

public interface ManutencaoService {

    void salvarManutencao(ManutencaoDTO dto);

    void deletarManutencao(UUID id);

    ManutencaoDTO buscarManutencao(UUID id);

    List<ManutencaoDTO> listarManutencao(UUID planoManutencaoId);
}
