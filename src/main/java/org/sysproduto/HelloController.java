package org.sysproduto;

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
import org.sysproduto.model.CadastroProduto;

import javafx.collections.FXCollections;

public class HelloController {

    // gerenciamento de produtos cadastrados
    private CadastroProduto cadastro = new  CadastroProduto();

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
    }

    // METODOS DOS BOTOES
    // ação do botao cadastrar
    @FXML
    private void onClickCadastrar() {
        // recupera o codigo cadastrado
        String codigo = txtCodigo.getText().trim();

        // recupera o nome digitado
        String nome = txtNome.getText().trim();

        // recupera a descrição do produto
        String descricao = txtDescricao.getText().trim();

        // reucpera as datas selecionadas
        LocalDate dataFabricacao = dpDataFabricacao.getValue();
        LocalDate dataValidade = dpDataValidade.getValue();

        // recupera os preços digitados
        BigDecimal precoCompra = new BigDecimal(
                txtPrecoCompra.getText().trim().replace(",", "."));
        BigDecimal precoVenda = new BigDecimal(
                txtPrecoVenda.getText().trim().replace(",", "."));

        // recupera a quantidade em estoque
        int quantidadeEstoque = Integer.parseInt(
                txtQuantidadeEstoque.getText().trim());

        // recupera a categoria selecionada
        Categoria categoria = cbCategoria.getValue();
    }









    // METODOS AUXILIARES
    // Atualiza a tabela com os produtos cadastrados
    private void atualizarTabela() {
        tblProdutos.setItems(
           FXCollections.observableArrayList(cadastro.listarProdutos())
        );
    }














}
