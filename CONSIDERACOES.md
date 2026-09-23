# Considerações sobre o desafio

Documento solicitado no enunciado, com as premissas adotadas, as alternativas que considerei
e descartei, o que faria se o projeto evoluísse e algumas sugestões. As decisões técnicas
detalhadas estão em `docs/SDD.md` e o contrato em `src/main/resources/openapi.yaml`.

## 1. Premissas adotadas

Alguns pontos do enunciado admitiam mais de uma leitura e precisei escolher uma, já que a
escolha muda o comportamento da API. Registro aqui o que adotei.

**A tabela de regras prevalece sobre o exemplo de saída.** O payload de exemplo mostra um
cliente com renda 4000 recebendo o cartão com cashback, que exige renda mínima de 7500.
Tratei o exemplo como ilustração do formato e segui a tabela.

**Grafia do tipo de cartão.** Adotei `CARTAO_COM_CASHBACK`, conforme o domínio de tipos, e
considerei o `CARTAO_COM_CASHBACH` do exemplo como erro de digitação.

**Idades nas bordas das faixas.** As regras adicionais usam intervalos exclusivos em ambos os
lados, o que deixa as idades 18, 25 e 30 sem regra explícita. O próprio exemplo de payload
usa um cliente de 25 anos. Adotei:

| Idade | Fora de SP | Em SP |
|---|---|---|
| 18 a 24 | somente sem anuidade | somente sem anuidade |
| 25 a 29 | conforme renda | todos, conforme renda |
| 30 ou mais | conforme renda | cashback e sem anuidade, conforme renda |

**`data_nascimento` é a fonte da verdade para a idade.** Os dois campos são obrigatórios e o
exemplo traz um cliente nascido em 2000 com idade 25. Como a validação de maioridade é
descrita sobre `data_nascimento`, a idade usada nas regras é sempre calculada. O campo
`idade` continua obrigatório e é devolvido na resposta, e uma divergência entre os dois
resulta em 400, para que um dado inconsistente não entre na análise de crédito.

**Fronteira entre 204 e 422.** Renda insuficiente se encaixa nas duas descrições do
enunciado. Separei pelo significado: renda abaixo do mínimo de todos os produtos é análise de
crédito e responde 422 (o enunciado usa esse caso como exemplo de 422); lista vazia após as
regras de perfil é adequação de produto e responde 204.

**Regras adicionais restringem a tabela de renda, não a substituem.** Um cliente de 32 anos
em SP com renda 8000 não recebe o cartão de parceiros, porque a regra de SP o remove mesmo
com renda suficiente.

**Anuidade do cartão de parceiros.** O enunciado define 0.00 para o sem anuidade e o exemplo
mostra 20.00 para o cashback, mas não informa a do cartão de parceiros. Defini 10.00 como
valor de estudo, parametrizado no `application.yml`.

**Escopo de validação.** O enunciado pede para considerar que os dados chegam corretos em
tipo e formato, mas lista como obrigatórias a presença dos campos, a recusa de renda negativa
e a recusa de clientes com menos de 18 anos. Implementei exatamente esse recorte, com
anotações padrão do Bean Validation, e não implementei policiamento de formato. Ficaram de
fora, de propósito: dígito verificador do CPF (o campo é aceito com ou sem pontuação),
formato de telefone, validação de UF contra a lista de unidades federativas e recusa de data
de nascimento futura. Todas seriam naturais em um cenário real.

## 2. O 204 é inalcançável com as regras atuais

Analisando as regras em conjunto, notei que o status 204 nunca ocorre na configuração atual:
qualquer cliente com renda a partir de 3500 é candidato ao cartão sem anuidade, e nenhuma das
regras de perfil remove esse cartão. Ele sobrevive tanto à regra de idade quanto à de SP.

Mantive o caminho implementado por dois motivos: ele faz parte do contrato exigido, e as
regras são configuráveis e tendem a crescer. Se surgir uma regra que remova o cartão sem
anuidade de algum perfil, o 204 passa a ocorrer sem nenhuma alteração de código. Para não
deixar o caminho sem cobertura, ele é testado com uma regra adicional criada apenas no teste,
que devolve conjunto vazio: o Spring a descobre automaticamente e o endpoint responde 204,
sem que nenhuma linha de código de produção mude. Esse teste também serve como demonstração
de que novas regras entram sem tocar no que já existe.

## 3. Alternativas que considerei e descartei

**Ordenar as regras por especificidade em vez de aplicá-las todas.** A leitura mais literal do
enunciado seria ordenar as regras da mais específica para a mais geral (jovem, depois SP entre
25 e 29, depois SP, depois o caso padrão) e aplicar a primeira que se encaixasse no cliente.
Preferi que cada regra apenas remova cartões candidatos e que o resultado seja a interseção de
todas. Assim a ordem de aplicação é irrelevante e acrescentar uma regra nunca altera o
comportamento das existentes. A versão ordenada seria mais fiel ao texto, mas criaria uma
dependência de ordem que costuma quebrar silenciosamente quando o projeto cresce.

