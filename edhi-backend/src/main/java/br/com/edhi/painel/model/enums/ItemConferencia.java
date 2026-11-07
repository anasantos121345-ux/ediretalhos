package br.com.edhi.painel.model.enums;

/** O que a Edilene confere antes de entregar um pedido. */
public enum ItemConferencia implements ComRotulo {
    MEDIDA("Medida"),
    ESTAMPA("Estampa"),
    CHEIRO("Cheiro");

    private final String rotulo;

    ItemConferencia(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String getRotulo() {
        return rotulo;
    }

    /** Aceita "medida", "Medida" ou "MEDIDA", vindo da URL. */
    public static ItemConferencia de(String texto) {
        return ComRotulo.porTexto(ItemConferencia.class, texto);
    }
}
