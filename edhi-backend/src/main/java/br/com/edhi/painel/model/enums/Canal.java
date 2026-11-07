package br.com.edhi.painel.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Canal implements ComRotulo {
    FEIRA("Feira"),
    WHATSAPP_INSTAGRAM("WhatsApp e Instagram"),
    EMPRESA_BRINDE("Empresa e brinde");

    private final String rotulo;

    Canal(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    @JsonCreator
    public static Canal de(String texto) {
        return ComRotulo.porTexto(Canal.class, texto);
    }
}
