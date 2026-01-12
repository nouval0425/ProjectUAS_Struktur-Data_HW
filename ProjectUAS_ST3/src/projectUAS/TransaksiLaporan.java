package projectUAS;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.sql.*;

public class TransaksiLaporan {

    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("Laporan Penjualan HW");

        VBox root = new VBox();
        root.setPadding(new Insets(20));
        root.setSpacing(15);
        // Style background agar putih bersih saat diprint
        root.setStyle("-fx-background-color: white;"); 

        Label title = new Label("Riwayat Transaksi Penjualan");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TableView<LaporanItem> table = createReportTable();
        loadReportData(table.getItems());

        // --- TOMBOL PRINT ---
        Button btnPrint = new Button("Cetak Laporan / PDF");
        btnPrint.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Aksi ketika tombol print ditekan (Mencetak seluruh isi VBox root)
        btnPrint.setOnAction(e -> printLaporan(root, stage));

        root.getChildren().addAll(title, btnPrint, table);

        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    // --- FUNGSI UNTUK MENCETAK ---
    private static void printLaporan(Node nodeToPrint, Stage owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        
        if (job != null) {
            // Tampilkan dialog print native OS
            boolean proceed = job.showPrintDialog(owner);
            
            if (proceed) {
                // 1. Dapatkan ukuran halaman printer
                PageLayout pageLayout = job.getJobSettings().getPageLayout();
                double printableWidth = pageLayout.getPrintableWidth();
                double printableHeight = pageLayout.getPrintableHeight();

                // 2. Hitung skala agar Node muat di lebar kertas (Fit to Width)
                double nodeWidth = nodeToPrint.getBoundsInParent().getWidth();
                double scaleX = printableWidth / nodeWidth;
                
                // Gunakan skala terkecil agar tidak terpotong (biasanya scaleX)
                // Kita batasi maksimal skala 1.0 (agar tidak membesar jika tabel kecil)
                double scaleFactor = Math.min(scaleX, 1.0);

                // 3. Terapkan Transformasi Skala
                Scale scale = new Scale(scaleFactor, scaleFactor);
                nodeToPrint.getTransforms().add(scale);

                // 4. Lakukan Print
                boolean success = job.printPage(nodeToPrint);
                
                // 5. Hapus Transformasi Skala (Kembalikan tampilan asli di layar)
                nodeToPrint.getTransforms().remove(scale);

                if (success) {
                    job.endJob(); // Selesai print
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Info");
                    alert.setHeaderText(null);
                    alert.setContentText("Laporan berhasil dikirim ke printer.");
                    alert.showAndWait();
                }
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Tidak dapat menemukan layanan printer.");
            alert.show();
        }
    }

    private static TableView<LaporanItem> createReportTable() {
        TableView<LaporanItem> table = new TableView<>();
        
        // Agar tabel mengisi ruang saat di-print
        table.setPrefHeight(1000); 

        TableColumn<LaporanItem, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());
        colId.setPrefWidth(50);

        TableColumn<LaporanItem, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(data -> data.getValue().customerProperty());

        TableColumn<LaporanItem, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(data -> data.getValue().tanggalProperty());
        
        TableColumn<LaporanItem, String> colNamaBarang = new TableColumn<>("Barang");
        colNamaBarang.setCellValueFactory(data -> data.getValue().namaBarangProperty());
        colNamaBarang.setPrefWidth(200);

        TableColumn<LaporanItem, Integer> colJumlah = new TableColumn<>("Qty");
        colJumlah.setCellValueFactory(data -> data.getValue().jumlahBarangProperty().asObject());

        TableColumn<LaporanItem, Double> colTotal = new TableColumn<>("Total Bayar");
        colTotal.setCellValueFactory(data -> data.getValue().totalProperty().asObject());
        
        table.getColumns().addAll(colId, colCustomer, colTanggal, colNamaBarang, colJumlah, colTotal);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        return table;
    }

    private static void loadReportData(ObservableList<LaporanItem> dataList) {
        dataList.clear();
        String sql = "SELECT * FROM transaksi ORDER BY tanggal DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                dataList.add(new LaporanItem(
                    rs.getInt("id_transaksi"),
                    rs.getString("nama_customer"),
                    rs.getTimestamp("tanggal").toString(),
                    rs.getString("nama_barang"),   
                    rs.getInt("jumlah_barang"),    
                    rs.getDouble("total_bayar")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- Model Item ---
    public static class LaporanItem {
        private final IntegerProperty id;
        private final StringProperty customer;
        private final StringProperty tanggal;
        private final StringProperty namaBarang;
        private final IntegerProperty jumlahBarang;
        private final DoubleProperty total;

        public LaporanItem(int id, String customer, String tanggal, String namaBarang, int jumlahBarang, double total) {
            this.id = new SimpleIntegerProperty(id);
            this.customer = new SimpleStringProperty(customer);
            this.tanggal = new SimpleStringProperty(tanggal);
            this.namaBarang = new SimpleStringProperty(namaBarang == null ? "-" : namaBarang);
            this.jumlahBarang = new SimpleIntegerProperty(jumlahBarang);
            this.total = new SimpleDoubleProperty(total);
        }

        public IntegerProperty idProperty() { return id; }
        public StringProperty customerProperty() { return customer; }
        public StringProperty tanggalProperty() { return tanggal; }
        public StringProperty namaBarangProperty() { return namaBarang; }
        public IntegerProperty jumlahBarangProperty() { return jumlahBarang; }
        public DoubleProperty totalProperty() { return total; }
    }
}