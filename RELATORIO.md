# Relatório do Projeto — sys-produto

## Introdução

O projeto **sys-produto** consiste em um sistema de cadastro de produtos de uma loja ou supermercado, desenvolvido em Java com interface gráfica JavaFX. Sua finalidade é permitir o registro, a consulta e a manutenção de informações sobre os produtos, além de apresentar relatórios de validade e estoque. O desenvolvimento reúne conceitos de programação orientada a objetos, manipulação de arquivos e processamento de dados com a Stream API.

## Funcionalidades Implementadas

O sistema permite cadastrar produtos com código, nome, descrição, datas de fabricação e validade, preços de compra e venda, quantidade em estoque e categoria. Os registros são apresentados em uma tabela, cuja seleção preenche o formulário para edição. É possível atualizar os dados, excluir um produto mediante confirmação e consultar um registro pelo código. O botão **Novo** limpa o formulário, enquanto **Listar todos** restaura a exibição completa dos produtos.

Os dados são carregados de um arquivo CSV ao iniciar o programa. Cadastro, atualização e exclusão são salvos automaticamente; o botão **Salvar** permite gravar novamente a lista cadastrada.

As validações verificam a unicidade e o formato do código, o tamanho mínimo do nome, as datas, os preços, o estoque e a seleção da categoria. O sistema apresenta mensagens para entradas inválidas e problemas de leitura ou gravação.

Foram implementados dois relatórios: produtos com validade entre a data atual e os próximos 60 dias, incluindo os limites desse período e excluindo os já vencidos; e produtos com estoque inferior a 10 unidades.

## Decisões de Desenvolvimento

O JavaFX foi utilizado para construir a interface com formulário, seleção de categorias, botões e tabela. O layout foi mantido em FXML, compatível com o Scene Builder, e o `HelloController` permaneceu responsável pelos eventos da interface. O formulário foi organizado em duas colunas, com os controles de ação e consulta em uma única linha.

A programação orientada a objetos está representada pelas classes `Produto` e `Categoria`, sendo cada produto associado a uma categoria por composição. A classe `CadastroProduto` concentra as operações de gerenciamento, enquanto `ArquivoUtil` realiza a leitura e a gravação dos dados. Essa divisão separa as responsabilidades da interface, das regras de negócio e do armazenamento.

O CSV, com campos separados por ponto-e-vírgula, foi adotado para a persistência local, sem necessidade de banco de dados. A gravação trata caracteres especiais e procura preservar os registros diante de falhas. Caso o arquivo não possa ser carregado corretamente, novas gravações são bloqueadas para evitar sua substituição por dados incompletos.

A Stream API é utilizada para filtrar os produtos dos dois relatórios e organizar as informações apresentadas ao usuário.

## Testes Realizados

A compilação e o empacotamento do projeto foram concluídos com sucesso. Foram executados **11 testes automatizados**, sem falhas, erros ou testes ignorados: oito relacionados às regras de negócio e à persistência, e três à interface gráfica.

As verificações abrangeram cadastro, consulta, atualização, exclusão, validações, gravação e recarga do CSV, tratamento de arquivos inválidos e limites dos relatórios. Também foram testados os eventos dos botões e a apresentação dos dados na tabela. A interface foi conferida em uma tela virtual; a aparência e a interação no computador da apresentação ainda devem ser verificadas manualmente.

## Conclusão

O projeto implementa as operações de gerenciamento de produtos, a persistência em CSV e os dois relatórios previstos para a atividade. A organização das classes e da interface permite relacionar os recursos desenvolvidos aos conceitos de orientação a objetos, tratamento de eventos e processamento de coleções. Os testes realizados verificaram as funcionalidades implementadas no ambiente de desenvolvimento utilizado.
