package br.com.edhi.painel.controller;

import br.com.edhi.painel.model.dto.BackupDTO;
import br.com.edhi.painel.service.BackupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Botões "Baixar backup", "Restaurar backup", "Carregar exemplo" e "Apagar tudo". */
@RestController
@RequestMapping("/api")
public class PainelController {

    private final BackupService backupService;

    public PainelController(BackupService backupService) {
        this.backupService = backupService;
    }

    @GetMapping("/backup")
    public BackupDTO exportar() {
        return backupService.exportar();
    }

    /** Substitui todos os dados pelos do backup. */
    @PostMapping("/backup")
    public Map<String, Integer> importar(@RequestBody BackupDTO backup) {
        return backupService.importar(backup);
    }

    @DeleteMapping("/dados")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagarTudo() {
        backupService.apagarTudo();
    }
}
