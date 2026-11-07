package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Feira;
import br.com.edhi.painel.model.Pedido;
import br.com.edhi.painel.model.Venda;
import br.com.edhi.painel.model.dto.BackupDTO;
import br.com.edhi.painel.model.dto.BackupDTO.FeiraBackup;
import br.com.edhi.painel.model.dto.BackupDTO.PedidoBackup;
import br.com.edhi.painel.model.dto.BackupDTO.VendaBackup;
import br.com.edhi.painel.model.enums.Canal;
import br.com.edhi.painel.model.enums.ItemConferencia;
import br.com.edhi.painel.model.enums.Produto;
import br.com.edhi.painel.model.enums.StatusPedido;
import br.com.edhi.painel.repository.FeiraRepository;
import br.com.edhi.painel.repository.PedidoRepository;
import br.com.edhi.painel.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/** Exporta, importa e apaga todos os dados, no formato do backup do painel HTML. */
@Service
public class BackupService {

    private final VendaRepository vendaRepository;
    private final PedidoRepository pedidoRepository;
    private final FeiraRepository feiraRepository;

    public BackupService(VendaRepository vendaRepository, PedidoRepository pedidoRepository,
                         FeiraRepository feiraRepository) {
        this.vendaRepository = vendaRepository;
        this.pedidoRepository = pedidoRepository;
        this.feiraRepository = feiraRepository;
    }

    public BackupDTO exportar() {
        List<VendaBackup> vendas = vendaRepository.findAll().stream()
                .map(v -> new VendaBackup(String.valueOf(v.getId()), v.getData().toString(), v.getCliente(),
                        v.getProduto().getRotulo(), v.getQuantidade(), v.getValor(), v.getCanal().getRotulo()))
                .toList();
        List<PedidoBackup> pedidos = pedidoRepository.findAll().stream()
                .map(p -> new PedidoBackup(String.valueOf(p.getId()), p.getCliente(), p.getPeca().getRotulo(),
                        p.getQuantidade(), p.getValor(), p.getCanal().getRotulo(),
                        p.getPrazo() == null ? "" : p.getPrazo().toString(),
                        p.getStatus().ordinal(),
                        List.of(p.isMedidaConferida(), p.isEstampaConferida(), p.isCheiroConferido()),
                        p.isLancado()))
                .toList();
        List<FeiraBackup> feiras = feiraRepository.findAll().stream()
                .map(f -> new FeiraBackup(String.valueOf(f.getId()), f.getNome(),
                        f.getData() == null ? "" : f.getData().toString(), f.getCusto(), f.getVendas()))
                .toList();
        return new BackupDTO(vendas, pedidos, feiras);
    }

    /** Substitui tudo pelo conteúdo do backup, como o "Restaurar backup" do painel. */
    @Transactional
    public Map<String, Integer> importar(BackupDTO backup) {
        if (backup == null) {
            throw RegraNegocioException.invalido("Este arquivo não é um backup válido.");
        }
        List<Venda> vendas = nulo(backup.sales()).stream().map(this::venda).toList();
        List<Pedido> pedidos = nulo(backup.orders()).stream().map(this::pedido).toList();
        List<Feira> feiras = nulo(backup.fairs()).stream().map(this::feira).toList();

        apagarTudo();
        vendaRepository.saveAll(vendas);
        pedidoRepository.saveAll(pedidos);
        feiraRepository.saveAll(feiras);
        return Map.of("vendas", vendas.size(), "pedidos", pedidos.size(), "feiras", feiras.size());
    }

    @Transactional
    public void apagarTudo() {
        vendaRepository.deleteAllInBatch();
        pedidoRepository.deleteAllInBatch();
        feiraRepository.deleteAllInBatch();
    }

    private Venda venda(VendaBackup b) {
        Venda v = new Venda();
        v.setData(data(b.date()) != null ? data(b.date()) : LocalDate.now());
        v.setCliente(Validacao.texto(b.cliente(), "cliente"));
        v.setProduto(Validacao.obrigatorio(Produto.de(b.produto()), "peça"));
        v.setQuantidade(Validacao.quantidade(b.qtd()));
        v.setValor(Validacao.naoNegativo(b.valor(), "valor"));
        v.setCanal(Validacao.obrigatorio(Canal.de(b.canal()), "canal"));
        return v;
    }

    private Pedido pedido(PedidoBackup b) {
        Pedido p = new Pedido();
        p.setCliente(Validacao.texto(b.cliente(), "cliente"));
        p.setPeca(Validacao.obrigatorio(Produto.de(b.peca()), "peça"));
        p.setQuantidade(Validacao.quantidade(b.qtd()));
        p.setValor(Validacao.naoNegativo(b.valor(), "valor"));
        p.setCanal(Validacao.obrigatorio(Canal.de(b.canal()), "canal"));
        p.setPrazo(data(b.prazo()));
        int s = b.status() == null ? 0 : b.status();
        StatusPedido[] todos = StatusPedido.values();
        p.setStatus(todos[Math.max(0, Math.min(s, todos.length - 1))]);
        List<Boolean> checks = nulo(b.checks());
        ItemConferencia[] itens = ItemConferencia.values();
        for (int i = 0; i < itens.length && i < checks.size(); i++) {
            p.setConferido(itens[i], Boolean.TRUE.equals(checks.get(i)));
        }
        p.setLancado(Boolean.TRUE.equals(b.launched()));
        return p;
    }

    private Feira feira(FeiraBackup b) {
        Feira f = new Feira();
        f.setNome(Validacao.texto(b.nome(), "nome da feira"));
        f.setData(data(b.data()));
        f.setCusto(Validacao.naoNegativo(b.custo() != null ? b.custo() : BigDecimal.ZERO, "custo da vaga"));
        f.setVendas(Validacao.naoNegativo(b.vendas() != null ? b.vendas() : BigDecimal.ZERO, "vendas"));
        return f;
    }

    private static LocalDate data(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw RegraNegocioException.invalido("Data inválida no backup: " + s);
        }
    }

    private static <T> List<T> nulo(List<T> lista) {
        return lista == null ? List.of() : lista;
    }
}
