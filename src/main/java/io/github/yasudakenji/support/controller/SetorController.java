package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.SetorDTO;
import io.github.yasudakenji.support.service.SetorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/setor")
@RequiredArgsConstructor
public class SetorController {

    private final SetorService service;

    @GetMapping("/buscar")
    public ResponseEntity<SetorDTO> buscarSetor(@RequestParam("id") UUID id) {
        return ResponseEntity.ok(service.buscarSetor(id));
    }

    @GetMapping("/listar")
    public List<SetorDTO> listarSetor() {
        return service.listarSetor();
    }

    @PostMapping("/salvar")
    void salvarSetor(@Valid @RequestBody SetorDTO dto) {
        service.salvarSetor(dto);
    }

    @PutMapping("/atualizar")
    void atualizarSetor(@Valid @RequestBody SetorDTO dto) {
        service.atualizarSetor(dto);
    }

    @DeleteMapping("/deletar")
    void deletarSetor(@RequestParam("id") UUID id) {
        service.deletarSetor(id);
    }
}
