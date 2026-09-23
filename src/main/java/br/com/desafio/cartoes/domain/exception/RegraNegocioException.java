package br.com.desafio.cartoes.domain.exception;

// Sem dependência de HTTP: converter tipoErro em status code é responsabilidade do controller/handler.
public class RegraNegocioException extends RuntimeException {

    private final TipoErro tipoErro;

    public RegraNegocioException(TipoErro tipoErro, String mensagem) {
        super(mensagem);
        this.tipoErro = tipoErro;
    }

    public TipoErro tipoErro() {
        return tipoErro;
    }
}
