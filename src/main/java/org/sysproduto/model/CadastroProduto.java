package org.sysproduto.model;

import org.sysproduto.utils.ArquivoUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CadastroProduto {
    private final ArrayList<Produto> produtos = new ArrayList<>();
    private String erroCarregamento;

    public CadastroProduto() {
        try {
            produtos.addAll(ArquivoUtil.carregarDados());
        } catch (IOException e) {
            // Não permita sobrescrever um arquivo que não foi carregado corretamente.
            erroCarregamento = e.getMessage();
        }
    }

    public String getErroCarregamento() {
        return erroCarregamento;
    }

    public void cadastrarProduto(Produto produto) {
        verificarCarregamento();
        validarProduto(produto);
        if (consultarProduto(produto.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe um produto cadastrado com esse código.");
        }
        produtos.add(produto);
        try {
            salvar();
        } catch (IllegalStateException e) {
            produtos.remove(produto);
            throw e;
        }
    }

    public Produto consultarProduto(String codigo) {
        if (codigo == null) {
            return null;
        }
        return produtos.stream()
                .filter(p -> p.getCodigo().equalsIgnoreCase(codigo.trim()))
                .findFirst().orElse(null);
    }

    public void atualizarProduto(String codigoOriginal, Produto atualizado) {
        verificarCarregamento();
        validarProduto(atualizado);
        Produto original = consultarProduto(codigoOriginal);
        if (original == null) {
            throw new IllegalArgumentException("Selecione um produto existente para atualizar.");
        }
        if (!original.getCodigo().equalsIgnoreCase(atualizado.getCodigo())) {
            throw new IllegalArgumentException("O código identifica o produto e não pode ser alterado.");
        }
        int posicao = produtos.indexOf(original);
        produtos.set(posicao, atualizado);
        try {
            salvar();
        } catch (IllegalStateException e) {
            produtos.set(posicao, original);
            throw e;
        }
    }

    public boolean excluirProduto(String codigo) {
        verificarCarregamento();
        Produto produto = consultarProduto(codigo);
        if (produto == null) {
            return false;
        }
        int posicao = produtos.indexOf(produto);
        produtos.remove(posicao);
        try {
            salvar();
        } catch (IllegalStateException e) {
            produtos.add(posicao, produto);
            throw e;
        }
        return true;
    }

    public void salvar() {
        verificarCarregamento();
        try {
            ArquivoUtil.salvarDados(produtos);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar os produtos: " + e.getMessage(), e);
        }
    }

    private void verificarCarregamento() {
        if (erroCarregamento != null) {
            throw new IllegalStateException("O CSV não foi carregado. Corrija o arquivo e reinicie o sistema. "
                    + erroCarregamento);
        }
    }

    private void validarProduto(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("O produto não pode ser nulo.");
        }
        produto.validar();
    }

    public ArrayList<Produto> listarProdutos() {
        return new ArrayList<>(produtos);
    }

    public List<Produto> produtosProximosVencimento() {
        LocalDate hoje = LocalDate.now();
        return produtos.stream()
                .filter(p -> !p.getDataValidade().isBefore(hoje)
                        && !p.getDataValidade().isAfter(hoje.plusDays(60)))
                .toList();
    }

    public List<Produto> produtosEstoqueBaixo() {
        return produtos.stream()
                .filter(p -> p.getQuantidadeEstoque() < 10)
                .toList();
    }
}
