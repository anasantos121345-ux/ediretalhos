package br.com.edhi.painel.repository;

import br.com.edhi.painel.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}
