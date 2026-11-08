package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.ManutencaoDTO;
import io.github.yasudakenji.support.service.ManutencaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/manutencao")
@RequiredArgsConstructor
public class ManutencaoController {

    private final ManutencaoService service;

    @GetMapping("/buscar")
    public ResponseEntity<ManutencaoDTO> buscarManutencao(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(service.buscarManutencao(id));
    }

    @GetMapping("/listar")
    public List<ManutencaoDTO> listarManutencao(
            @RequestParam(value = "planoManutencaoId", required = false) UUID planoManutencaoId) {
        return service.listarManutencao(planoManutencaoId);
    }

    @PostMapping("/salvar")
    void salvarManutencao(@Valid @RequestBody ManutencaoDTO dto) {
        service.salvarManutencao(dto);
    }

    @DeleteMapping("/deletar")
    void deletarManutencao(@RequestParam("id") UUID id) {
        service.deletarManutencao(id);
    }
}
