package projectUAS;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.sql.*;
import projectUAS.DBConnection; 

public class Login {

    // Method ini menerima Stage utama dan sebuah "Runnable" (aksi yang dilakukan jika login sukses)
    public static void show(Stage stage, Runnable onSuccess) {
        
        VBox root = new VBox(15);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        // Judul
        Label lblTitle = new Label("LOGIN BOS");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        lblTitle.setStyle("-fx-text-fill: #2c3e50;");

        // Input Username
        TextField txtUser = new TextField();
        txtUser.setPromptText("Username");
        txtUser.setPrefWidth(250);
        txtUser.setMaxWidth(300);
        txtUser.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        // Input Password
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Password");
        txtPass.setMaxWidth(300);
        txtPass.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        // Tombol Login
        Button btnLogin = new Button("LOGIN");
        btnLogin.setPrefWidth(300);
        btnLogin.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10; -fx-cursor: hand;");

        // Label Error
        Label lblError = new Label();
        lblError.setStyle("-fx-text-fill: red;");

        
        
     // --- LOGIKA LOGIN DENGAN DATABASE ---
        btnLogin.setOnAction(e -> {
            String userInput = txtUser.getText();
            String passInput = txtPass.getText();

            // Validasi input kosong
            if (userInput.isEmpty() || passInput.isEmpty()) {
                lblError.setText("Username dan Password harus diisi!");
                return;
            }

            // Cek ke Database
            try (Connection conn = DBConnection.getConnection()) {
                // Query mencari user dengan username DAN password yang cocok
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                
                ps.setString(1, userInput);
                ps.setString(2, passInput); // Catatan: Di aplikasi nyata, password sebaiknya di-hash (MD5/BCrypt)
                
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    // JIKA DITEMUKAN (rs.next() bernilai true)
                    System.out.println("Login Berhasil! User: " + rs.getString("username"));
                    onSuccess.run(); // Pindah ke Dashboard
                } else {
                    // JIKA TIDAK DITEMUKAN
                    lblError.setText("Username atau Password salah!");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                lblError.setText("Gagal koneksi database: " + ex.getMessage());
            }
        });

        root.getChildren().addAll(lblTitle, new Label(""), txtUser, txtPass, btnLogin, lblError);

        Scene scene = new Scene(root, 400, 500);
        stage.setTitle("Login - Penjualan HW");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
}