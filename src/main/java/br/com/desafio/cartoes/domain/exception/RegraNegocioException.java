package br.com.desafio.cartoes.domain.exception;

// Sem dependência de HTTP: converter tipoErro em status code é responsabilidade do controller/handler.
public class RegraNegocioException extends RuntimeException {

    private final String tipoErro;

    public RegraNegocioException(String tipoErro, String mensagem) {
        super(mensagem);
        this.tipoErro = tipoErro;
    }

    public String tipoErro() {
        return tipoErro;
    }
}
