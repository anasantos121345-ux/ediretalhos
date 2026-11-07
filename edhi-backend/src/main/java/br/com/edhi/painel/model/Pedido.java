package br.com.edhi.painel.model;

import br.com.edhi.painel.model.enums.Canal;
import br.com.edhi.painel.model.enums.ItemConferencia;
import br.com.edhi.painel.model.enums.Produto;
import br.com.edhi.painel.model.enums.StatusPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tb_pedido")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Produto peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Canal canal;

    private LocalDate prazo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status = StatusPedido.NOVO;

    private boolean medidaConferida;
    private boolean estampaConferida;
    private boolean cheiroConferido;

    /** Já virou venda? Evita lançar o mesmo pedido duas vezes. */
    private boolean lancado;

    public Pedido() {
    }

    public boolean isConferido(ItemConferencia item) {
        return switch (item) {
            case MEDIDA -> medidaConferida;
            case ESTAMPA -> estampaConferida;
            case CHEIRO -> cheiroConferido;
        };
    }

    public void setConferido(ItemConferencia item, boolean valor) {
        switch (item) {
            case MEDIDA -> medidaConferida = valor;
            case ESTAMPA -> estampaConferida = valor;
            case CHEIRO -> cheiroConferido = valor;
        }
    }

    public boolean isConferenciaCompleta() {
        return medidaConferida && estampaConferida && cheiroConferido;
    }

    public boolean isEmAberto() {
        return status != StatusPedido.ENTREGUE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public Produto getPeca() { return peca; }
    public void setPeca(Produto peca) { this.peca = peca; }

    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public StatusPedido getStatus() { return status; }
    public void setStatus(StatusPedido status) { this.status = status; }

    public boolean isMedidaConferida() { return medidaConferida; }
    public void setMedidaConferida(boolean medidaConferida) { this.medidaConferida = medidaConferida; }

    public boolean isEstampaConferida() { return estampaConferida; }
    public void setEstampaConferida(boolean estampaConferida) { this.estampaConferida = estampaConferida; }

    public boolean isCheiroConferido() { return cheiroConferido; }
    public void setCheiroConferido(boolean cheiroConferido) { this.cheiroConferido = cheiroConferido; }

    public boolean isLancado() { return lancado; }
    public void setLancado(boolean lancado) { this.lancado = lancado; }
}
