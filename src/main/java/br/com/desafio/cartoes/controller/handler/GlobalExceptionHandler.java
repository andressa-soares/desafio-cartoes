package br.com.desafio.cartoes.controller.handler;

import br.com.desafio.cartoes.controller.dto.ErroResponse;
import br.com.desafio.cartoes.controller.exception.RequisicaoInvalidaException;
import br.com.desafio.cartoes.domain.exception.RegraNegocioException;
import br.com.desafio.cartoes.domain.exception.TipoErro;
import jakarta.servlet.http.HttpServletRequest;
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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

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
    public ResponseEntity<ErroResponse> tratarRegraNegocio(RegraNegocioException excecao, HttpServletRequest requisicao) {
        log.warn("evento=regra_negocio_violada excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), requisicao.getMethod(), requisicao.getRequestURI(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(), excecao.tipoErro());
        return responder(HttpStatus.UNPROCESSABLE_ENTITY, MENSAGEM_REGRA_NEGOCIO, excecao.tipoErro(), excecao.getMessage());
    }

    @ExceptionHandler(RequisicaoInvalidaException.class)
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(RequisicaoInvalidaException excecao, HttpServletRequest requisicao) {
        log.warn("evento=requisicao_invalida excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), requisicao.getMethod(), requisicao.getRequestURI(),
                HttpStatus.BAD_REQUEST.value(), TipoErro.ERRO_VALIDACAO);
        return responder(HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA, TipoErro.ERRO_VALIDACAO, excecao.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("evento=formato_nao_suportado excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), metodoDe(request), caminhoDe(request),
                HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), TipoErro.FORMATO_NAO_SUPORTADO);
        return responderObjeto(headers, HttpStatus.UNSUPPORTED_MEDIA_TYPE, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.FORMATO_NAO_SUPORTADO, MENSAGEM_INTERNA_FORMATO_NAO_SUPORTADO);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("evento=corpo_invalido excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), metodoDe(request), caminhoDe(request),
                HttpStatus.BAD_REQUEST.value(), TipoErro.ERRO_VALIDACAO);
        // Nunca ecoar excecao.getMessage(): a mensagem crua do Jackson pode conter
        // trechos do corpo enviado, inclusive dados do cliente.
        return responderObjeto(headers, HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, MENSAGEM_INTERNA_CORPO_INVALIDO);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("evento=validacao_falhou excecao={} metodo={} caminho={} status={} tipo_erro={} violacoes={}",
                excecao.getClass().getSimpleName(), metodoDe(request), caminhoDe(request),
                HttpStatus.BAD_REQUEST.value(), TipoErro.ERRO_VALIDACAO, excecao.getBindingResult().getErrorCount());

        String mensagemInterna = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> nomeDoCampoNoContrato(erro.getField()) + ": " + erro.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));

        return responderObjeto(headers, HttpStatus.BAD_REQUEST, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, mensagemInterna);
    }

    // Rede de segurança para exceções sem handler dedicado (inclusive 404 e 405).
    // Mensagem sempre fixa: não sabemos de antemão se excecao.getMessage() é segura.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excecao, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        log.warn("evento=requisicao_nao_tratada excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), metodoDe(request), caminhoDe(request), status.value(), TipoErro.ERRO_VALIDACAO);
        return responderObjeto(headers, status, MENSAGEM_REQUISICAO_INVALIDA,
                TipoErro.ERRO_VALIDACAO, MENSAGEM_INTERNA_REQUISICAO_NAO_TRATADA);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(Exception excecao, HttpServletRequest requisicao) {
        // excecao como último argumento aciona o stack trace completo no SLF4J.
        log.error("evento=erro_inesperado excecao={} metodo={} caminho={} status={} tipo_erro={}",
                excecao.getClass().getSimpleName(), requisicao.getMethod(), requisicao.getRequestURI(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(), TipoErro.ERRO_INTERNO, excecao);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, MENSAGEM_ERRO_INTERNO, TipoErro.ERRO_INTERNO, MENSAGEM_INTERNA_ERRO_INTERNO);
    }

    // Assinatura fixada pela superclasse (ResponseEntityExceptionHandler) — daí o cast.
    private static String metodoDe(WebRequest request) {
        return request instanceof ServletWebRequest servletWebRequest && servletWebRequest.getHttpMethod() != null
                ? servletWebRequest.getHttpMethod().name()
                : "?";
    }

    private static String caminhoDe(WebRequest request) {
        return request instanceof ServletWebRequest servletWebRequest
                ? servletWebRequest.getRequest().getRequestURI()
                : request.getDescription(false);
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
