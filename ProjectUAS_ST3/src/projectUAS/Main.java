package projectUAS;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // --- PERUBAHAN DISINI ---
        // Saat aplikasi mulai, jangan langsung buka dashboard.
        // Panggil Login dulu. Jika login sukses, baru panggil showDashboard.
        
        Login.show(primaryStage, () -> {
            showDashboard(primaryStage); // Ini akan dijalankan jika login berhasil
        });
    }

    // --- KODE DASHBOARD LAMA DIPINDAHKAN KE SINI ---
    public void showDashboard(Stage primaryStage) {
        VBox root = new VBox(30);
        root.setAlignment(Pos.CENTER);

        try {
            // Path gambar
            Image bgImg = new Image(getClass().getResourceAsStream("Background_merah.png")); 

            if (bgImg.isError()) {
                System.out.println("Gagal memuat gambar. Pastikan file ada di src/application.img/");
            }

            BackgroundImage bImg = new BackgroundImage(
                bgImg,
                BackgroundRepeat.NO_REPEAT, 
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,    
                new BackgroundSize(100, 100, true, true, true, false) 
            );

            root.setBackground(new Background(bImg));
        } catch (Exception e) {
            System.out.println("Peringatan: Gambar background tidak ditemukan.");
            root.setStyle("-fx-background-color: #ecf0f1;");
        }

        
        Label lblTitle = new Label("PENJUALAN HW");
        lblTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

        TilePane menuContainer = new TilePane();
        menuContainer.setHgap(20);
        menuContainer.setVgap(20);
        menuContainer.setPrefColumns(2);
        menuContainer.setAlignment(Pos.CENTER);

        Button btnBarang = createMenuButton("📦 DATA BARANG");
        Button btnTransaksi = createMenuButton("🛒 TRANSAKSI BARU");
        Button btnLaporan = createMenuButton("📊 LAPORAN");
        Button btnExit = createMenuButton("❌ KELUAR");

       
        btnBarang.setOnAction(e -> BarangCRUD.show());
        btnTransaksi.setOnAction(e -> TransaksiCRUD.show());
        btnLaporan.setOnAction(e -> TransaksiLaporan.show());
        btnExit.setOnAction(e -> primaryStage.close());

        menuContainer.getChildren().addAll(btnBarang, btnTransaksi, btnLaporan, btnExit);
        root.getChildren().addAll(lblTitle, menuContainer);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Penjualan HW - Dashboard");
        primaryStage.setScene(scene);
        
        // Agar posisi window tetap di tengah saat ganti scene dari login ke dashboard
        primaryStage.centerOnScreen(); 
        primaryStage.show();
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setPrefSize(220, 110);
      
        btn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: rgba(52, 152, 219, 0.9); -fx-text-fill: white; -fx-background-radius: 15; -fx-cursor: hand;");
        return btn;
    }

    public static void main(String[] args) {
        launch(args);
    }
}