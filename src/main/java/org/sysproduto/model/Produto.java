package org.sysproduto.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Produto {
    private String codigo;
    private String nome;
    private String descricao;
    private LocalDate dataFabricacao;
    private LocalDate dataValidade;
    private BigDecimal precoCompra;
    private BigDecimal precoVenda;
    private int quantidadeEstoque;
    private Categoria categoria;

    // construtor sem parametros

    public Produto() {
    }

    // construtor com paramretros
    public Produto(String codigo, String nome, String descricao,
                   LocalDate dataFabricacao, LocalDate dataValidade, BigDecimal precoCompra,
                   BigDecimal precoVenda, int quantidadeEstoque, Categoria categoria) {

        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
        this.dataFabricacao = dataFabricacao;
        this.dataValidade = dataValidade;
        this.precoCompra = precoCompra;
        this.precoVenda = precoVenda;
        this.quantidadeEstoque = quantidadeEstoque;
        this.categoria = categoria;
    }
    // metodos get e setter
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataFabricacao() {
        return dataFabricacao;
    }

    public void setDataFabricacao(LocalDate dataFabricacao) {
        this.dataFabricacao = dataFabricacao;
    }

    public LocalDate getDataValidade() {
        return dataValidade;
    }

    public void setDataValidade(LocalDate dataValidade) {
        this.dataValidade = dataValidade;
    }

    public BigDecimal getPrecoCompra() {
        return precoCompra;
    }

    public void setPrecoCompra(BigDecimal precoCompra) {
        this.precoCompra = precoCompra;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public int getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(int quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public void validar() {

        // validação do Código
        if (codigo == null || !codigo.matches( "[a-zA-Z0-9]{8}" ) ) {
            throw new IllegalArgumentException(
                    "O código deve conter 8 caracteres alfanuméricos."
            );
        }

        //validação do nome
        if (nome == null || nome.trim().length() <3) {
            throw new IllegalArgumentException(
                    "O nome deve conter pelo menos 3 caracteres."
            );
        }

        // validação da data de fabricação
        if (dataFabricacao == null || dataFabricacao.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "A data de fabricação não pode ser futura."
            );
        }

        // validação da data de validade
        if (dataValidade == null || dataValidade.isBefore(dataFabricacao)) {
            throw new IllegalArgumentException(
                    "A data de validade não pode ser anterior á fabricação."
            );
        }

        // validação do preço de compra
        if (precoCompra == null || precoCompra.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O preço de compra deve ser maior que zero."
            );
        }
        // validação do preço de venda
        if (precoVenda == null || precoVenda.compareTo(precoCompra) <= 0) {
            throw new IllegalArgumentException(
                    "O preço de venda deve ser maior que o preço de compra."
            );
        }

        // Validação da quantidade em estoque
        if (quantidadeEstoque < 0) {
            throw new IllegalArgumentException (
                    "A quantidade em estoque não pode ser negativa."
            );
        }

        // Validação da categoria
        if (categoria == null || categoria.getNome() == null || categoria.getNome().isBlank()) {
            throw new IllegalArgumentException(
                    "O produto deve possuir uma categoria."
            );
        }
    }


}
