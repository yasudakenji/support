package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.UsuarioDTO;
import io.github.yasudakenji.support.model.Usuario;
import io.github.yasudakenji.support.repository.UsuarioRepository;
import io.github.yasudakenji.support.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void salvarUsuario(UsuarioDTO dto) {
        String email = dto.getEmail().toLowerCase(Locale.ROOT);
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado");
        }

        if (dto.getSenha().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha deve ter até 72 bytes");
        }

        String senhaHash = passwordEncoder.encode(dto.getSenha());
        Usuario usuario = dto.toEntity(email, senhaHash);
        try {
            repository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este e-mail já está cadastrado", exception);
        }
    }
}
