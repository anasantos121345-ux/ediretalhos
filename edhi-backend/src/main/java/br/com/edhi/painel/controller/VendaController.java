package br.com.edhi.painel.controller;

import br.com.edhi.painel.model.Venda;
import br.com.edhi.painel.model.dto.VendaRequest;
import br.com.edhi.painel.service.VendaService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Venda criar(@RequestBody VendaRequest req) {
        return service.criar(req);
    }

    @GetMapping
    public List<Venda> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