**Colocar as regras no service.** Seria mais direto e é comum em projetos pequenos. Deixei as
regras no domínio porque regra e fluxo mudam por motivos diferentes: a regra muda quando o
negócio pede, o fluxo muda por motivo técnico. Juntos, tendem a produzir um service que só
cresce em condicionais.

**Separar a exceção de SP em uma terceira regra.** A regra dos 25 a 29 anos em SP é uma
exceção à regra de SP, não uma regra independente. Como classes separadas, as duas
precisariam concordar entre si para funcionar; mantidas juntas, a exceção fica explícita no
mesmo lugar em que a restrição é aplicada.

**Validador customizado do Bean Validation para as regras de idade.** Cheguei a desenhar uma
anotação de classe com seu validador para verificar maioridade e coerência entre `idade` e
`data_nascimento`. Descartei por desproporção: duas classes e uma anotação customizada para
duas verificações que cabem junto do cálculo da idade, no mapper. Ganhei simplicidade e
mantive o mesmo caminho de erro das demais violações.

**Persistir as solicitações.** O enunciado permite banco de dados, mas nada no fluxo exige
estado: a resposta é função apenas da entrada e da configuração. Optei por manter a aplicação
sem persistência em vez de adicionar infraestrutura sem uso.

**Formato de erro segundo a RFC 9457.** O enunciado recomenda a RFC e, ao mesmo tempo, exige um
payload com `codigo`, `mensagem` e `detalhe_erro`. Segui o payload exigido, por ser
obrigatório. Os dois são conciliáveis, já que a RFC prevê campos de extensão: a resposta traria
`type`, `title`, `status` e `detail` com `application/problem+json`, e os campos do enunciado
conviveriam como extensões. Faria assim se o formato pudesse ser negociado.

## 4. Decisões que eu destacaria

**Parâmetro é configuração, comportamento é código.** Rendas mínimas, limites, anuidades,
idade mínima e faixas etárias estão no `application.yml`; nenhum desses valores aparece como
literal no código, nem em mensagens de erro. Já o conjunto de cartões permitidos por uma regra
e a UF que ela observa são a identidade da regra, então permanecem no código. Um teste altera
a idade mínima por configuração e verifica que o comportamento muda, provando que a
externalização é real e não decorativa.

**Relógio injetado.** A idade calculada e a data da solicitação dependem do instante atual.
Injetar um `Clock` permite fixá-lo nos testes, que assim não quebram na virada do ano nem no
aniversário de nenhum cliente de teste.

**Erros em um único ponto.** Todas as respostas de erro, inclusive 404 e 405, passam pelo mesmo
handler e saem no formato do contrato. Nenhuma expõe stack trace, nome de classe ou dados
recebidos.

**Contrato antes do código.** Escrevi o `openapi.yaml` e o documento de design antes de
implementar. Isso obrigou a fechar as ambiguidades da seção 1 antes de transformá-las em
código, e o histórico de commits reflete essa ordem.

## 5. Evolução

**Persistência das solicitações.** Se houver necessidade de histórico, auditoria ou consulta
posterior, o `numero_solicitacao` já existe como identificador. Entraria um repositório
chamado pelo service, sem alteração nas regras.

**Origem dos parâmetros.** Os produtos e as faixas etárias vêm do `application.yml`. Se o
negócio precisar cadastrá-los sem depender de deploy, a leitura passaria para um banco ou
serviço de configuração. Como o domínio recebe os parâmetros já convertidos e não sabe de onde
vêm, a mudança ficaria contida na camada de configuração.

**Organização por funcionalidade.** O projeto está organizado por camada, o que é adequado para
um único contexto de negócio. Se surgir um segundo contexto, a evolução natural é agrupar por
funcionalidade, com cada contexto contendo suas próprias camadas e um pacote compartilhado para
o que for comum. A separação atual torna essa mudança mecânica.

**Segurança.** Implementei o que protege o serviço sem depender de decisões que o enunciado não
fornece. Ficaram para uma etapa seguinte a autenticação e autorização, que dependem de saber
quem consome a API, e o controle de taxa de requisições, que normalmente vive na camada de
gateway.

**Motor de regras.** Se o número de regras crescer muito ou se áreas de negócio precisarem
alterá-las sem passar por desenvolvimento, um motor de regras externo seria a evolução natural.
Para a quantidade atual, seria complexidade sem retorno.

**Observabilidade.** O passo seguinte seria exportar métricas para um sistema de monitoração e
adotar rastreamento distribuído, que só faz sentido quando houver outros serviços na cadeia.

## 6. Sobre o processo

Trabalhei em duas fases. Primeiro fechei as decisões, escrevi o contrato e o documento de
design, só depois comecei a implementar. Isso deixou as ambiguidades resolvidas antes de virarem
código e tornou o histórico de commits legível, com o planejamento visível antes da
implementação. Cada funcionalidade entrou junto com seus testes, incluindo casos de borda e de
erro, e não apenas os caminhos felizes.

A maior parte do meu esforço não foi escrever código, e sim decidir o comportamento e estratégia 
adotada. Considero que esse foi o ponto mais interessante do
desafio.