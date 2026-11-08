package io.github.yasudakenji.support.controller;

import com.jayway.jsonpath.JsonPath;
import io.github.yasudakenji.support.model.Usuario;
import io.github.yasudakenji.support.model.enums.Perfil;
import io.github.yasudakenji.support.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "SUPPORT_ADMIN_PASSWORD=senha-admin-de-teste",
        "SUPPORT_USER_PASSWORD=senha-usuario-de-teste"
})
@AutoConfigureMockMvc
@Transactional
class ContaControllerIntegrationTest {

    private static final String ADMIN = basic("admin", "senha-admin-de-teste");
    private static final String USUARIO = basic("usuario", "senha-usuario-de-teste");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void exigeAutenticacaoParaConsultar() throws Exception {
        mvc.perform(get("/equipamento/listar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioConsultaMasNaoCriaSetorMesmoComCsrfValido() throws Exception {
        mvc.perform(get("/equipamento/listar").header("Authorization", USUARIO))
                .andExpect(status().isOk());
        MvcResult conta = mvc.perform(get("/conta/atual").header("Authorization", USUARIO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrador").value(false))
                .andReturn();

        criarSetor(USUARIO, conta, 403);
    }

    @Test
    void adminCriaSetorComCsrfValido() throws Exception {
        MvcResult conta = mvc.perform(get("/conta/atual").header("Authorization", ADMIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.administrador").value(true))
                .andReturn();

        mvc.perform(get("/equipamento/listar").header("Authorization", ADMIN)
                        .cookie(conta.getResponse().getCookie("XSRF-TOKEN")))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("XSRF-TOKEN"));

        criarSetor(ADMIN, conta, 200);
    }

    @Test
    void adminNaoGravaSemTokenCsrf() throws Exception {
        mvc.perform(post("/setor/salvar").header("Authorization", ADMIN)
                        .contentType("application/json").content("{\"nome\":\"Setor de teste\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @EnumSource(Perfil.class)
    void adminCadastraUsuarioEContaEntraComPerfilEscolhido(Perfil perfil) throws Exception {
        String email = "cadastro@support.test";
        String senha = "senha-nova-de-teste";
        MvcResult contaAdmin = obterConta(ADMIN);
        cadastrarUsuario(ADMIN, contaAdmin, email, perfil, 200);

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertNotNull(usuario.getId());
        assertEquals("Conta cadastrada", usuario.getNome());
        assertEquals(email, usuario.getEmail());
        assertEquals(perfil, usuario.getPerfil());
        assertNotEquals(senha, usuario.getSenhaHash());
        assertTrue(passwordEncoder.matches(senha, usuario.getSenhaHash()));

        boolean administrador = perfil == Perfil.ADMIN;
        String autorizacao = basic("CADASTRO@support.test", senha);
        MvcResult contaNova = mvc.perform(get("/conta/atual").header("Authorization", autorizacao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Conta cadastrada"))
                .andExpect(jsonPath("$.administrador").value(administrador))
                .andExpect(jsonPath("$.senhaHash").doesNotExist())
                .andReturn();
        criarSetor(autorizacao, contaNova, administrador ? 200 : 403);
    }

    @Test
    void usuarioComumNaoCadastraConta() throws Exception {
        cadastrarUsuario(USUARIO, obterConta(USUARIO), "bloqueado@support.test", Perfil.ADMIN, 403);
    }

    @Test
    void rejeitaEmailDuplicadoMesmoComMaiusculas() throws Exception {
        MvcResult contaAdmin = obterConta(ADMIN);
        cadastrarUsuario(ADMIN, contaAdmin, "duplicado@support.test", Perfil.USUARIO, 200);
        cadastrarUsuario(ADMIN, contaAdmin, "DUPLICADO@support.test", Perfil.ADMIN, 409);
    }

    private MvcResult obterConta(String autorizacao) throws Exception {
        return mvc.perform(get("/conta/atual").header("Authorization", autorizacao))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void cadastrarUsuario(String autorizacao, MvcResult conta, String email, Perfil perfil,
            int statusEsperado) throws Exception {
        String dados = """
                {"nome":"Conta cadastrada","email":"%s","senha":"senha-nova-de-teste","perfil":"%s"}
                """.formatted(email, perfil);
        enviarPost("/usuario/salvar", autorizacao, conta, dados, statusEsperado);
    }

    private void criarSetor(String autorizacao, MvcResult conta, int statusEsperado) throws Exception {
        enviarPost("/setor/salvar", autorizacao, conta, "{\"nome\":\"Setor de teste\"}", statusEsperado);
    }

    private void enviarPost(String rota, String autorizacao, MvcResult conta, String dados, int statusEsperado)
            throws Exception {
        String token = JsonPath.read(conta.getResponse().getContentAsString(), "$.csrfToken");
        Cookie cookie = conta.getResponse().getCookie("XSRF-TOKEN");
        assertNotNull(cookie);

        mvc.perform(post(rota).header("Authorization", autorizacao)
                        .cookie(cookie).header("X-XSRF-TOKEN", token)
                        .contentType("application/json").content(dados))
                .andExpect(status().is(statusEsperado));
    }

    private static String basic(String usuario, String senha) {
        byte[] credencial = (usuario + ":" + senha).getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(credencial);
    }
}
