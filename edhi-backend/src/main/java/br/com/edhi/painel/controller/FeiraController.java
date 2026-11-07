package br.com.edhi.painel.controller;

import br.com.edhi.painel.model.Feira;
import br.com.edhi.painel.model.dto.FeiraRequest;
import br.com.edhi.painel.service.FeiraService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feiras")
public class FeiraController {

    private final FeiraService service;

    public FeiraController(FeiraService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Feira criar(@RequestBody FeiraRequest req) {
        return service.criar(req);
    }

    /** Já vem ordenada da feira que mais vale a pena para a que menos vale. */
    @GetMapping
    public List<Feira> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
