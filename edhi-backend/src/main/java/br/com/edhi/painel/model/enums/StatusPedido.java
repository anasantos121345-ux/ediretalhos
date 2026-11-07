package br.com.edhi.painel.model.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/** Estados do pedido, na ordem em que ele avança. */
public enum StatusPedido implements ComRotulo {
    NOVO("Novo"),
    EM_PRODUCAO("Em produção"),
    PRONTO_PARA_CONFERIR("Pronto para conferir"),
    ENTREGUE("Entregue");

    private final String rotulo;

    StatusPedido(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    @JsonValue
    public String getRotulo() {
        return rotulo;
    }

    /** Próximo estado. Depois de "Entregue" volta para "Novo", como no painel. */
    public StatusPedido proximo() {
        StatusPedido[] todos = values();
        return todos[(ordinal() + 1) % todos.length];
    }
}
