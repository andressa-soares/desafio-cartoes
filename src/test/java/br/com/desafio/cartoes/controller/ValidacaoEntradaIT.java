package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.fixture.ClockFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Ponta a ponta da validação de entrada (400): campos obrigatórios, renda
 * negativa, idade mínima e coerência idade/data_nascimento. Usa o application.yml
 * de teste (idade mínima 18, mesmos produtos de produção). CPF, telefone e UF não
 * têm formato validado — fora do escopo do enunciado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Validação de entrada (400)")
class ValidacaoEntradaIT {

    @TestConfiguration
    static class RelogioFixoConfig {

        @Bean
        @Primary
        Clock clockDeTeste() {
            return ClockFixture.fixo();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("campo obrigatório ausente retorna 400 no payload padrão com ERRO_VALIDACAO")
    void campoObrigatorioAusenteRetorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": {
                                    "cpf": "12345678910",
                                    "idade": 40,
                                    "data_nascimento": "1986-01-01",
                                    "uf": "BA",
                                    "renda_mensal": 10000.00,
                                    "email": "cliente@teste.com",
                                    "telefone_whatsapp": "11999999999"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("ERRO_VALIDACAO"))
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("nome")));
    }

    @Test
    @DisplayName("renda negativa retorna 400 citando renda_mensal na mensagem")
    void rendaNegativaRetorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": {
                                    "nome": "Cliente Teste",
                                    "cpf": "12345678910",
                                    "idade": 40,
                                    "data_nascimento": "1986-01-01",
                                    "uf": "BA",
                                    "renda_mensal": -100.00,
                                    "email": "cliente@teste.com",
                                    "telefone_whatsapp": "11999999999"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("renda_mensal")));
    }

    @Test
    @DisplayName("cliente com 17 anos retorna 400 citando data_nascimento na mensagem")
    void clienteCom17AnosRetorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": {
                                    "nome": "Cliente Teste",
                                    "cpf": "12345678910",
                                    "idade": 17,
                                    "data_nascimento": "2009-01-01",
                                    "uf": "BA",
                                    "renda_mensal": 10000.00,
                                    "email": "cliente@teste.com",
                                    "telefone_whatsapp": "11999999999"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.mensagem").value("Requisição inválida."))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("ERRO_VALIDACAO"))
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("data_nascimento")));
    }

    @Test
    @DisplayName("cliente inteiramente ausente retorna 400 citando cliente na mensagem")
    void clienteAusenteRetorna400() throws Exception {
        // Corpo é um JSON válido (diferente de corpo vazio, que nem chega a ser
        // interpretado): SolicitacaoRequest é montado com cliente = null, e quem
        // barra aqui é o @NotNull do próprio campo, não o cascateamento de @Valid.
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("400"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("ERRO_VALIDACAO"))
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("cliente")));
    }

    @Test
    @DisplayName("idade divergente da data de nascimento retorna 400 citando idade na mensagem")
    void idadeDivergenteRetorna400() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cliente": {
                                    "nome": "Cliente Teste",
                                    "cpf": "12345678910",
                                    "idade": 99,
                                    "data_nascimento": "1986-01-01",
                                    "uf": "BA",
                                    "renda_mensal": 10000.00,
                                    "email": "cliente@teste.com",
                                    "telefone_whatsapp": "11999999999"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("idade")));
    }

    @Nested
    @DisplayName("múltiplas violações")
    class MultiplasViolacoes {

        @Test
        @DisplayName("uma única resposta 400 reúne todas as violações, ordenadas por campo")
        void multiplasViolacoesRetornamUmaUnicaResposta400() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "cliente": {
                                        "cpf": "12345678910",
                                        "idade": 40,
                                        "data_nascimento": "1986-01-01",
                                        "renda_mensal": -100.00,
                                        "email": "cliente@teste.com",
                                        "telefone_whatsapp": "11999999999"
                                      }
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("nome")))
                    .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("renda_mensal")))
                    .andExpect(jsonPath("$.detalhe_erro.mensagem_interna", containsString("uf")))
                    .andExpect(jsonPath("$.detalhe_erro.mensagem_interna",
                            stringContainsInOrder(List.of("nome", "renda_mensal", "uf"))));
        }
    }

    @Nested
    @DisplayName("nomes de campo no contrato")
    class NomesDeCampo {

        @Test
        @DisplayName("mensagens usam os nomes do contrato em snake_case, nunca os nomes Java")
        void mensagensUsamNomesDoContrato() throws Exception {
            // data_nascimento ausente e renda negativa: as duas violações passam pela
            // mesma tradução Java -> contrato usada pelo handler de Bean Validation.
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "cliente": {
                                        "nome": "Cliente Teste",
                                        "cpf": "12345678910",
                                        "idade": 40,
                                        "uf": "BA",
                                        "renda_mensal": -100.00,
                                        "email": "cliente@teste.com",
                                        "telefone_whatsapp": "11999999999"
                                      }
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("data_nascimento")))
                    .andExpect(content().string(containsString("renda_mensal")))
                    .andExpect(content().string(not(containsString("dataNascimento"))))
                    .andExpect(content().string(not(containsString("rendaMensal"))));
        }
    }

    @Nested
    @DisplayName("dados sensíveis")
    class DadosSensiveis {

        @Test
        @DisplayName("resposta 400 não contém CPF, e-mail, telefone nem data enviados")
        void resposta400NaoContemDadosDoCliente() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "cliente": {
                                        "cpf": "98765432100",
                                        "idade": 40,
                                        "data_nascimento": "1986-05-20",
                                        "uf": "BA",
                                        "renda_mensal": 10000.00,
                                        "email": "sensivel@teste.com",
                                        "telefone_whatsapp": "11988887777"
                                      }
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(not(containsString("98765432100"))))
                    .andExpect(content().string(not(containsString("1986-05-20"))))
                    .andExpect(content().string(not(containsString("sensivel@teste.com"))))
                    .andExpect(content().string(not(containsString("11988887777"))));
        }
    }

    @Nested
    @DisplayName("regressão")
    class Regressao {

        @Test
        @DisplayName("requisição válida continua respondendo 200")
        void requisicaoValidaContinuaRespondendo200() throws Exception {
            mockMvc.perform(post("/cartoes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "cliente": {
                                        "nome": "Cliente Teste",
                                        "cpf": "12345678910",
                                        "idade": 32,
                                        "data_nascimento": "1994-01-01",
                                        "uf": "SP",
                                        "renda_mensal": 8000.00,
                                        "email": "cliente@teste.com",
                                        "telefone_whatsapp": "11999999999"
                                      }
                                    }
                                    """))
                    .andExpect(status().isOk());
        }
    }
}
