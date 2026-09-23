import http from 'k6/http';
import { check, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.4/index.js';

// Teste de stress para POST /cartoes: escala a carga rápido pra achar onde a
// API começa a degradar, não o tráfego normal esperado.
// Aponta para outro ambiente sem editar o script:
// run with: k6 run -e BASE_URL=https://staging.exemplo.com cartoes.test.js
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Por padrão, o k6 conta qualquer resposta fora de 2xx/3xx como falha na
// métrica http_req_failed. Uma das massas abaixo espera 422 de propósito
// (renda insuficiente é uma regra de negócio, não um erro) — sem isso, o
// threshold de http_req_failed falharia mesmo com a API se comportando certo.
const respostaEsperada = http.expectedStatuses(200, 422);

// Estágios comprimidos (padrão do tipo Stress leva ~14min; aqui cabe em
// ~3m30s) para escalar rápido até 800 VUs — o suficiente pra estourar o pool
// padrão de threads do Tomcat (200) e revelar onde a API degrada.
export const options = {
  stages: [
    { duration: '30s', target: 100 },
    { duration: '30s', target: 250 },
    { duration: '1m', target: 500 },
    { duration: '1m', target: 800 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    // Latência é só observada, não travada: degradar sob stress é esperado.
  },
};

// Massas cobrindo os cenários reais das regras de elegibilidade (renda, idade,
// UF): cada uma tem o status HTTP que a API deve produzir para aquele cliente.
const massas = [
  {
    nome: '32 anos, SP, renda 8000 (sem anuidade + cashback)',
    payload: {
      cliente: {
        nome: 'Cliente Teste',
        cpf: '12345678910',
        idade: 32,
        data_nascimento: '1994-01-01',
        uf: 'SP',
        renda_mensal: 8000.00,
        email: 'cliente@teste.com',
        telefone_whatsapp: '11999999999',
      },
    },
    expectedStatus: 200,
  },
  {
    nome: '22 anos, RJ, renda 9000 (jovem, só sem anuidade)',
    payload: {
      cliente: {
        nome: 'Cliente Jovem',
        cpf: '98765432100',
        idade: 22,
        data_nascimento: '2004-03-15',
        uf: 'RJ',
        renda_mensal: 9000.00,
        email: 'jovem@teste.com',
        telefone_whatsapp: '21988887777',
      },
    },
    expectedStatus: 200,
  },
  {
    nome: '27 anos, SP, renda 8000 (exceção da regra de SP, os três cartões)',
    payload: {
      cliente: {
        nome: 'Cliente Excecao',
        cpf: '11122233344',
        idade: 27,
        data_nascimento: '1999-01-01',
        uf: 'SP',
        renda_mensal: 8000.00,
        email: 'excecao@teste.com',
        telefone_whatsapp: '11977776666',
      },
    },
    expectedStatus: 200,
  },
  {
    nome: '40 anos, RJ, renda 6000 (sem anuidade + parceiros)',
    payload: {
      cliente: {
        nome: 'Cliente Padrao',
        cpf: '55566677788',
        idade: 40,
        data_nascimento: '1986-01-01',
        uf: 'RJ',
        renda_mensal: 6000.00,
        email: 'padrao@teste.com',
        telefone_whatsapp: '21999998888',
      },
    },
    expectedStatus: 200,
  },
  {
    nome: '40 anos, BA, renda 1000 (renda insuficiente)',
    payload: {
      cliente: {
        nome: 'Cliente Renda Baixa',
        cpf: '99988877766',
        idade: 40,
        data_nascimento: '1986-01-01',
        uf: 'BA',
        renda_mensal: 1000.00,
        email: 'baixa@teste.com',
        telefone_whatsapp: '71999997777',
      },
    },
    expectedStatus: 422,
  },
];

export default function () {
  const massa = massas[Math.floor(Math.random() * massas.length)];

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
    tags: { massa: massa.nome },
    responseCallback: respostaEsperada,
  };

  const res = http.post(`${BASE_URL}/cartoes`, JSON.stringify(massa.payload), params);

  check(res, {
    // Cada massa tem um resultado de negócio esperado (200 ou 422) — não é
    // "status é 2xx" genérico, porque renda insuficiente é um cenário válido.
    'status bate com o esperado para a massa': (r) => r.status === massa.expectedStatus,
    'tempo de resposta < 500ms': (r) => r.timings.duration < 500,
    'corpo tem o formato esperado': (r) => {
      if (massa.expectedStatus === 200) {
        return r.body && r.body.includes('numero_solicitacao') && r.body.includes('cartoes_ofertados');
      }
      return r.body && r.body.includes('tipo_erro');
    },
  });

  sleep(1);
}

export function handleSummary(data) {
  return {
    'summary.json': JSON.stringify(data, null, 2),
    stdout: textSummary(data, { indent: ' ', enableColors: true }),
  };
}
