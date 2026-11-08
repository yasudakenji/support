package io.github.yasudakenji.support.service;

import io.github.yasudakenji.support.dto.PlanoManutencaoDTO;

import java.util.List;
import java.util.UUID;

public interface PlanoManutencaoService {

    void salvarPlanoManutencao(PlanoManutencaoDTO dto);

    PlanoManutencaoDTO buscarPlanoManutencao(UUID id);

    List<PlanoManutencaoDTO> listarPlanoManutencao(UUID equipamentoId);

    void atualizarPlanoManutencao(PlanoManutencaoDTO dto);

    void deletarPlanoManutencao(UUID id);
}
