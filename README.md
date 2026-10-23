# sys-produto

Sistema acadêmico de cadastro de produtos em JavaFX. O projeto mantém o formulário e a tabela criados no Scene Builder, com `HelloController` como controlador, e armazena os dados em `produtos.csv`.

## Requisitos e execução

- JDK **21** (configure o Project SDK e o JDK do Maven no IntelliJ IDEA).
- Internet na primeira execução para baixar Maven e dependências.
- Ambiente gráfico para abrir a interface JavaFX.

Abra a pasta que contém `pom.xml` no IntelliJ IDEA, importe como projeto Maven e aguarde a sincronização. No terminal dessa mesma pasta, execute:

Windows (PowerShell ou Prompt de Comando):

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

Linux/macOS:

```bash
bash mvnw clean verify
bash mvnw javafx:run
```

Também é possível executar a meta `javafx:run` pela janela Maven do IntelliJ. O wrapper usa Maven 3.8.5; a versão JavaFX do projeto é 21.0.6. Não é necessário instalar o JavaFX manualmente nem configurar um banco de dados. O JAR simples não inclui todas as dependências: prefira `javafx:run`.

## Como usar

1. **Novo:** limpa o formulário e permite informar um novo código.
2. Preencha código, nome, descrição, fabricação, validade, preços, estoque e categoria. **Cadastrar** valida os dados, inclui o produto, grava o CSV e atualiza a tabela.
3. Clique em uma linha da tabela para preencher o formulário. O código fica bloqueado porque identifica o produto. Edite os outros dados e clique em **Atualizar** para gravar as alterações.
4. **Excluir:** exige um produto selecionado e confirmação. Cancelar mantém os dados intactos.
5. **Salvar:** grava novamente a lista cadastrada no CSV. As operações anteriores já salvam automaticamente. Edições ainda no formulário precisam de **Atualizar**; um formulário novo precisa de **Cadastrar**.
6. **Consultar:** digite o código no campo de consulta e clique no botão ou pressione Enter. A consulta ignora maiúsculas/minúsculas e mostra o produto na tabela e no formulário. **Listar todos** restaura a lista completa.
7. **Relatórios:** abre uma janela com produtos que vencem de hoje até os próximos 60 dias (limites incluídos, sem produtos já vencidos) e produtos com menos de 10 unidades (incluindo estoque zero). Os dois relatórios usam `Stream.filter`; a apresentação usa `map` e `collect`.

Os preços aceitam vírgula ou ponto decimal, sem separador de milhar (ex.: `15,90`). O estoque deve ser inteiro. Datas podem ser escolhidas no calendário ou digitadas no formato indicado pelo componente.

## Validações e persistência

- Código único com exatamente 8 caracteres alfanuméricos, sem distinguir maiúsculas de minúsculas.
- Nome com pelo menos 3 caracteres após remover espaços externos.
- Fabricação obrigatória, até a data atual; validade obrigatória, igual ou posterior à fabricação.
- Compra positiva; venda maior que a compra; estoque inteiro não negativo; categoria obrigatória.
- Mensagens de erro na interface, sem encerrar o programa por entradas inválidas.

O CSV é UTF-8, sem cabeçalho, com 12 campos separados por `;`:

```text
codigo;nome;descricao;dataFabricacao;dataValidade;precoCompra;precoVenda;quantidadeEstoque;categoriaId;categoriaNome;categoriaDescricao;categoriaSetor
```

Datas são gravadas em `yyyy-MM-dd`, preços com ponto decimal. Campos com `;`, aspas ou quebras de linha são envolvidos por aspas, e aspas internas são duplicadas. O formato anterior sem aspas permanece compatível.

O arquivo é carregado ao abrir o programa. Quando não existe, o cadastro começa vazio e a primeira gravação cria o arquivo. A escrita usa um arquivo temporário e substituição do destino; falhas de escrita revertem a operação em memória. Se o CSV estiver corrompido ou ilegível, o programa avisa e bloqueia gravações para proteger os dados: faça backup, corrija o arquivo e reinicie. Não apague o CSV para resolver um erro sem preservar seus dados.

Por padrão, `produtos.csv` fica no **diretório de execução**; use a raiz do projeto como Working directory no IntelliJ. Para testes isolados, a propriedade JVM `-Dsysproduto.csv=/caminho/produtos.csv` permite outro arquivo. O sistema destina-se ao uso por uma instância por vez.

## Testes

```bash
bash mvnw clean verify
```

No Windows, use `mvnw.cmd clean verify`. Os testes JUnit cobrem regras de negócio, CRUD, relatórios, CSV com caracteres especiais, arquivos inválidos e reversão em falhas de gravação. Os dados de teste ficam em diretórios temporários, sem modificar o CSV do projeto.

Os testes de interface disparam os botões reais do FXML e exigem uma variável `DISPLAY` funcional. Em Linux com uma tela X11 acessível:

```bash
DISPLAY=:0 bash mvnw verify
```

Use o display real da sua máquina. Em ambientes sem `DISPLAY` (incluindo Windows), esses testes são **ignorados**, e os testes de modelo/CSV continuam executando. O resultado do Maven deve ser conferido em `Tests run`, `Failures`, `Errors` e `Skipped`. O teste gráfico foi executado na nuvem em uma tela virtual. Confira manualmente a aparência, o calendário e a navegação por teclado no computador da apresentação.

## Obter a versão pelo GitHub Desktop

1. Clique em **Fetch origin**.
2. Em **Current branch**, escolha `feat/finalizacao-academica`; se necessário, use a lista de branches remotas.
3. Clique em **Pull origin** se houver atualizações.
4. Use **Repository > Open in Explorer/Finder** e abra essa pasta no IntelliJ IDEA (selecione `pom.xml` ou a pasta do projeto).
5. Selecione JDK 21 e recarregue o projeto Maven. Execute os comandos acima.

A branch principal não precisa ser alterada para testar ou entregar a branch final. Depois de revisar o Pull Request e realizar a integração no GitHub, volte à branch `master` no GitHub Desktop e faça **Fetch origin > Pull origin** para obter a versão integrada. Preserve alterações locais antes de trocar de branch.

Para gerar um ZIP dos arquivos versionados da versão revisada:

```bash
git archive --format=zip --prefix=sys-produto/ -o ../sys-produto-final.zip HEAD
```

O ZIP contém código, FXML, wrapper, documentação e testes, sem caches Maven ou diretório `target`. Veja [RELATORIO.md](RELATORIO.md) para as decisões de implementação e a correspondência com os requisitos da atividade.
