package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Pedido;
import br.com.edhi.painel.model.Venda;
import br.com.edhi.painel.model.dto.PedidoRequest;
import br.com.edhi.painel.model.enums.ItemConferencia;
import br.com.edhi.painel.model.enums.StatusPedido;
import br.com.edhi.painel.repository.PedidoRepository;
import br.com.edhi.painel.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository repository;
    private final VendaRepository vendaRepository;

    public PedidoService(PedidoRepository repository, VendaRepository vendaRepository) {
        this.repository = repository;
        this.vendaRepository = vendaRepository;
    }

    /** Em aberto primeiro, por prazo mais próximo; sem prazo no fim. Entregues por último. */
    public List<Pedido> listar() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing((Pedido p) -> !p.isEmAberto())
                        .thenComparing(Pedido::getPrazo, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Pedido::getId))
                .toList();
    }

    private Pedido buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Pedido não encontrado."));
    }

    @Transactional
    public Pedido criar(PedidoRequest req) {
        Pedido pedido = new Pedido();
        preencher(pedido, req);
        return repository.save(pedido);
    }

    /**
     * Avança para o próximo estado, como o botão de estado do painel.
     * Só pode virar "Entregue" com medida, estampa e cheiro conferidos.
     */
    @Transactional
    public Pedido avancar(Long id) {
        Pedido pedido = buscar(id);
        if (pedido.isLancado()) {
            throw RegraNegocioException.conflito("Este pedido já virou venda e não muda mais de estado.");
        }
        StatusPedido proximo = pedido.getStatus().proximo();
        if (proximo == StatusPedido.ENTREGUE && !pedido.isConferenciaCompleta()) {
            throw RegraNegocioException.conflito("Antes de entregar, confira medida, estampa e cheiro.");
        }
        pedido.setStatus(proximo);
        return repository.save(pedido);
    }

    /** Marca ou desmarca um item da conferência (medida, estampa ou cheiro). */
    @Transactional
    public Pedido alternarConferencia(Long id, ItemConferencia item) {
        Pedido pedido = buscar(id);
        if (pedido.getStatus() == StatusPedido.ENTREGUE) {
            throw RegraNegocioException.conflito("O pedido já foi entregue.");
        }
        pedido.setConferido(item, !pedido.isConferido(item));
        return repository.save(pedido);
    }

    /** Cria a venda de um pedido entregue, com a data de hoje. Cada pedido só vira venda uma vez. */
    @Transactional
    public Venda lancarComoVenda(Long id) {
        Pedido pedido = buscar(id);
        if (pedido.getStatus() != StatusPedido.ENTREGUE) {
            throw RegraNegocioException.conflito("Só dá para lançar como venda um pedido entregue.");
        }
        if (pedido.isLancado()) {
            throw RegraNegocioException.conflito("Este pedido já foi lançado como venda.");
        }
        Venda venda = new Venda();
        venda.setData(LocalDate.now());
        venda.setCliente(pedido.getCliente());
        venda.setProduto(pedido.getPeca());
        venda.setQuantidade(pedido.getQuantidade());
        venda.setValor(pedido.getValor());
        venda.setCanal(pedido.getCanal());
        venda.setPedido(pedido);
        pedido.setLancado(true);
        repository.save(pedido);
        return vendaRepository.save(venda);
    }

    /** Apaga o pedido. Se ele já virou venda, a venda continua lançada. */
    @Transactional
    public void excluir(Long id) {
        Pedido pedido = buscar(id);
        for (Venda v : vendaRepository.findByPedido(pedido)) {
            v.setPedido(null);
        }
        repository.delete(pedido);
    }

    private void preencher(Pedido pedido, PedidoRequest req) {
        Validacao.obrigatorio(req, "pedido");
        pedido.setCliente(Validacao.texto(req.cliente(), "cliente"));
        pedido.setPeca(Validacao.obrigatorio(req.peca(), "peça"));
        pedido.setQuantidade(Validacao.quantidade(req.quantidade()));
        pedido.setValor(Validacao.naoNegativo(req.valor(), "valor"));
        pedido.setCanal(Validacao.obrigatorio(req.canal(), "canal"));
        pedido.setPrazo(req.prazo());
    }
}
