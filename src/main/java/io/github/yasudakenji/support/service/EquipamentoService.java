package io.github.yasudakenji.support.service;


import io.github.yasudakenji.support.dto.EquipamentoDTO;

import java.util.List;
import java.util.UUID;

public interface EquipamentoService {

    void salvarEquipamento(EquipamentoDTO equipamento);

    EquipamentoDTO buscarEquipamento(UUID id);

    List<EquipamentoDTO> listarEquipamento(UUID setorId);

    void deletarEquipamento(UUID id);

    void atualizarEquipamento(EquipamentoDTO equipamento);

}
