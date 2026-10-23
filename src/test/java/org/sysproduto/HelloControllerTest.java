package org.sysproduto;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;
import org.sysproduto.model.Produto;
import org.sysproduto.model.Categoria;
import org.sysproduto.utils.ArquivoUtil;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "DISPLAY", matches = ".+")
class HelloControllerTest {
    @TempDir Path pasta;
    private Parent raiz;
    private Stage janela;
    private String caminhoAnterior;

    @BeforeAll static void iniciarJavaFX() throws Exception {
        CountDownLatch pronto = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            pronto.countDown();
        });
        assertTrue(pronto.await(15, TimeUnit.SECONDS));
    }

    private static <T> T naInterface(Callable<T> acao) throws Exception {
        FutureTask<T> tarefa = new FutureTask<>(acao);
        Platform.runLater(tarefa);
        return tarefa.get(20, TimeUnit.SECONDS);
    }

    @BeforeEach void preparar() {
        caminhoAnterior = System.getProperty("sysproduto.csv");
        System.setProperty("sysproduto.csv", pasta.resolve("produtos.csv").toString());
    }

    @AfterEach void limpar() throws Exception {
        naInterface(() -> {
            for (Window w : List.copyOf(Window.getWindows())) w.hide();
            return null;
        });
        if (caminhoAnterior == null) System.clearProperty("sysproduto.csv");
        else System.setProperty("sysproduto.csv", caminhoAnterior);
    }

    private void abrir() throws Exception {
        raiz = FXMLLoader.load(HelloApplication.class.getResource("main-view.fxml"));
        janela = new Stage();
        janela.setScene(new Scene(raiz, 1000, 650));
        janela.show();
    }

    private TextField campo(String id) { return (TextField) raiz.lookup("#" + id); }
    @SuppressWarnings("unchecked")
    private TableView<Produto> tabela() { return (TableView<Produto>) raiz.lookup("#tblProdutos"); }
    private void clicar(String id) { ((Button) raiz.lookup("#" + id)).fire(); }
    private String status() { return ((Label) raiz.lookup("#lblStatus")).getText(); }
    private void fecharDialogos() {
        for (Window w : List.copyOf(Window.getWindows())) if (w != janela) w.hide();
    }

    private void preencher(String codigo) {
        clicar("btnNovo");
        campo("txtCodigo").setText(codigo);
        campo("txtNome").setText("Arroz branco");
        ((TextArea) raiz.lookup("#txtDescricao")).setText("Pacote; \"especial\"\n5 kg");
        ((DatePicker) raiz.lookup("#dpDataFabricacao")).setValue(LocalDate.now().minusDays(2));
        ((DatePicker) raiz.lookup("#dpDataValidade")).setValue(LocalDate.now().plusDays(30));
        campo("txtPrecoCompra").setText("15,00");
        campo("txtPrecoVenda").setText("22,50");
        campo("txtQuantidadeEstoque").setText("5");
        ((ComboBox<?>) raiz.lookup("#cbCategoria")).getSelectionModel().select(1);
    }

    @Test void eventosCrudConsultaRelatoriosEPersistencia() throws Exception {
        naInterface(() -> {
            abrir();
            clicar("btnNovo");
            preencher("ARROZ001");
            clicar("btnCadastrar");
            assertEquals(1, tabela().getItems().size());
            assertEquals("ARROZ001", tabela().getColumns().getFirst().getCellData(0));
            assertTrue(campo("txtCodigo").getText().isEmpty());
            assertEquals("Pacote; \"especial\"\n5 kg", ArquivoUtil.carregarDados().getFirst().getDescricao());
            preencher("ARROZ002");
            clicar("btnCadastrar");
            assertEquals(2, tabela().getItems().size());
            campo("txtConsulta").setText("arroz001");
            clicar("btnConsultar");
            assertEquals(1, tabela().getItems().size());
            assertEquals("ARROZ001", campo("txtCodigo").getText());
            assertFalse(campo("txtCodigo").isEditable());
            campo("txtNome").setText("Arroz integral");
            campo("txtQuantidadeEstoque").setText("9");
            clicar("btnAtualizar");
            assertEquals(2, tabela().getItems().size());
            assertEquals("Arroz integral", ArquivoUtil.carregarDados().getFirst().getNome());
            assertEquals(9, ArquivoUtil.carregarDados().getFirst().getQuantidadeEstoque());
            clicar("btnSalvar");
            assertTrue(status().startsWith("Lista salva"));
            clicar("btnRelatorios");
            DialogPane relatorio = (DialogPane) Window.getWindows().stream()
                    .filter(w -> w != janela).findFirst().orElseThrow().getScene().getRoot();
            String texto = ((TextArea) relatorio.getContent()).getText();
            assertTrue(texto.contains("PRÓXIMOS AO VENCIMENTO"));
            assertTrue(texto.contains("ESTOQUE BAIXO"));
            assertTrue(texto.contains("Arroz integral"));
            fecharDialogos();
            janela.close();
            abrir(); // Novo controlador deve carregar os dois produtos do arquivo.
            assertEquals(2, tabela().getItems().size());
            tabela().getSelectionModel().selectFirst();
            Platform.runLater(() -> responderConfirmacao(ButtonType.NO));
            clicar("btnExcluir");
            assertEquals(2, tabela().getItems().size());
            Platform.runLater(() -> responderConfirmacao(ButtonType.YES));
            clicar("btnExcluir");
            assertEquals(1, tabela().getItems().size());
            assertEquals("ARROZ002", ArquivoUtil.carregarDados().getFirst().getCodigo());
            clicar("btnListarTodos");
            assertEquals(1, tabela().getItems().size());
            assertTrue(campo("txtCodigo").isEditable());
            assertNull(((ComboBox<?>) raiz.lookup("#cbCategoria")).getValue());
            return null;
        });
    }

    private void responderConfirmacao(ButtonType resposta) {
        DialogPane painel = (DialogPane) Window.getWindows().stream()
                .filter(w -> w != janela).findFirst().orElseThrow().getScene().getRoot();
        ((Button) painel.lookupButton(resposta)).fire();
    }

    @Test void errosDoFormularioNaoAlteramTabelaNemCsv() throws Exception {
        naInterface(() -> {
            abrir();
            clicar("btnAtualizar");
            assertTrue(status().contains("Selecione"));
            fecharDialogos();
            clicar("btnExcluir");
            assertTrue(status().contains("Selecione"));
            fecharDialogos();
            clicar("btnConsultar");
            assertTrue(status().contains("Informe o código"));
            fecharDialogos();
            campo("txtConsulta").setText("AUSENTE1");
            clicar("btnConsultar");
            assertTrue(status().contains("Nenhum produto"));
            fecharDialogos();
            preencher("ABC12345");
            campo("txtPrecoCompra").setText("abc");
            clicar("btnCadastrar");
            assertTrue(status().contains("preços numéricos"));
            fecharDialogos();
            preencher("ABC12345");
            ((DatePicker) raiz.lookup("#dpDataFabricacao")).getEditor().setText("99/99/9999");
            clicar("btnCadastrar");
            assertTrue(status().contains("Data inválida"), status());
            fecharDialogos();
            preencher("ABC12345");
            ((ComboBox<?>) raiz.lookup("#cbCategoria")).setValue(null);
            clicar("btnCadastrar");
            assertTrue(status().contains("categoria"));
            fecharDialogos();
            assertTrue(tabela().getItems().isEmpty());
            assertFalse(Files.exists(pasta.resolve("produtos.csv")));
            preencher("ABC12345");
            clicar("btnCadastrar");
            preencher("abc12345");
            clicar("btnCadastrar");
            assertTrue(status().contains("Já existe"));
            fecharDialogos();
            assertEquals(1, tabela().getItems().size());
            assertEquals(1, ArquivoUtil.carregarDados().size());
            return null;
        });
    }

    @Test void arquivoInvalidoEhInformadoEBotoesDeGravacaoFicamDesabilitados() throws Exception {
        Files.writeString(pasta.resolve("produtos.csv"), "CSV inválido");
        naInterface(() -> {
            abrir();
            for (String id : List.of("btnCadastrar", "btnAtualizar", "btnExcluir", "btnSalvar")) {
                assertTrue(((Button) raiz.lookup("#" + id)).isDisabled());
            }
            assertTrue(status().contains("CSV não carregado"));
            return null;
        });
        // Aguarda também a mensagem agendada pelo initialize().
        naInterface(() -> { fecharDialogos(); return null; });
        assertEquals("CSV inválido", Files.readString(pasta.resolve("produtos.csv")));
    }
}
