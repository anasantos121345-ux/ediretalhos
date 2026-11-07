package br.com.edhi.painel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/** Transforma os erros em JSON { "mensagem": "..." }. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> regra(RegraNegocioException e) {
        return corpo(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> jsonInvalido(HttpMessageNotReadableException e) {
        return corpo(HttpStatus.BAD_REQUEST,
                "Dados inválidos. Confira os campos, as datas (AAAA-MM-DD) e os nomes de peça e canal.");
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> parametroInvalido(Exception e) {
        String msg = e instanceof IllegalArgumentException && e.getMessage() != null
                ? e.getMessage() : "Parâmetro inválido.";
        return corpo(HttpStatus.BAD_REQUEST, msg);
    }

    private ResponseEntity<Map<String, String>> corpo(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of("mensagem", mensagem));
    }
}
