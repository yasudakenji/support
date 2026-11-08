package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.LeituraHorimetroDTO;
import io.github.yasudakenji.support.service.LeituraHorimetroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leitura-horimetro")
@RequiredArgsConstructor
public class LeituraHorimetroController {

    private final LeituraHorimetroService service;

    @GetMapping("/buscar")
    public ResponseEntity<LeituraHorimetroDTO> buscarLeituraHorimetro(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(service.buscarLeituraHorimetro(id));
    }

    @GetMapping("/listar")
    public List<LeituraHorimetroDTO> listarLeituraHorimetro(
            @RequestParam(value = "equipamentoId", required = false) UUID equipamentoId) {
        return service.listarLeituraHorimetro(equipamentoId);
    }

    @PostMapping("/salvar")
    void salvarLeituraHorimetro(@Valid @RequestBody LeituraHorimetroDTO dto) {
        service.salvarLeituraHorimetro(dto);
    }

    @DeleteMapping("/deletar")
    void deletarLeituraHorimetro(@RequestParam("id") UUID id) {
        service.deletarLeituraHorimetro(id);
    }
}
