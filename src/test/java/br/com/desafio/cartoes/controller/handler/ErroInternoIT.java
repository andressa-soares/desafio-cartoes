package br.com.desafio.cartoes.controller.handler;

import br.com.desafio.cartoes.service.SolicitacaoCartaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 * Classe separada para não afetar o contexto (e o cache de contexto) dos demais
 * testes de controller: substitui o service real por um mock que lança uma
 * exceção não prevista, com uma mensagem propositalmente sensível, para provar
 * que o handler de 500 nunca a repassa na resposta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("GlobalExceptionHandler: erro inesperado")
class ErroInternoIT {

    private static final String CPF_QUE_NAO_PODE_VAZAR = "11122233344";
    private static final String MENSAGEM_SENSIVEL =
            "Falha ao consultar o CPF " + CPF_QUE_NAO_PODE_VAZAR + " no bureau de crédito.";

    private static final String REQUEST_VALIDO = """
            {
              "cliente": {
                "nome": "Cliente Teste",
                "cpf": "98765432100",
                "idade": 40,
                "data_nascimento": "1986-01-01",
                "uf": "BA",
                "renda_mensal": 8000.00,
                "email": "cliente@teste.com",
                "telefone_whatsapp": "11999999999"
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitacaoCartaoService service;

    @BeforeEach
    void configuraServiceParaLancarExcecaoComMensagemSensivel() {
        when(service.solicitar(any())).thenThrow(new RuntimeException(MENSAGEM_SENSIVEL));
    }

    @Test
    @DisplayName("requisição válida retorna 500 com ERRO_INTERNO quando o service lança exceção não prevista")
    void requisicaoValidaRetorna500ComErroInterno() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_VALIDO))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").value("500"))
                .andExpect(jsonPath("$.detalhe_erro.tipo_erro").value("ERRO_INTERNO"));
    }

    @Test
    @DisplayName("resposta não contém a mensagem original da exceção nem o CPF que ela citava")
    void respostaNaoContemMensagemOriginalNemCpf() throws Exception {
        mockMvc.perform(post("/cartoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_VALIDO))
                .andExpect(content().string(not(containsString(CPF_QUE_NAO_PODE_VAZAR))))
                .andExpect(content().string(not(containsString("bureau de crédito"))))
                .andExpect(content().string(not(containsString(MENSAGEM_SENSIVEL))));
    }
}
