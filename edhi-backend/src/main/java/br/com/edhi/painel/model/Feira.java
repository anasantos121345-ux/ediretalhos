package br.com.edhi.painel.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@Table(name = "tb_feira")
public class Feira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private LocalDate data;

    /** Quanto custou a vaga. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal custo;

    /** Quanto foi vendido na feira. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal vendas;

    public Feira() {
    }

    /**
     * Quantos reais voltaram para cada real pago na vaga (vendas ÷ custo).
     * Nulo quando a vaga foi de graça.
     */
    @Transient
    public BigDecimal getRetorno() {
        if (custo == null || vendas == null || custo.signum() == 0) {
            return null;
        }
        return vendas.divide(custo, 2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public BigDecimal getCusto() { return custo; }
    public void setCusto(BigDecimal custo) { this.custo = custo; }

    public BigDecimal getVendas() { return vendas; }
    public void setVendas(BigDecimal vendas) { this.vendas = vendas; }
}
