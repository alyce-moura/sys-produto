package org.sysproduto;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.sysproduto.model.Categoria;
import org.sysproduto.model.Produto;
import javafx.util.StringConverter;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import org.sysproduto.model.CadastroProduto;

import javafx.collections.FXCollections;

public class HelloController {

    // gerenciamento de produtos cadastrados
    private final CadastroProduto cadastro = new CadastroProduto();
    private String codigoSelecionado;

    @FXML private TextField txtConsulta;
    @FXML private Label lblStatus;

    //  COMPONENETES DO FORMULÁRIO
    @FXML
    private TextField txtCodigo;

    @FXML
    private TextField txtNome;

    @FXML
    private TextArea txtDescricao;

    @FXML
    private DatePicker dpDataFabricacao;

    @FXML
    private DatePicker dpDataValidade;

    @FXML
    private TextField txtPrecoCompra;

    @FXML
    private TextField txtPrecoVenda;

    @FXML
    private TextField txtQuantidadeEstoque;

    @FXML
    private ComboBox<Categoria> cbCategoria;

    // Botoões de Ação do Sistema
    @FXML
    private Button btnNovo;

    @FXML
    private Button btnCadastrar;

    @FXML
    private Button btnAtualizar;

    @FXML
    private Button btnExcluir;

    @FXML
    private Button btnSalvar;

    // tabela de produtos
    @FXML
    private TableView<Produto> tblProdutos;

    @FXML
    private TableColumn<Produto, String> colCodigo;

    @FXML
    private TableColumn<Produto, String> colNome;

    @FXML
    private TableColumn<Produto, String> colCategoria;

    @FXML
    private TableColumn<Produto, BigDecimal> colPrecoCompra;

    @FXML
    private TableColumn<Produto, BigDecimal> colPrecoVenda;

    @FXML
    private TableColumn<Produto, Integer> colEstoque;

