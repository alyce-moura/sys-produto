package org.sysproduto.utils;

import org.sysproduto.model.Produto;
import org.sysproduto.model.Categoria;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ArquivoUtil {
    private static Path caminho() {
        // Por padrão, mantém o arquivo original na pasta de execução do projeto.
        // A propriedade permite testes isolados sem tocar nos dados do usuário.
        return Path.of(System.getProperty("sysproduto.csv", "produtos.csv"));
    }

    public static void salvarDados(List<Produto> produtos) throws IOException {
        List<String> linhas = new ArrayList<>();
        for (Produto p : produtos) {
            p.validar();
            linhas.add(String.join(";",
                    campo(p.getCodigo()), campo(p.getNome()), campo(p.getDescricao()),
                    p.getDataFabricacao().toString(), p.getDataValidade().toString(),
                    p.getPrecoCompra().toPlainString(), p.getPrecoVenda().toPlainString(),
                    String.valueOf(p.getQuantidadeEstoque()),
                    String.valueOf(p.getCategoria().getId()), campo(p.getCategoria().getNome()),
                    campo(p.getCategoria().getDescricao()), campo(p.getCategoria().getSetor())));
        }
        Path destino = caminho().toAbsolutePath();
        Path temporario = Files.createTempFile(destino.getParent(), "produtos-", ".tmp");
        try {
            Files.write(temporario, linhas, StandardCharsets.UTF_8);
            try {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporario);
        }
    }

    private static String campo(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n") || valor.contains("\r")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public static List<Produto> carregarDados() throws IOException {
        List<Produto> produtos = new ArrayList<>();
        if (!Files.exists(caminho())) {
            return produtos;
        }
        String conteudo = Files.readString(caminho(), StandardCharsets.UTF_8);
        if (conteudo.startsWith("\uFEFF")) {
            conteudo = conteudo.substring(1);
        }
        List<List<String>> registros = lerRegistros(conteudo);
        Set<String> codigos = new HashSet<>();
        for (int i = 0; i < registros.size(); i++) {
            List<String> dados = registros.get(i);
            try {
                if (dados.size() != 12) {
                    throw new IllegalArgumentException("Esperados 12 campos separados por ponto-e-vírgula.");
                }
                Categoria categoria = new Categoria(Integer.parseInt(dados.get(8)),
                        dados.get(9), dados.get(10), dados.get(11));
                Produto produto = new Produto(dados.get(0), dados.get(1), dados.get(2),
                        LocalDate.parse(dados.get(3)), LocalDate.parse(dados.get(4)),
                        new BigDecimal(dados.get(5)), new BigDecimal(dados.get(6)),
                        Integer.parseInt(dados.get(7)), categoria);
                produto.validar();
                if (!codigos.add(produto.getCodigo().toUpperCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Código de produto duplicado.");
                }
                produtos.add(produto);
            } catch (RuntimeException e) {
                throw new IOException("Registro " + (i + 1) + " inválido no CSV: " + e.getMessage(), e);
            }
        }
        return produtos;
    }

    // CSV com delimitador ';', aspas escapadas e quebras de linha em campos entre aspas.
    private static List<List<String>> lerRegistros(String texto) throws IOException {
        List<List<String>> registros = new ArrayList<>();
        List<String> registro = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreAspas = false;
        boolean aposAspas = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (entreAspas) {
                if (c == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        entreAspas = false;
                        aposAspas = true;
                    }
                } else {
                    campo.append(c);
                }
            } else if (c == ';' || c == '\n' || c == '\r') {
                registro.add(campo.toString());
                campo.setLength(0);
                aposAspas = false;
                if (c != ';') {
                    if (!(registro.size() == 1 && registro.getFirst().isEmpty())) {
                        registros.add(registro);
                    }
                    registro = new ArrayList<>();
                    if (c == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                        i++;
                    }
                }
            } else if (c == '"' && campo.isEmpty() && !aposAspas) {
                entreAspas = true;
            } else {
                if (aposAspas || c == '"') {
                    throw new IOException("CSV inválido: aspas fora de posição.");
                }
                campo.append(c);
            }
        }
        if (entreAspas) {
            throw new IOException("CSV inválido: campo com aspas não fechadas.");
        }
        if (!registro.isEmpty() || !campo.isEmpty() || aposAspas) {
            registro.add(campo.toString());
            registros.add(registro);
        }
        return registros;
    }
}
