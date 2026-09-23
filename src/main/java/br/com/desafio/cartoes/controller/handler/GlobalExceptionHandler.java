package br.com.desafio.cartoes.controller.handler;

import br.com.desafio.cartoes.controller.dto.ErroResponse;
import br.com.desafio.cartoes.controller.exception.RequisicaoInvalidaException;
import br.com.desafio.cartoes.domain.exception.RegraNegocioException;
import br.com.desafio.cartoes.domain.exception.TipoErro;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

/*
 * Único ponto de conversão de exceção em resposta HTTP: toda resposta de erro
 * da API sai daqui, sempre no payload {codigo, mensagem, detalhe_erro}.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String MENSAGEM_REGRA_NEGOCIO = "Regra de negócio não atendida.";
    private static final String MENSAGEM_REQUISICAO_INVALIDA = "Requisição inválida.";
    private static final String MENSAGEM_INTERNA_FORMATO_NAO_SUPORTADO = "A API aceita apenas application/json.";
    private static final String MENSAGEM_INTERNA_CORPO_INVALIDO =
            "O corpo da requisição está ausente ou não pôde ser interpretado como JSON.";
    private static final String MENSAGEM_INTERNA_REQUISICAO_NAO_TRATADA = "A API não conseguiu atender a essa requisição.";
    private static final String MENSAGEM_ERRO_INTERNO = "Um erro inesperado ocorreu.";
    private static final String MENSAGEM_INTERNA_ERRO_INTERNO =
            "Tivemos um problema, mas fique tranquilo que nosso time já foi avisado.";

    // FieldError.getField() devolve o nome do atributo Java (ex.: dataNascimento);
    // o contrato usa snake_case. Só os três campos abaixo divergem — os demais já
    // são iguais nos dois lados e não precisam de entrada no mapa.
    private static final Map<String, String> NOME_DO_CAMPO_NO_CONTRATO = Map.of(
            "dataNascimento", "data_nascimento",
            "rendaMensal", "renda_mensal",
            "telefoneWhatsapp", "telefone_whatsapp"
    );

    private final String nomeAplicacao;

    public GlobalExceptionHandler(@Value("${spring.application.name}") String nomeAplicacao) {
        this.nomeAplicacao = nomeAplicacao;
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException excecao) {
        log.warn("Regra de negócio não atendida: tipoErro={} status={}", excecao.tipoErro(), HttpStatus.UNPROCESSABLE_ENTITY.value());
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, MENSAGEM_REGRA_NEGOCIO, excecao.tipoErro(), excecao.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(RequisicaoInvalidaException excecao) {
        log.warn("Requisição inválida: status={}", HttpStatus.BAD_REQUEST.value());
        return responder(HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA, TipoErro.ERRO_VALIDACAO, excecao.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Content-Type não suportado: status={}", status.value());
        return responderObjeto(headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.FORMATO_NAO_SUPORTADO, MENSAGEM_INTERNA_FORMATO_NAO_SUPORTADO);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Corpo da requisição inválido: status={}", status.value());
        // Nunca ecoar excecao.getMessage(): a mensagem crua do Jackson pode conter
        // trechos do corpo enviado, inclusive dados do cliente.
        return responderObjeto(headers, HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, MENSAGEM_INTERNA_CORPO_INVALIDO);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("Validação de entrada falhou: status={} violacoes={}", status.value(), excecao.getBindingResult().getErrorCount());

        String mensagemInterna = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> nomeDoCampoNoContrato(erro.getField()) + ": " + erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        return responderObjeto(headers, HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, mensagemInterna);
    }

    // Cobre qualquer exceção que a superclasse resolveria com seu próprio corpo
    // padrão (ProblemDetail) — inclusive 404 (rota inexistente) e 405 (método não
    // suportado) — e a converte para o payload desta API. Mensagem interna fixa,
    // nunca a mensagem da exceção: este método é a rede de segurança para
    // exceções que ainda não têm tratamento dedicado, então não é possível
    // garantir de antemão que a mensagem delas nunca carregue dado do cliente.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excecao, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        log.warn("Requisição rejeitada: status={} excecao={}", status.value(), excecao.getClass().getSimpleName());
        return responderObjeto(headers, status, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, MENSAGEM_INTERNA_REQUISICAO_NAO_TRATADA);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception excecao) {
        log.error("Erro inesperado ao processar a requisição.", excecao);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO, TipoErro.ERRO_INTERNO, MENSAGEM_INTERNA_ERRO_INTERNO);
    }

    private ResponseEntity<ErroResponse> responder(HttpStatus status, String mensagem, TipoErro tipoErro, String mensagemInterna) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErroResponse.de(status, mensagem, nomeAplicacao, tipoErro, mensagemInterna));
    }

    private ResponseEntity<Object> responderObjeto(HttpHeaders headers, HttpStatus status, String mensagem,
                                                     TipoErro tipoErro, String mensagemInterna) {
        return ResponseEntity.status(status)
                .headers(headers)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErroResponse.de(status, mensagem, nomeAplicacao, tipoErro, mensagemInterna));
    }

    private static String nomeDoCampoNoContrato(String caminho) {
        String atributo = caminho.contains(".") ? caminho.substring(caminho.lastIndexOf('.') + 1) : caminho;
        return NOME_DO_CAMPO_NO_CONTRATO.getOrDefault(atributo, atributo);
    }
}
