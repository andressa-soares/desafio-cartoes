package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.fixture.ClockFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Ponta a ponta do POST /cartoes com as regras reais e o application.yml de
 * teste (mesmos valores do de produção). Só o relógio é substituído, para que
 * idade e data_solicitacao sejam determinísticas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("POST /cartoes")
class CartaoControllerIT {

    private static final String NOME = "Cliente Teste";
    private static final String CPF = "12345678910";
    private static final String EMAIL = "cliente@teste.com";
    private static final String TELEFONE = "11999999999";

    @TestConfiguration
    static class RelogioFixoConfig {

        // Nome de bean diferente de "clock" (o de CartoesConfig) de propósito: bean
        // definition overriding está desligado por padrão no Spring Boot 4. @Primary
        // já resolve a injeção por tipo sem precisar sobrescrever a definição.
        @Bean
        @Primary
        Clock clockDeTeste() {
            return ClockFixture.fixo();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    private static String requestJson(int idade, String dataNascimento, String uf, String rendaMensal) {
        return """
                {
                  "cliente": {
                    "nome": "%s",
                    "cpf": "%s",
                    "idade": %d,
                    "data_nascimento": "%s",
                    "uf": "%s",
                    "renda_mensal": %s,
                    "email": "%s",
                    "telefone_whatsapp": "%s"
                  }
                }
                """.formatted(NOME, CPF, idade, dataNascimento, uf, rendaMensal, EMAIL, TELEFONE);
    }

    @Test
    @DisplayName("32 anos, SP, renda 8000 retorna 200 com sem anuidade e cashback")
    void clienteDe32AnosEmSpComRenda8000RecebeSemAnuidadeECashback() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(32, "1994-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(2))
                .andExpect(jsonPath("$.cartoes_ofertados[*].tipo_cartao",
                        containsInAnyOrder("CARTAO_SEM_ANUIDADE", "CARTAO_COM_CASHBACK")));
    }

    @Test
    @DisplayName("22 anos, RJ, renda 9000 retorna 200 só com sem anuidade")
    void clienteDe22AnosNoRjComRenda9000RecebeSoSemAnuidade() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(22, "2004-01-01", "RJ", "9000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(1))
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").value("CARTAO_SEM_ANUIDADE"));
    }

    @Test
    @DisplayName("27 anos, SP, renda 8000 retorna 200 com os três cartões")
    void clienteDe27AnosEmSpComRenda8000RecebeOsTresCartoes() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(27, "1999-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(3))
                .andExpect(jsonPath("$.cartoes_ofertados[*].tipo_cartao", containsInAnyOrder(
                        "CARTAO_SEM_ANUIDADE", "CARTAO_DE_PARCEIROS", "CARTAO_COM_CASHBACK")));
    }

    @Test
    @DisplayName("40 anos, RJ, renda 6000 retorna 200 com sem anuidade e parceiros")
    void clienteDe40AnosNoRjComRenda6000RecebeSemAnuidadeEParceiros() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(40, "1986-01-01", "RJ", "6000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartoes_ofertados.length()").value(2))
                .andExpect(jsonPath("$.cartoes_ofertados[*].tipo_cartao",
                        containsInAnyOrder("CARTAO_SEM_ANUIDADE", "CARTAO_DE_PARCEIROS")));
    }

    @Test
    @DisplayName("resposta usa exatamente os nomes de campo em snake_case do contrato")
    void respostaUsaNomesDeCampoEmSnakeCase() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(32, "1994-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero_solicitacao").exists())
                .andExpect(jsonPath("$.data_solicitacao").exists())
                .andExpect(jsonPath("$.cliente.nome").value(NOME))
                .andExpect(jsonPath("$.cliente.cpf").value(CPF))
                .andExpect(jsonPath("$.cliente.idade").value(32))
                .andExpect(jsonPath("$.cliente.data_nascimento").value("1994-01-01"))
                .andExpect(jsonPath("$.cliente.uf").value("SP"))
                .andExpect(jsonPath("$.cliente.renda_mensal").value(8000.00))
                .andExpect(jsonPath("$.cliente.email").value(EMAIL))
                .andExpect(jsonPath("$.cliente.telefone_whatsapp").value(TELEFONE))
                .andExpect(jsonPath("$.cartoes_ofertados[0].tipo_cartao").exists())
                .andExpect(jsonPath("$.cartoes_ofertados[0].valor_anuidade_mensal").exists())
                .andExpect(jsonPath("$.cartoes_ofertados[0].valor_limite_disponivel").exists())
                .andExpect(jsonPath("$.cartoes_ofertados[0].status").value("APROVADO"));
    }

    @Test
    @DisplayName("corpo cru mantém duas casas decimais nos valores monetários")
    void corpoCruMantemDuasCasasDecimaisNosValoresMonetarios() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(32, "1994-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"valor_anuidade_mensal\":0.00")))
                .andExpect(content().string(containsString("\"renda_mensal\":8000.00")));
    }

    @Test
    @DisplayName("data_solicitacao é exatamente a data do relógio fixo")
    void dataSolicitacaoEhExatamenteADataDoRelogioFixo() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(32, "1994-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data_solicitacao").value("2026-09-22T10:15:30.123"));
    }

    @Test
    @DisplayName("numero_solicitacao é um UUID válido")
    void numeroSolicitacaoEhUmUuidValido() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(32, "1994-01-01", "SP", "8000.00")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero_solicitacao").value(
                        matchesPattern("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }
}