    // inicialização da interface
    @FXML
    public void initialize() {
        // CONFIGURAÇÃO DA COLUNA CÓDIGO
        colCodigo.setCellValueFactory(
                new PropertyValueFactory<>("codigo")
        );
        // configuração da coluna Nome
        colNome.setCellValueFactory(
                new PropertyValueFactory<>("nome")
        );
        // configuração da coluna Categoria
        colCategoria.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getCategoria().getNome()
                )
        );
        // Configuração da coluna Preço de Compra
        colPrecoCompra.setCellValueFactory(
                new PropertyValueFactory<>("precoCompra")
        );
        // Configuração da coluna Preço de Compra
        colPrecoVenda.setCellValueFactory(
                new PropertyValueFactory<>("precoVenda")
        );
        // Configuração da Coluna Preço de Venda
        colEstoque.setCellValueFactory(
                new PropertyValueFactory<>("quantidadeEstoque")
        );
        // CONFIGURAÇÃO DAS CATEGORIAS

        // adc as categorias ao ComboBox
        cbCategoria.getItems().addAll(
           new Categoria(1, "Eletrônicos", "Produtos eletrônicos", "Tecnologia"),
           new Categoria(2, "Alimentos", "Produtos alimentícios", "Alimentação"),
           new Categoria(3, "Roupas", "Produtos de vestuário", "Moda"),
           new Categoria(4, "Limpeza", "Produtos de limpeza", "Higiene")
        );

        // exibe o nome da categoria no ComboBox
        cbCategoria.setConverter(new StringConverter<Categoria>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria == null ? "" : categoria.getNome();
            }
            @Override
            public Categoria fromString(String nome) {
                return null;
            }
        });

        // carrega os produtos cadastrados na tabela
        atualizarTabela();
        tblProdutos.getSelectionModel().selectedItemProperty().addListener(
                (observavel, anterior, atual) -> {
                    if (atual != null) {
                        preencherFormulario(atual);
                    } else {
                        codigoSelecionado = null;
                        txtCodigo.setEditable(true);
                    }
                });
        if (cadastro.getErroCarregamento() != null) {
            btnCadastrar.setDisable(true);
            btnAtualizar.setDisable(true);
            btnExcluir.setDisable(true);
            btnSalvar.setDisable(true);
            lblStatus.setText("CSV não carregado. Corrija o arquivo e reinicie o sistema.");
            Platform.runLater(() -> mostrarErro("Erro ao carregar produtos: " + cadastro.getErroCarregamento()));
        }
    }

    @FXML
    private void onClickCadastrar() {
        executarOperacao(() -> {
            cadastro.cadastrarProduto(lerFormulario());
            atualizarTabela();
            onClickNovo();
            lblStatus.setText("Produto cadastrado e salvo no CSV.");
        });
    }

    @FXML
    private void onClickNovo() {
        tblProdutos.getSelectionModel().clearSelection();
        codigoSelecionado = null;
        txtCodigo.setEditable(true);
        txtCodigo.clear();
        txtNome.clear();
        txtDescricao.clear();
        dpDataFabricacao.setValue(null);
        dpDataFabricacao.getEditor().clear();
        dpDataValidade.setValue(null);
        dpDataValidade.getEditor().clear();
        txtPrecoCompra.clear();
        txtPrecoVenda.clear();
        txtQuantidadeEstoque.clear();
        cbCategoria.getSelectionModel().clearSelection();
        cbCategoria.setValue(null);
        lblStatus.setText("Informe os dados de um novo produto.");
        txtCodigo.requestFocus();
    }

    @FXML
    private void onClickAtualizar() {
        executarOperacao(() -> {
            exigirSelecao();
            cadastro.atualizarProduto(codigoSelecionado, lerFormulario());
            atualizarTabela();
            onClickNovo();
            lblStatus.setText("Produto atualizado e salvo no CSV.");
        });
    }

    @FXML
    private void onClickExcluir() {
        executarOperacao(() -> {
            exigirSelecao();
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir o produto " + codigoSelecionado + "?", ButtonType.YES, ButtonType.NO);
            confirmacao.setTitle("Excluir produto");
            confirmacao.setHeaderText("Confirmação de exclusão");
            confirmacao.initOwner(tblProdutos.getScene().getWindow());
            if (confirmacao.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
            if (!cadastro.excluirProduto(codigoSelecionado)) {
                throw new IllegalArgumentException("Produto não encontrado.");
            }
            atualizarTabela();
            onClickNovo();
            lblStatus.setText("Produto excluído. CSV atualizado.");
        });
    }

    @FXML
    private void onClickSalvar() {
        executarOperacao(() -> {
            cadastro.salvar();
            lblStatus.setText("Lista salva no CSV. Para gravar edições do formulário, use Atualizar.");
        });
    }

    @FXML
    private void onClickConsultar() {
        executarOperacao(() -> {
            String codigo = txtConsulta.getText().trim();
            if (codigo.isEmpty()) {
                throw new IllegalArgumentException("Informe o código do produto para consultar.");
            }
            Produto encontrado = cadastro.consultarProduto(codigo);
            if (encontrado == null) {
                throw new IllegalArgumentException("Nenhum produto encontrado com esse código.");
            }
            tblProdutos.setItems(FXCollections.observableArrayList(encontrado));
            tblProdutos.getSelectionModel().select(encontrado);
            lblStatus.setText("Produto encontrado. Selecione Listar todos para retornar à lista completa.");
        });
    }

    @FXML
    private void onClickListarTodos() {
        atualizarTabela();
        onClickNovo();
        txtConsulta.clear();
        lblStatus.setText(cadastro.listarProdutos().size() + " produto(s) cadastrado(s).");
    }

    @FXML
    private void onClickRelatorios() {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("Relatórios de produtos");
        dialogo.setHeaderText("Relatórios gerados com a API Stream — "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dialogo.initOwner(tblProdutos.getScene().getWindow());
        TextArea texto = new TextArea(
                "PRÓXIMOS AO VENCIMENTO (de hoje até 60 dias, inclusive)\n"
                + formatarRelatorio(cadastro.produtosProximosVencimento())
                + "\n\nESTOQUE BAIXO (menos de 10 unidades)\n"
                + formatarRelatorio(cadastro.produtosEstoqueBaixo()));
        texto.setEditable(false);
        texto.setWrapText(true);
        texto.setPrefSize(700, 400);
        dialogo.getDialogPane().setContent(texto);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialogo.show();
    }

    private String formatarRelatorio(List<Produto> produtos) {
        if (produtos.isEmpty()) {
            return "Nenhum produto encontrado.";
        }
        return produtos.stream()
                .map(p -> p.getCodigo() + " | " + p.getNome() + " | Categoria: "
                        + p.getCategoria().getNome() + " | Validade: "
                        + p.getDataValidade().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                        + " | Estoque: " + p.getQuantidadeEstoque())
                .collect(Collectors.joining("\n"));
    }

    private Produto lerFormulario() {
        // commitValue considera também datas digitadas, sem depender de sair do campo.
        dpDataFabricacao.commitValue();
        dpDataValidade.commitValue();
        return new Produto(txtCodigo.getText().trim(), txtNome.getText().trim(),
                txtDescricao.getText().trim(), dpDataFabricacao.getValue(), dpDataValidade.getValue(),
                new BigDecimal(txtPrecoCompra.getText().trim().replace(",", ".")),
                new BigDecimal(txtPrecoVenda.getText().trim().replace(",", ".")),
                Integer.parseInt(txtQuantidadeEstoque.getText().trim()), cbCategoria.getValue());
    }

    private void preencherFormulario(Produto produto) {
        codigoSelecionado = produto.getCodigo();
        txtCodigo.setText(produto.getCodigo());
        txtCodigo.setEditable(false);
        txtNome.setText(produto.getNome());
        txtDescricao.setText(produto.getDescricao());
        dpDataFabricacao.setValue(produto.getDataFabricacao());
        dpDataValidade.setValue(produto.getDataValidade());
        txtPrecoCompra.setText(produto.getPrecoCompra().toPlainString());
        txtPrecoVenda.setText(produto.getPrecoVenda().toPlainString());
        txtQuantidadeEstoque.setText(String.valueOf(produto.getQuantidadeEstoque()));
        cbCategoria.setValue(produto.getCategoria());
        lblStatus.setText("Produto selecionado. Edite os dados e clique em Atualizar.");
    }

    private void exigirSelecao() {
        if (codigoSelecionado == null) {
            throw new IllegalArgumentException("Selecione um produto na tabela ou consulte seu código primeiro.");
        }
    }

    private void executarOperacao(Runnable operacao) {
        try {
            operacao.run();
        } catch (DateTimeParseException e) {
            mostrarErro("Data inválida. Informe uma data no formato exibido pelo calendário.");
        } catch (NumberFormatException e) {
            mostrarErro("Informe preços numéricos (ex.: 25,90) e estoque como número inteiro.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        lblStatus.setText(mensagem);
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Não foi possível concluir a operação");
        alerta.setHeaderText("Verifique os dados informados");
        alerta.setContentText(mensagem);
        alerta.initOwner(tblProdutos.getScene().getWindow());
        alerta.show();
    }

    private void atualizarTabela() {
        tblProdutos.setItems(FXCollections.observableArrayList(cadastro.listarProdutos()));
    }
}
