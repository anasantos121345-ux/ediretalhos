package br.com.edhi.painel.controller;

import br.com.edhi.painel.model.Pedido;
import br.com.edhi.painel.model.Venda;
import br.com.edhi.painel.model.dto.PedidoRequest;
import br.com.edhi.painel.model.enums.ItemConferencia;
import br.com.edhi.painel.service.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido criar(@RequestBody PedidoRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<Pedido> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }

    /** Avança o estado: Novo → Em produção → Pronto para conferir → Entregue. */
    @PatchMapping("/{id}/avancar")
    public Pedido avancar(@PathVariable Long id) {
        return service.avancar(id);
    }

    /** Marca ou desmarca um item: /api/pedidos/5/conferencia/medida (ou estampa, cheiro). */
    @PatchMapping("/{id}/conferencia/{item}")
    public Pedido conferir(@PathVariable Long id, @PathVariable String item) {
        return service.alternarConferencia(id, ItemConferencia.de(item));
    }

    /** Cria a venda de um pedido entregue. */
    @PostMapping("/{id}/lancar-venda")
    @ResponseStatus(HttpStatus.CREATED)
    public Venda lancarVenda(@PathVariable Long id) {
        return service.lancarComoVenda(id);
    }
}
