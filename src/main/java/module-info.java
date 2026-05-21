module com.example.jsonschemagenerator {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.fasterxml.jackson.databind;

    opens com.example.jsonschemagenerator to javafx.fxml;
    exports com.example.jsonschemagenerator;
    exports com.example.jsonschemagenerator.views.controllers;
    exports com.example.jsonschemagenerator.views.components;
    opens com.example.jsonschemagenerator.views.controllers to javafx.fxml;
}