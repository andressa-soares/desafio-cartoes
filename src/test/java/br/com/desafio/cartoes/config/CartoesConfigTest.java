package br.com.desafio.cartoes.config;

import br.com.desafio.cartoes.domain.regra.FiltroRenda;
import br.com.desafio.cartoes.domain.regra.ParametrosRegras;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Arrays;

import static br.com.desafio.cartoes.domain.fixture.ClienteFixture.umCliente;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CartoesConfig")
class CartoesConfigTest {

    private static final String[] CONFIGURACAO_VALIDA = {
            "cartoes.idade-minima=18",
            "cartoes.faixa-jovem.inicio=18",
            "cartoes.faixa-jovem.fim=24",
            "cartoes.faixa-excecao-residente-sp.inicio=25",
            "cartoes.faixa-excecao-residente-sp.fim=29",
            "cartoes.produtos[0].tipo=CARTAO_SEM_ANUIDADE",
            "cartoes.produtos[0].renda-minima=3500.00",
            "cartoes.produtos[0].limite=1000.00",
            "cartoes.produtos[0].anuidade-mensal=0.00",
            "cartoes.produtos[1].tipo=CARTAO_DE_PARCEIROS",
            "cartoes.produtos[1].renda-minima=5500.00",
            "cartoes.produtos[1].limite=3000.00",
            "cartoes.produtos[1].anuidade-mensal=10.00",
            "cartoes.produtos[2].tipo=CARTAO_COM_CASHBACK",
            "cartoes.produtos[2].renda-minima=7500.00",
            "cartoes.produtos[2].limite=5000.00",
            "cartoes.produtos[2].anuidade-mensal=20.00",
    };

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(CartoesConfig.class);

    private static String[] semPropriedade(String prefixoDaChave) {
        return Arrays.stream(CONFIGURACAO_VALIDA)
                .filter(linha -> !linha.startsWith(prefixoDaChave))
                .toArray(String[]::new);
    }

    private static String[] substituindo(String linhaAntiga, String linhaNova) {
        return Arrays.stream(CONFIGURACAO_VALIDA)
                .map(linha -> linha.equals(linhaAntiga) ? linhaNova : linha)
                .toArray(String[]::new);
    }

    @Nested
    @DisplayName("subida do contexto")
    class SubidaDoContexto {

        @Test
        @DisplayName("sobe e expõe FiltroRenda e ParametrosRegras com a configuração completa")
        void sobeEExpoeBeansComConfiguracaoCompleta() {
            contextRunner.withPropertyValues(CONFIGURACAO_VALIDA).run(context -> {
                assertThat(context).hasSingleBean(FiltroRenda.class);
                assertThat(context).hasSingleBean(ParametrosRegras.class);
            });
        }

        @Test
        @DisplayName("falha ao subir quando a renda mínima de um produto está ausente")
        void falhaQuandoRendaMinimaAusente() {
            contextRunner.withPropertyValues(semPropriedade("cartoes.produtos[0].renda-minima"))
                    .run(context -> assertThat(context).hasFailed());
        }

        @Test
        @DisplayName("falha ao subir quando a lista de produtos está vazia")
        void falhaQuandoListaDeProdutosVazia() {
            contextRunner.withPropertyValues(semPropriedade("cartoes.produtos"))
                    .run(context -> assertThat(context).hasFailed());
        }

        @Test
        @DisplayName("falha ao subir quando a faixa jovem está ausente")
        void falhaQuandoFaixaJovemAusente() {
            contextRunner.withPropertyValues(semPropriedade("cartoes.faixa-jovem"))
                    .run(context -> assertThat(context).hasFailed());
        }

        @Test
        @DisplayName("falha ao subir quando o tipo de cartão não existe no enum")
        void falhaQuandoTipoDeCartaoInexistente() {
            contextRunner.withPropertyValues(substituindo(
                            "cartoes.produtos[0].tipo=CARTAO_SEM_ANUIDADE",
                            "cartoes.produtos[0].tipo=CARTAO_INEXISTENTE"))
                    .run(context -> assertThat(context).hasFailed());
        }
    }

    @Nested
    @DisplayName("filtroRenda")
    class FiltroRendaBean {

        @Test
        @DisplayName("alcança dois produtos para renda 5500 com a configuração padrão")
        void alcancaDoisProdutosComConfiguracaoPadrao() {
            contextRunner.withPropertyValues(CONFIGURACAO_VALIDA).run(context -> {
                var filtroRenda = context.getBean(FiltroRenda.class);
                var cliente = umCliente().comRendaMensal("5500.00").build();

                var produtos = filtroRenda.aplicar(cliente);

                assertThat(produtos).hasSize(2);
            });
        }

        @Test
        @DisplayName("alcança apenas um produto quando a renda mínima de parceiros sobe para 6000")
        void alcancaUmProdutoQuandoRendaMinimaDeParceirosSobe() {
            var propriedades = substituindo(
                    "cartoes.produtos[1].renda-minima=5500.00",
                    "cartoes.produtos[1].renda-minima=6000.00");

            contextRunner.withPropertyValues(propriedades).run(context -> {
                var filtroRenda = context.getBean(FiltroRenda.class);
                var cliente = umCliente().comRendaMensal("5500.00").build();

                var produtos = filtroRenda.aplicar(cliente);

                assertThat(produtos).hasSize(1);
            });
        }
    }
}
