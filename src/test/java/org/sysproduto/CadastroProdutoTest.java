package org.sysproduto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sysproduto.model.CadastroProduto;
import org.sysproduto.model.Categoria;
import org.sysproduto.model.Produto;
import org.sysproduto.utils.ArquivoUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class CadastroProdutoTest {
    @TempDir Path pasta;
    private String caminhoAnterior;

    @BeforeEach void preparar() {
        caminhoAnterior = System.getProperty("sysproduto.csv");
        System.setProperty("sysproduto.csv", pasta.resolve("produtos.csv").toString());
    }

    @AfterEach void restaurar() {
        if (caminhoAnterior == null) System.clearProperty("sysproduto.csv");
        else System.setProperty("sysproduto.csv", caminhoAnterior);
    }

    private Produto produto(String codigo, int dias, int estoque) {
        return new Produto(codigo, "Arroz", "Pacote", LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(dias), new BigDecimal("10.00"), new BigDecimal("20.00"),
                estoque, new Categoria(1, "Alimentos", "Alimentícios", "Supermercado"));
    }

    @Test void cadastrarConsultarAtualizarExcluirEPersistir() throws IOException {
        CadastroProduto cadastro = new CadastroProduto();
        assertNull(cadastro.getErroCarregamento());
        assertTrue(cadastro.listarProdutos().isEmpty());
        cadastro.cadastrarProduto(produto("ABC12345", 10, 5));
        assertNotNull(new CadastroProduto().consultarProduto(" abc12345 "));
        assertNull(cadastro.consultarProduto("INEXISTE"));
        assertThrows(IllegalArgumentException.class,
                () -> cadastro.cadastrarProduto(produto("abc12345", 20, 1)));
        Produto atualizado = produto("ABC12345", 20, 15);
        atualizado.setNome("Arroz integral");
        cadastro.atualizarProduto("ABC12345", atualizado);
        assertEquals("Arroz integral", new CadastroProduto().consultarProduto("ABC12345").getNome());
        cadastro.salvar();
        assertThrows(IllegalArgumentException.class,
                () -> cadastro.atualizarProduto("ABC12345", produto("OUTRO001", 10, 3)));
        assertThrows(IllegalArgumentException.class,
                () -> cadastro.atualizarProduto("AUSENTE1", atualizado));
        assertFalse(cadastro.excluirProduto("AUSENTE1"));
        assertTrue(cadastro.excluirProduto("abc12345"));
        assertTrue(new CadastroProduto().listarProdutos().isEmpty());
        assertEquals("", Files.readString(pasta.resolve("produtos.csv")));
    }

    @Test void validarTodosOsCamposObrigatoriosELimites() {
        List<Consumer<Produto>> invalidacoes = List.of(
                p -> p.setCodigo(null), p -> p.setCodigo("123"), p -> p.setCodigo("ABCD-123"),
                p -> p.setNome(null), p -> p.setNome("  "), p -> p.setNome("ab"),
                p -> p.setDataFabricacao(null), p -> p.setDataFabricacao(LocalDate.now().plusDays(1)),
                p -> p.setDataValidade(null), p -> p.setDataValidade(LocalDate.now().minusDays(31)),
                p -> p.setPrecoCompra(null), p -> p.setPrecoCompra(BigDecimal.ZERO),
                p -> p.setPrecoCompra(new BigDecimal("-1")), p -> p.setPrecoVenda(null),
                p -> p.setPrecoVenda(new BigDecimal("10")), p -> p.setPrecoVenda(new BigDecimal("-1")),
                p -> p.setQuantidadeEstoque(-1), p -> p.setCategoria(null));
        for (Consumer<Produto> invalidacao : invalidacoes) {
            Produto p = produto("ABC12345", 10, 0);
            invalidacao.accept(p);
            assertThrows(IllegalArgumentException.class, p::validar);
        }
        assertThrows(IllegalArgumentException.class, () -> new CadastroProduto().cadastrarProduto(null));
        Produto limite = produto("ABC12345", 0, 0);
        limite.setDataFabricacao(LocalDate.now());
        assertDoesNotThrow(limite::validar);
    }

    @Test void csvPreservaAcentosPontoEVirgulaAspasEQuebrasDeLinha() throws IOException {
        Produto p = produto("CSV12345", 60, 0);
        p.setNome("Café; especial");
        p.setDescricao("Descrição com \"aspas\"\ne segunda linha\r\nterceira");
        p.setCategoria(new Categoria(2, "Alimentos; bebidas", "Descrição \"especial\"", "Alimentação"));
        ArquivoUtil.salvarDados(List.of(p));
        Produto lido = ArquivoUtil.carregarDados().getFirst();
        assertEquals(p.getNome(), lido.getNome());
        assertEquals(p.getDescricao(), lido.getDescricao());
        assertEquals(p.getCategoria().getNome(), lido.getCategoria().getNome());
        assertEquals(p.getCategoria().getDescricao(), lido.getCategoria().getDescricao());
        assertEquals(p.getPrecoCompra(), lido.getPrecoCompra());
    }

    @Test void csvOriginalSemAspasEBomContinuamCompativeis() throws IOException {
        Produto p = produto("CSV12345", 1, 3);
        ArquivoUtil.salvarDados(List.of(p));
        Path csv = pasta.resolve("produtos.csv");
        Files.writeString(csv, "\uFEFF" + Files.readString(csv).replace("\n", "\r\n"));
        assertEquals("CSV12345", ArquivoUtil.carregarDados().getFirst().getCodigo());
    }

    @Test void csvInvalidoBloqueiaGravacaoESemPerderArquivoOriginal() throws IOException {
        Path csv = pasta.resolve("produtos.csv");
        for (String invalido : List.of("invalido;campos", "\"aspas não fechadas", "campo\"invalido")) {
            Files.writeString(csv, invalido);
            assertThrows(IOException.class, ArquivoUtil::carregarDados);
            CadastroProduto cadastro = new CadastroProduto();
            assertNotNull(cadastro.getErroCarregamento());
            assertThrows(IllegalStateException.class, cadastro::salvar);
            assertThrows(IllegalStateException.class,
                    () -> cadastro.cadastrarProduto(produto("ABC12345", 1, 2)));
            assertEquals(invalido, Files.readString(csv));
        }
    }

    @Test void csvRejeitaDatasNumerosEDuplicatasInvalidos() throws IOException {
        Path csv = pasta.resolve("produtos.csv");
        ArquivoUtil.salvarDados(List.of(produto("ABC12345", 1, 5)));
        String valido = Files.readString(csv);
        for (String invalido : List.of(valido + valido.toLowerCase(),
                valido.replace(LocalDate.now().plusDays(1).toString(), "2026-99-99"),
                valido.replace(";10.00;", ";invalido;"), valido.replace(";5;1;", ";-5;1;"))) {
            Files.writeString(csv, invalido);
            assertThrows(IOException.class, ArquivoUtil::carregarDados);
        }
    }

    @Test void falhaNaGravacaoReverteCadastroAtualizacaoEExclusao() throws IOException {
        CadastroProduto cadastro = new CadastroProduto();
        cadastro.cadastrarProduto(produto("ABC12345", 10, 5));
        String original = Files.readString(pasta.resolve("produtos.csv"));
        System.setProperty("sysproduto.csv", pasta.resolve("pasta-ausente/produtos.csv").toString());
        assertThrows(IllegalStateException.class,
                () -> cadastro.cadastrarProduto(produto("NOVO0001", 1, 2)));
        assertEquals(1, cadastro.listarProdutos().size());
        Produto alterado = produto("ABC12345", 20, 30);
        assertThrows(IllegalStateException.class, () -> cadastro.atualizarProduto("ABC12345", alterado));
        assertEquals(5, cadastro.consultarProduto("ABC12345").getQuantidadeEstoque());
        assertThrows(IllegalStateException.class, () -> cadastro.excluirProduto("ABC12345"));
        assertNotNull(cadastro.consultarProduto("ABC12345"));
        assertEquals(original, Files.readString(pasta.resolve("produtos.csv")));
    }

    @Test void relatoriosRespeitamLimitesDeSessentaDiasEDezUnidades() {
        CadastroProduto c = new CadastroProduto();
        c.cadastrarProduto(produto("VENCIDO1", -1, 10));
        c.cadastrarProduto(produto("HOJE0001", 0, 0));
        c.cadastrarProduto(produto("LIMITE60", 60, 9));
        c.cadastrarProduto(produto("FORA0061", 61, 10));
        assertEquals(List.of("HOJE0001", "LIMITE60"), c.produtosProximosVencimento().stream()
                .map(Produto::getCodigo).toList());
        assertEquals(List.of("HOJE0001", "LIMITE60"), c.produtosEstoqueBaixo().stream()
                .map(Produto::getCodigo).toList());
        c.listarProdutos().clear();
        assertEquals(4, c.listarProdutos().size());
    }
}
