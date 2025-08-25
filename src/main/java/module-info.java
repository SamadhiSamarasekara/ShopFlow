module shopmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.base;

    exports com.shop;
    exports com.shop.controller;
    exports com.shop.model;
    
    opens com.shop to javafx.fxml;
    opens com.shop.controller to javafx.fxml;
    opens com.shop.model to javafx.base;
}
