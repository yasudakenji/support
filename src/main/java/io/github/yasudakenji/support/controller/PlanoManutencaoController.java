package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.PlanoManutencaoDTO;
import io.github.yasudakenji.support.service.PlanoManutencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/plano-manutencao")
@RequiredArgsConstructor
public class PlanoManutencaoController {

    private final PlanoManutencaoService service;

    @GetMapping("/buscar")
    public ResponseEntity<PlanoManutencaoDTO> buscarPlanoManutencao(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(service.buscarPlanoManutencao(id));
    }

    @GetMapping("/listar")
    public List<PlanoManutencaoDTO> listarPlanoManutencao(
            @RequestParam(value = "equipamentoId", required = false) UUID equipamentoId) {
        return service.listarPlanoManutencao(equipamentoId);
    }

    @PostMapping("/salvar")
    void salvarPlanoManutencao(@Valid @RequestBody PlanoManutencaoDTO dto) {
        service.salvarPlanoManutencao(dto);
    }

    @PutMapping("/atualizar")
    void atualizarPlanoManutencao(@Valid @RequestBody PlanoManutencaoDTO dto) {
        service.atualizarPlanoManutencao(dto);
    }

    @DeleteMapping("/deletar")
    void deletarPlanoManutencao(@RequestParam("id") UUID id) {
        service.deletarPlanoManutencao(id);
    }
}
