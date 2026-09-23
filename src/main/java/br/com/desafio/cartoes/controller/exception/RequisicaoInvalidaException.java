package br.com.desafio.cartoes.controller.exception;

// Validações que Bean Validation não cobre sozinho: idade mínima (vem de
// configuração, não cabe em anotação) e coerência idade/data_nascimento
// (cruza dois campos). Ver SolicitacaoMapper.toCliente.
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
