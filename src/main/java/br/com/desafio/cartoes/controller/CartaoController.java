package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.controller.dto.SolicitacaoRequest;
import br.com.desafio.cartoes.controller.dto.SolicitacaoResponse;
import br.com.desafio.cartoes.controller.mapper.SolicitacaoMapper;
import br.com.desafio.cartoes.domain.model.Cliente;
import br.com.desafio.cartoes.domain.model.Solicitacao;
import br.com.desafio.cartoes.service.SolicitacaoCartaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
public class CartaoController {

    private final SolicitacaoCartaoService service;
    private final SolicitacaoMapper mapper;

    public CartaoController(SolicitacaoCartaoService service, SolicitacaoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping(path = "/cartoes", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SolicitacaoResponse> solicitar(@Valid @RequestBody SolicitacaoRequest request) {
        Cliente cliente = mapper.toCliente(request.cliente());
        Solicitacao solicitacao = service.solicitar(cliente);

        if (!solicitacao.possuiCartaoAprovado()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(mapper.toResponse(solicitacao));
    }
}