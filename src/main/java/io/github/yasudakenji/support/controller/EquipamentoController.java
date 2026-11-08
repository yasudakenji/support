package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.EquipamentoDTO;
import io.github.yasudakenji.support.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/equipamento")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    @GetMapping("/buscar")
    public ResponseEntity<EquipamentoDTO> buscarEquipamento(@Valid @RequestParam("id") UUID id){
        return ResponseEntity.ok(equipamentoService.buscarEquipamento(id));
    }

    @GetMapping("/listar")
    public List<EquipamentoDTO> listarEquipamento(
            @RequestParam(value = "setorId", required = false) UUID setorId) {
        return equipamentoService.listarEquipamento(setorId);
    }

    @PostMapping("/salvar")
    void salvarEquipamento(@Valid @RequestBody EquipamentoDTO dto){
        equipamentoService.salvarEquipamento(dto);
    }

    @DeleteMapping("/deletar")
    void deletarEquipamento(@RequestParam("id") UUID id){
        equipamentoService.deletarEquipamento(id);
    }

    @PutMapping("/atualizar")
    void atualizarEquipamento(@Valid @RequestBody EquipamentoDTO dto){
        equipamentoService.atualizarEquipamento(dto);
    }


}
