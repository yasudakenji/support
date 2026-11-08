package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.dto.UsuarioDTO;
import io.github.yasudakenji.support.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping("/salvar")
    void salvarUsuario(@Valid @RequestBody UsuarioDTO dto) {
        service.salvarUsuario(dto);
    }
}
