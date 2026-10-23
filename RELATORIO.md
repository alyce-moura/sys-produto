# Relatório de implementação — sys-produto

## Funcionalidades e requisitos da atividade

| Exigência do PDF | Implementação |
| --- | --- |
| Interface JavaFX com formulário, tabela e eventos | FXML original preservado, `HelloController` finalizado e seleção da tabela preenchendo o formulário |
| Cadastro com todos os dados | Botão Cadastrar, categorias no ComboBox e validação central em `Produto.validar()` |
| Exclusão de produto | Seleção na tabela ou pela consulta, confirmação e persistência automática |
| Consulta específica por código | Campo de consulta, botão Consultar e tecla Enter; busca sem diferenciar maiúsculas/minúsculas |
| Listagem de todos os produtos | Carregamento inicial e botão Listar todos |
| Classes Categoria e Produto com atributos exigidos | Classes existentes preservadas, inclusive composição e tipos `LocalDate` e `BigDecimal` |
| CSV separado por ponto-e-vírgula | 12 campos em UTF-8, compatível com o CSV original e com escape de caracteres especiais |
| Carregamento ao iniciar e gravação após operações | `CadastroProduto` carrega no construtor; cadastro, atualização e exclusão salvam automaticamente |
| Pelo menos dois relatórios com Streams | Próximos ao vencimento em até 60 dias e estoque abaixo de 10 unidades, exibidos pela interface |
| Validações e tratamento de exceções | Regras de código, nome, datas, preços, estoque e categoria; mensagens na interface e proteção contra perda do CSV |
| README e breve relatório | `README.md` e este documento |
| Código no GitHub e projeto ZIP | Branch própria para revisão; ZIP gerável com `git archive` conforme README |

Além do mínimo do PDF, os botões já presentes **Novo**, **Atualizar** e **Salvar** foram conectados às respectivas operações para completar o layout existente. Não foram criadas novas classes de produção.

## Decisões de implementação

- O `HelloController` continua como único controlador. A disposição original de formulário, botões e tabela foi mantida; acrescentou-se uma linha de consulta/relatórios e uma mensagem de estado. O namespace FXML foi alinhado ao JavaFX 21 usado pelo projeto.
- A seleção de uma linha carrega todos os campos. O código fica imutável durante a edição; para cadastrar outro produto, usa-se Novo. Atualizar grava as edições do formulário. Salvar grava novamente a lista, sem cadastrar implicitamente um formulário incompleto.
- `CadastroProduto` centraliza as operações e os dois filtros de relatório. Produtos com validade entre hoje e hoje + 60 dias são incluídos; os já vencidos ficam fora desse relatório. Estoque baixo corresponde estritamente a quantidade menor que 10.
- `ArquivoUtil` mantém o arquivo local, sem banco ou biblioteca adicional. O parser trata campos com `;`, aspas e quebras de linha, e identifica registros inválidos, datas/números incorretos e códigos duplicados.
- A gravação é feita em arquivo temporário e substitui o destino. Se a escrita falhar, cadastro, atualização e exclusão revertem a alteração em memória. Uma falha de carregamento bloqueia gravações para impedir a substituição acidental de dados não carregados.
- A abertura do pacote do modelo para `javafx.base` permite que a tabela modular use os getters via `PropertyValueFactory`.
- O plugin Surefire foi atualizado para executar de fato o JUnit 5 já declarado no projeto. Os testes usam diretórios temporários e não alteram o CSV do usuário.

## Validação e verificação manual

Na execução final na nuvem, `clean verify` terminou com **BUILD SUCCESS**: **11 testes executados, 0 falhas, 0 erros e 0 ignorados** (8 de modelo/CSV e 3 de interface). Também foi verificada a aplicação no modo modular, com cadastro pela interface e leitura dos getters da tabela. O arquivo `produtos.csv` versionado foi preservado.

Os testes automatizados cobrem CRUD e recarga, unicidade de código, validações dos campos, caracteres especiais no CSV, arquivos corrompidos, reversão em falhas de escrita e limites dos dois relatórios. Os testes JavaFX carregam o FXML real e disparam cadastro, consulta, atualização, exclusão com cancelamento/confirmação, Novo, Salvar, listagem e abertura dos relatórios.

A execução dos testes gráficos depende de `DISPLAY`; sem ele, são reportados como ignorados. Antes da apresentação, confirme no computador de entrega a aparência do formulário, redimensionamento, acentos, seleção de datas e navegação por teclado. Confira também a abertura pelo IntelliJ com JDK 21 e o arquivo CSV no diretório correto. Esses itens dependem da máquina local e não podem ser garantidos apenas pela execução na nuvem.
