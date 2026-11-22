package br.edu.upe.sigep.validation;

import java.nio.charset.StandardCharsets;

public final class PoliticaSenha {

    private PoliticaSenha() {
    }

    public static boolean valida(String senha) {
        return senha != null && !senha.isBlank()
                && senha.codePointCount(0, senha.length()) >= 12
                && cabeNoBCrypt(senha);
    }

    public static boolean cabeNoBCrypt(String senha) {
        return senha.getBytes(StandardCharsets.UTF_8).length <= 72;
    }
}
