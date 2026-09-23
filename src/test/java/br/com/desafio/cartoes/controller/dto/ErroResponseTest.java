package br.com.desafio.cartoes.controller.dto;

import br.com.desafio.cartoes.domain.exception.TipoErro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErroResponse")
class ErroResponseTest {

    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Nested
    @DisplayName("de")
    class De {

        @Test
        @DisplayName("preenche codigo como a representação em String do status HTTP")
        void preencheCodigoComoStringDoStatus() {
            var erro = ErroResponse.de(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio não atendida.",
                    "desafio-cartoes", TipoErro.RENDA_INSUFICIENTE, "mensagem interna");

            assertThat(erro.codigo()).isEqualTo("422");
        }

        @Test
        @DisplayName("preenche mensagem e os três campos de detalhe_erro com os valores informados")
        void preencheDemaisCampos() {
            var erro = ErroResponse.de(HttpStatus.UNPROCESSABLE_ENTITY, "Regra de negócio não atendida.",
                    "desafio-cartoes", TipoErro.RENDA_INSUFICIENTE, "mensagem interna");

            assertThat(erro.mensagem()).isEqualTo("Regra de negócio não atendida.");
            assertThat(erro.detalheErro().app()).isEqualTo("desafio-cartoes");
            assertThat(erro.detalheErro().tipoErro()).isEqualTo(TipoErro.RENDA_INSUFICIENTE);
            assertThat(erro.detalheErro().mensagemInterna()).isEqualTo("mensagem interna");
        }
    }

    @Nested
    @DisplayName("serialização")
    class Serializacao {

        private final ErroResponse erro = ErroResponse.de(HttpStatus.BAD_REQUEST, "Requisição inválida.",
                "desafio-cartoes", TipoErro.ERRO_VALIDACAO, "detalhe");

        @Test
        @DisplayName("produz exatamente os campos codigo, mensagem e detalhe_erro no nível raiz")
        void produzCamposDoNivelRaiz() {
            var json = objectMapper.readTree(objectMapper.writeValueAsString(erro));

            assertThat(json.propertyNames()).containsExactlyInAnyOrder("codigo", "mensagem", "detalhe_erro");
        }

        @Test
        @DisplayName("produz exatamente os campos app, tipo_erro e mensagem_interna dentro de detalhe_erro")
        void produzCamposDeDetalheErro() {
            var json = objectMapper.readTree(objectMapper.writeValueAsString(erro));

            assertThat(json.get("detalhe_erro").propertyNames())
                    .containsExactlyInAnyOrder("app", "tipo_erro", "mensagem_interna");
        }

        @Test
        @DisplayName("serializa tipo_erro pelo nome da constante do enum")
        void serializaTipoErroPeloNomeDaConstante() {
            var json = objectMapper.readTree(objectMapper.writeValueAsString(erro));

            assertThat(json.get("detalhe_erro").get("tipo_erro").asString()).isEqualTo("ERRO_VALIDACAO");
        }
    }
}
