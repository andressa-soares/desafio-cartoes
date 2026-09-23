package br.com.desafio.cartoes.domain.model;

public record FaixaEtaria(int inicio, int fimInclusivo) {

    public FaixaEtaria {
        if (inicio < 0) {
            throw new IllegalArgumentException("Início da faixa etária não pode ser negativo.");
        }
        if (fimInclusivo < inicio) {
            throw new IllegalArgumentException("Fim da faixa etária não pode ser menor que o início.");
        }
    }

    public boolean contem(int idade) {
        return idade >= inicio && idade <= fimInclusivo;
    }
}
