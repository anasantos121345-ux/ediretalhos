package br.com.edhi.painel.repository;

import br.com.edhi.painel.model.Pedido;
import br.com.edhi.painel.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findAllByOrderByDataDescIdDesc();

    List<Venda> findByPedido(Pedido pedido);
}
