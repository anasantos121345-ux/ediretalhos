package br.com.edhi.painel.service;

import br.com.edhi.painel.exception.RegraNegocioException;
import br.com.edhi.painel.model.Feira;
import br.com.edhi.painel.model.dto.FeiraRequest;
import br.com.edhi.painel.repository.FeiraRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Service
public class FeiraService {

    private final FeiraRepository repository;

    public FeiraService(FeiraRepository repository) {
        this.repository = repository;
    }

    /** Ordenadas pelo retorno, da que mais vale a pena para a que menos vale. Sem custo vão para o fim. */
    public List<Feira> listar() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Feira::getRetorno,
                        Comparator.nullsLast(Comparator.<BigDecimal>reverseOrder())))
                .toList();
    }

    /** Custo e vendas em branco contam como zero. */
    @Transactional
    public Feira criar(FeiraRequest req) {
        Validacao.obrigatorio(req, "feira");
        Feira feira = new Feira();
        feira.setNome(Validacao.texto(req.nome(), "nome"));
        feira.setData(req.data());
        feira.setCusto(Validacao.naoNegativo(req.custo() != null ? req.custo() : BigDecimal.ZERO, "custo da vaga"));
        feira.setVendas(Validacao.naoNegativo(req.vendas() != null ? req.vendas() : BigDecimal.ZERO, "vendas"));
        return repository.save(feira);
    }

    @Transactional
    public void excluir(Long id) {
        Feira feira = repository.findById(id)
                .orElseThrow(() -> RegraNegocioException.naoEncontrado("Feira não encontrada."));
        repository.delete(feira);
    }
}
