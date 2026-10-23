module org.sysproduto {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.sysproduto to javafx.fxml;
    exports org.sysproduto;
}