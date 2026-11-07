package br.com.edhi.painel.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Produto implements ComRotulo {
    ECOBAG("Ecobag"),
    TAPETE("Tapete"),
    COLCHA("Colcha"),
    PANO_DE_COPA("Pano de copa"),
    OUTRO("Outro");

    private final String rotulo;

    Produto(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static Produto de(String texto) {
        return ComRotulo.porTexto(Produto.class, texto);
    }
}
