module com.example.oodproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;
    requires java.desktop;
    requires javafx.swing;
    requires itextpdf;
    //requires com.itextpdf;  // এটি পরিবর্তন করুন

    opens com.example.oodproject to javafx.fxml;
    exports com.example.oodproject;
}