package br.com.desafio.cartoes.controller;

import br.com.desafio.cartoes.domain.regra.RegraElegibilidade;
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
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Classe separada para não afetar o contexto (e o cache de contexto) dos demais
 * testes de controller: registra uma RegraElegibilidade extra que zera qualquer
 * conjunto de candidatos.
 *
 * Com as regras reais o 204 é inalcançável — o cartão sem anuidade sobrevive a
 * todas. Este teste prova o caminho adicionando uma regra nova, sem alterar
 * nenhum código de produção existente: é a promessa do SDD em prática.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("POST /cartoes sem cartão aprovado")
class CartaoControllerSemCartaoIT {

    @TestConfiguration
    static class RegraQueZeraTudoConfig {

        @Bean
        RegraElegibilidade regraQueZeraTudo() {
            return (cliente, candidatos) -> Set.of();
        }

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

    @Test
    @DisplayName("qualquer cliente com renda suficiente recebe 204 sem corpo")
    void clienteComRendaSuficienteRecebe204SemCorpo() throws Exception {
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
                                    "renda_mensal": 10000.00,
                                    "email": "cliente@teste.com",
                                    "telefone_whatsapp": "11999999999"
                                  }
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
