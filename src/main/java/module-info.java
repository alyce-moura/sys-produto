module org.sysproduto {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.sysproduto to javafx.fxml;
    opens org.sysproduto.model to javafx.base;
    exports org.sysproduto;
}