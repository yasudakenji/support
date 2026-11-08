package io.github.yasudakenji.support.config;

import io.github.yasudakenji.support.model.Usuario;
import io.github.yasudakenji.support.repository.UsuarioRepository;
import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
public class SegurancaConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository tokens = new CookieCsrfTokenRepository();
        tokens.setCookieCustomizer(cookie -> cookie.sameSite("Strict"));

        http.authorizeHttpRequests(regras -> regras
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "USUARIO")
                .requestMatchers(HttpMethod.HEAD, "/**").hasAnyRole("ADMIN", "USUARIO")
                .anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // O Basic reautentica a cada requisição. Preserve o token CSRF entre elas.
                .csrf(csrf -> csrf.csrfTokenRepository(tokens)
                        .sessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy()))
                .logout(logout -> logout.disable());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService usuarios(PasswordEncoder encoder, UsuarioRepository usuarioRepository,
            @Value("${SUPPORT_ADMIN_PASSWORD:}") String senhaAdmin,
            @Value("${SUPPORT_USER_PASSWORD:}") String senhaUsuario) {
        InMemoryUserDetailsManager usuarios = new InMemoryUserDetailsManager();
        if (!senhaAdmin.isBlank()) {
            usuarios.createUser(User.withUsername("admin").password(encoder.encode(senhaAdmin)).roles("ADMIN").build());
        }
        if (!senhaUsuario.isBlank()) {
            usuarios.createUser(User.withUsername("usuario").password(encoder.encode(senhaUsuario)).roles("USUARIO").build());
        }
        return login -> {
            if (usuarios.userExists(login)) {
                return usuarios.loadUserByUsername(login);
            }

            Usuario usuario = usuarioRepository.findByEmailIgnoreCase(login).orElse(null);
            if (usuario == null) {
                throw new UsernameNotFoundException("Usuário não encontrado");
            }
            return User.withUsername(usuario.getEmail())
                    .password(usuario.getSenhaHash())
                    .roles(usuario.getPerfil().name())
                    .build();
        };
    }
}
