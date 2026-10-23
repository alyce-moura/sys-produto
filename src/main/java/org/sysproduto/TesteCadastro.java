
package org.sysproduto;

import org.sysproduto.model.CadastroProduto;
import org.sysproduto.model.Categoria;
import org.sysproduto.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TesteCadastro {

    public static void main(String[] args) {

        // Criando uma categoria
        Categoria categoria = new Categoria(
                1,
                "Alimentos",
                "Produtos alimentícios",
                "Supermercado"
        );

        // Criando um produto
        Produto produto = new Produto(
                "ABC12345",
                "Arroz Branco",
                "Pacote de arroz de 5kg",
                LocalDate.of(2026, 9, 10),
                LocalDate.of(2027, 9, 10),
                new BigDecimal("15.00"),
                new BigDecimal("22.00"),
                50,
                categoria
        );

        // Criando o cadastro
        CadastroProduto cadastro = new CadastroProduto();

        // cadastro.cadastrarProduto(produto);
       // cadastro.cadastrarProduto(produto);



       // boolean excluido = cadastro.excluirProduto("ABC12345");

        //if (excluido) {
        //    System.out.println("Produto excluído com sucesso!");
        //} else {
          //  System.out.println("Produto não encontrado para exclusão!");
       // }




        // Consultando o produto carregado do CSV
        Produto encontrado = cadastro.consultarProduto("ABC12345");

        if (encontrado != null) {
            System.out.println("Produto carregado com sucesso!");
            System.out.println("Nome: " + encontrado.getNome());
            System.out.println("Código: " + encontrado.getCodigo());
        } else {
            System.out.println("Produto não encontrado!");
        }
    }
}
