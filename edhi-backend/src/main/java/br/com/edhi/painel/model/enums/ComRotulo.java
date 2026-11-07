package br.com.edhi.painel.model.enums;

import java.text.Normalizer;

/**
 * Enum com um texto de exibição (o mesmo que o painel mostra).
 * O JSON usa o rótulo ("WhatsApp e Instagram"), mas também aceita o nome
 * da constante ("WHATSAPP_INSTAGRAM"), sem diferenciar maiúsculas nem acentos.
 */
public interface ComRotulo {

    String getRotulo();

    static <E extends Enum<E> & ComRotulo> E porTexto(Class<E> tipo, String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String alvo = normalizar(texto);
        for (E e : tipo.getEnumConstants()) {
            if (normalizar(e.getRotulo()).equals(alvo) || normalizar(e.name()).equals(alvo)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Valor inválido: " + texto);
    }

    private static String normalizar(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "")
                .toLowerCase();
    }
}
