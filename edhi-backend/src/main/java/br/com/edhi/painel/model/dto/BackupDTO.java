package br.com.edhi.painel.model.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mesmo formato do arquivo "Baixar backup" do painel HTML
 * (backup-painel-edhi-AAAA-MM-DD.json). Um backup feito no navegador
 * pode ser enviado para a API, e o da API pode ser restaurado no navegador.
 */
public record BackupDTO(List<VendaBackup> sales, List<PedidoBackup> orders, List<FeiraBackup> fairs) {

    public record VendaBackup(String id, String date, String cliente, String produto,
                              Integer qtd, BigDecimal valor, String canal) {
    }

    /** status vai de 0 (Novo) a 3 (Entregue); checks = [medida, estampa, cheiro]. */
    public record PedidoBackup(String id, String cliente, String peca, Integer qtd, BigDecimal valor,
                               String canal, String prazo, Integer status, List<Boolean> checks,
                               Boolean launched) {
    }

    public record FeiraBackup(String id, String nome, String data, BigDecimal custo, BigDecimal vendas) {
    }
}
