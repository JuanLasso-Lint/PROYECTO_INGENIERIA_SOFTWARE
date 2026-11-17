module org.example.royecttofinal {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires com.github.librepdf.openpdf;

    opens org.example.royecttofinal.app to javafx.fxml;
    opens org.example.royecttofinal.ViewController to javafx.fxml;
    opens org.example.royecttofinal.Model to javafx.base;

    exports org.example.royecttofinal.app;
    exports org.example.royecttofinal.Model;
    exports org.example.royecttofinal.Controller;
    exports org.example.royecttofinal.ViewController;
}