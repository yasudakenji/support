package io.github.yasudakenji.support.controller;

import io.github.yasudakenji.support.model.Usuario;
import io.github.yasudakenji.support.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/conta")
@RequiredArgsConstructor
public class ContaController {

    private final UsuarioRepository usuarioRepository;

    @GetMapping("/atual")
    public Map<String, Object> buscarContaAtual(HttpServletRequest request, CsrfToken csrfToken) {
        String login = request.getUserPrincipal().getName();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(login).orElse(null);
        String nome = login;
        if (usuario != null) {
            nome = usuario.getNome();
        }
        return Map.of(
                "usuario", login,
                "nome", nome,
                "administrador", request.isUserInRole("ADMIN"),
                "csrfToken", csrfToken.getToken()
        );
    }
}
