package br.com.desafio.cartoes.controller.handler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Ponta a ponta das respostas de erro que não dependem de validação de campo:
 * 422, 415, 400 (corpo malformado/vazio), 404 e 405. A validação de campo por
 * campo (renda negativa, menor de idade etc.) está em ValidacaoEntradaIT.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerIT {

    @Autowired
    private MockMvc mockMvc;

    @Value("${spring.application.name}")
    private String nomeAplicacao;

    private static String requestComRenda(String rendaMensal) {
        return """
                {
                  "cliente": {
                    "nome": "Cliente Teste",
                    "cpf": "12345678910",
                    "idade": 40,
                    "data_nascimento": "1986-01-01",
                    "uf": "BA",
                    "renda_mensal": %s,
                    "email": "cliente@teste.com",
                    "telefone_whatsapp": "11999999999"
                  }
                }
                """.formatted(rendaMensal);
    }

    private static void assertPayloadPadrao(ResultActions resultado) throws Exception {
        resultado
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(jsonPath("$.mensagem").exists())
                .andExpect(jsonPath("$.detalhe_erro").exists())
                .andExpect(jsonPath("$.detalhe_erro.app").exists())
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").exists())
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna").exists());
    }

    @Nested
    @DisplayName("422 renda insuficiente")
    class RendaInsuficiente {

        @Test
        @DisplayName("renda 1000 retorna 422 com RENDA_INSUFICIENTE e detalhe_erro.app igual ao nome da aplicação")
        void renda1000Retorna422() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestComRenda("1000.00")))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(jsonPath("$.codigo").value("422"))
                    .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("RENDA_INSUFICIENTE"))
                    .andExpect(jsonPath("$.detalhe_erro.app").value(nomeAplicacao));
        }

        @Test
        @DisplayName("renda exatamente 3499.99 retorna 422")
        void renda349999Retorna422() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestComRenda("3499.99")))
                    .andExpect(status().isUnprocessableEntity());
        }

        @Test
        @DisplayName("renda 3500 não retorna 422")
        void renda3500NaoRetorna422() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestComRenda("3500.00")))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("415 formato não suportado")
    class FormatoNaoSuportado {

        @Test
        @DisplayName("Content-Type text/plain retorna 415 com FORMATO_NAO_SUPORTADO")
        void contentTypeTextPlainRetorna415() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.TEXT_PLAIN)
                            .content("qualquer corpo"))
                    .andExpect(status().isUnsupportedMediaType())
                    .andExpect(jsonPath("$.codigo").value("415"))
                    .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("FORMATO_NAO_SUPORTADO"));
        }
    }

    @Nested
    @DisplayName("400 corpo inválido")
    class CorpoInvalido {

        @Test
        @DisplayName("JSON malformado retorna 400 com ERRO_VALIDACAO")
        void jsonMalformadoRetorna400() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ \"cliente\": "))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("400"))
                    .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("ERRO_VALIDACAO"));
        }

        @Test
        @DisplayName("corpo vazio retorna 400")
        void corpoVazioRetorna400() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigo").value("400"));
        }
    }

    @Nested
    @DisplayName("rotas e métodos não previstos no contrato")
    class RotasEMetodos {

        @Test
        @DisplayName("GET /cartoes retorna 405 no payload padrão")
        void getCartoesRetorna405() throws Exception {
            mockMvc.perform(get("/cartoes"))
                    .andExpect(status().isMethodNotAllowed())
                    .andExpect(jsonPath("$.codigo").value("405"))
                    .andExpect(jsonPath("$.detalhe_erro.app").value(nomeAplicacao));
        }

        @Test
        @DisplayName("POST /cartao (rota inexistente) retorna 404 no payload padrão")
        void postCartaoRotaInexistenteRetorna404() throws Exception {
            mockMvc.perform(post("/cartao")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestComRenda("5000.00")))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.codigo").value("404"))
                    .andExpect(jsonPath("$.detalhe_erro.app").value(nomeAplicacao));
        }
    }

    @Nested
    @DisplayName("formato da resposta de erro")
    class FormatoDaResposta {

        @Test
        @DisplayName("toda resposta de erro tem os três campos do contrato e os três de detalhe_erro")
        void todaRespostaDeErroTemOsCamposDoContrato() throws Exception {
            assertPayloadPadrao(mockMvc.perform(post("/cartoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestComRenda("1000.00"))));

            assertPayloadPadrao(mockMvc.perform(post("/cartoes")
                    .contentType(MediaType.TEXT_PLAIN)
                    .content("qualquer corpo")));

            assertPayloadPadrao(mockMvc.perform(post("/cartoes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{ \"cliente\": ")));

            assertPayloadPadrao(mockMvc.perform(get("/cartoes")));

            assertPayloadPadrao(mockMvc.perform(post("/cartao")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestComRenda("5000.00"))));
        }

        @Test
        @DisplayName("resposta de erro não expõe stackTrace, exception, trace nem nome de classe Java")
        void respostaDeErroNaoExpoeDetalhesInternos() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestComRenda("1000.00")))
                    .andExpect(jsonPath("$.stackTrace").doesNotExist())
                    .andExpect(jsonPath("$.exception").doesNotExist())
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(content().string(not(containsString("java.lang"))))
                    .andExpect(content().string(not(containsString("Exception"))));
        }
    }
}
