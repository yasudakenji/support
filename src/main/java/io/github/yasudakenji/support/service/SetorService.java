package io.github.yasudakenji.support.service;

import io.github.yasudakenji.support.dto.SetorDTO;

import java.util.List;
import java.util.UUID;

public interface SetorService {

    void salvarSetor(SetorDTO dto);

    SetorDTO buscarSetor(UUID id);

    List<SetorDTO> listarSetor();

    void atualizarSetor(SetorDTO dto);

    void deletarSetor(UUID id);
}
