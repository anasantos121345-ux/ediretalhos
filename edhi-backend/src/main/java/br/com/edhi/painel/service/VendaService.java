package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Venda;
import br.com.edhi.painel.model.dto.VendaRequest;
import br.com.edhi.painel.repository.VendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class VendaService {

    private final VendaRepository repository;

    public VendaService(VendaRepository repository) {
        this.repository = repository;
    }

    /** Vendas mais recentes primeiro. */
    public List<Venda> listar() {
        return repository.findAllByOrderByDataDescIdDesc();
    }

    /** Sem data, a venda fica com a data de hoje. */
    @Transactional
    public Venda criar(VendaRequest req) {
        Validacao.obrigatorio(req, "venda");
        Venda venda = new Venda();
        venda.setData(req.data() != null ? req.data() : LocalDate.now());
        venda.setCliente(Validacao.texto(req.cliente(), "cliente"));
        venda.setProduto(Validacao.obrigatorio(req.produto(), "peça"));
        venda.setQuantidade(Validacao.quantidade(req.quantidade()));
        venda.setValor(Validacao.naoNegativo(req.valor(), "valor"));
        venda.setCanal(Validacao.obrigatorio(req.canal(), "canal"));
        return repository.save(venda);
    }

    @Transactional
    public void excluir(Long id) {
        Venda venda = repository.findById(id)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Venda não encontrada."));
        repository.delete(venda);
    }
}
