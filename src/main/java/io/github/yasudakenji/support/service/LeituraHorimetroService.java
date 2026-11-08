package io.github.yasudakenji.support.service;

import io.github.yasudakenji.support.dto.LeituraHorimetroDTO;

import java.util.List;
import java.util.UUID;

public interface LeituraHorimetroService {

    void salvarLeituraHorimetro(LeituraHorimetroDTO dto);

    void deletarLeituraHorimetro(UUID id);

    LeituraHorimetroDTO buscarLeituraHorimetro(UUID id);

    List<LeituraHorimetroDTO> listarLeituraHorimetro(UUID equipamentoId);
}
