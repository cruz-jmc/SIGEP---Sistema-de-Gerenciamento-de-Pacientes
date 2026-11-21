
package br.edu.upe.sigep.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class TokenServiceTest {

    private TokenService tokenService;

    @BeforeEach
    void preparar() {
        tokenService = new TokenService();
    }

    @Test
    void deveGerarTokenNaoVazio() {

        String token = tokenService.gerarToken();

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void deveGerarTokensDiferentes() {

        String primeiro = tokenService.gerarToken();
        String segundo = tokenService.gerarToken();

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveGerarHashCom64Caracteres() {

        String hash = tokenService.gerarHash("token-de-teste");

        assertEquals(64, hash.length());
    }

    @Test
    void deveGerarMesmoHashParaMesmoToken() {

        String primeiro = tokenService.gerarHash("abc123");
        String segundo = tokenService.gerarHash("abc123");

        assertEquals(primeiro, segundo);
    }

    @Test
    void deveGerarHashesDiferentesParaTokensDiferentes() {

        String primeiro = tokenService.gerarHash("token-A");
        String segundo = tokenService.gerarHash("token-B");

        assertNotEquals(primeiro, segundo);
    }

    @Test
    void deveGerarTokenBase64UrlSafeDe32Bytes() {
        String token = tokenService.gerarToken();

        assertEquals(43, token.length());
        assertTrue(token.matches("[A-Za-z0-9_-]+"));
        assertEquals(32, Base64.getUrlDecoder().decode(token).length);
    }

    @Test
    void deveGerarSha256DeValoresConhecidosEmUtf8() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                tokenService.gerarHash("abc")
        );
        assertEquals(
                "0664077f33cc3ebbaa4bbdacac0eb70e740983080f01dce29929e73b7785a7ad",
                tokenService.gerarHash("ação")
        );
    }
}
