package org.sysproduto.utils;

import org.sysproduto.model.Produto;
import org.sysproduto.model.Categoria;

import java.time.LocalDate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

import java.nio.file.StandardCopyOption;


public class ArquivoUtil {
    private static final Path CAMINHO = Path.of("produtos.csv");

    // metodo salvar dados
    public static void salvarDados(List<Produto> produtos) throws IOException {
        List<String> linhas = new ArrayList<>();
        for (Produto p: produtos) {

            String linha = String.join(";",
                    p.getCodigo(),
                    p.getNome(),
                    p.getDescricao(),
                    p.getDataFabricacao().toString(),
                    p.getDataValidade().toString(),
                    p.getPrecoCompra().toString(),
                    p.getPrecoVenda().toString(),
                    String.valueOf(p.getQuantidadeEstoque()),
                    String.valueOf(p.getCategoria().getId()),
                    p.getCategoria().getNome(),
                    p.getCategoria().getDescricao(),
                    p.getCategoria().getSetor()
            );
            linhas.add(linha);
        }

        // Cria um arquivo temporário
        Path temporario = Files.createTempFile(
                CAMINHO.toAbsolutePath().getParent(),
                "produtos-",
                ".tmp"
        );

        try {

            // Salva os dados no arquivo temporário
            Files.write(
                    temporario,
                    linhas,
                    StandardCharsets.UTF_8
            );

            // Substitui o CSV original pelo arquivo temporário
            Files.move(
                    temporario,
                    CAMINHO,
                    StandardCopyOption.REPLACE_EXISTING
            );

        } finally {

            // Remove o arquivo temporário, caso ainda exista
            Files.deleteIfExists(temporario);
        }
    }

    // metodo carregardados
    public static List<Produto> carregarDados() throws IOException {
        List<Produto> produtos = new ArrayList<>();
        if (!Files.exists(CAMINHO)) {
            return produtos;
        }
        List<String> linhas = Files.readAllLines(
                CAMINHO, StandardCharsets.UTF_8
        );

        for (String linha : linhas) {
            String[] dados = linha.split(";", -1);

            if (dados.length != 12) {
                throw new IOException(
                        "Linha inválida no arquivo CSV: " + linha
                );
            }
            // conversao dos dados do produto
            String codigo = dados[0];
            String nome = dados[1];
            String descricao = dados[2];

            LocalDate dataFabricacao = LocalDate.parse(dados[3]);
            LocalDate dataValidade = LocalDate.parse(dados[4]);

            BigDecimal precoCompra = new BigDecimal(dados[5]);
            BigDecimal precoVenda = new BigDecimal(dados[6]);

            int quantidadeEstoque = Integer.parseInt(dados[7]);

            // conversao de dados da categoria
            int idCategoria = Integer.parseInt(dados[8]);
            String nomeCategoria = dados[9];
            String descricaoCategoria = dados[10];
            String setorCategoria = dados[11];

            // categoria
            Categoria categoria = new Categoria(
                    idCategoria,
                    nomeCategoria,
                    descricaoCategoria,
                    setorCategoria
            );

            // criando o produto
            Produto produto = new Produto(
                    codigo,
                    nome,
                    descricao,
                    dataFabricacao,
                    dataValidade,
                    precoCompra,
                    precoVenda,
                    quantidadeEstoque,
                    categoria
            );
            // validando e adicionando á lisra
            produto.validar();
            produtos.add(produto);







        }
        return produtos;
    }













}
