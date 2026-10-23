package org.sysproduto.model;

import java.util.ArrayList;

import org.sysproduto.utils.ArquivoUtil;
import java.io.IOException;

public class CadastroProduto {
    private ArrayList<Produto> produtos;

    public CadastroProduto() {
        try {
            produtos = new ArrayList<>(ArquivoUtil.carregarDados());
        }  catch (IOException e) {
            System.out.println("Erro ao carregar produtos:" + e.getMessage());
            produtos = new ArrayList<>();
        }
    }

    public void cadastrarProduto(Produto produto) {

        // verifica se o produto foi informado
        if (produto == null) {
            throw new IllegalArgumentException(
                    "O produto não pode ser nulo."
            );
        }

        // verifica se os dados dp produto são válidos
        produto.validar();

        // verifica se já existe um produto com o mesmo código
        for (Produto p : produtos) {
            if (p.getCodigo().equalsIgnoreCase(produto.getCodigo())) {
                throw new IllegalArgumentException(
                        "Já existe um produto cadastrado com esse código."
                );
            }
        }
        // Adiciona o produto á lista
        produtos.add(produto);

        // salva o produto no arquivo CSV
        try {
            ArquivoUtil.salvarDados(produtos);

        } catch (IOException e) {
            produtos.remove(produto);

            throw new IllegalStateException(
                    "Erro ao salvar o produto: " + e.getMessage(), e
            );
        }
    }



    // metodo para consultar produtos pelo código
    public Produto consultarProduto(String codigo) {
        for (Produto p : produtos) {
            if (p.getCodigo().equalsIgnoreCase(codigo)) {
                return p;
            }
        }
        return null;
    }

    // metodo para excluir produto pelo codigo
    public boolean excluirProduto(String codigo) {
        Produto produto = consultarProduto(codigo);

        if (produto != null) {
            int posicao = produtos.indexOf(produto);
            produtos.remove(produto);

            try {
                ArquivoUtil.salvarDados(produtos);

            } catch (IOException e) {
                produtos.add(posicao, produto);

                throw new IllegalStateException(
                        "Erro ao excluir o produto: " + e.getMessage(), e
                );
            }
            return true;
        }
        return false;
    }



    public ArrayList<Produto> listarProdutos() {
        return new ArrayList<>(produtos);
    }









}