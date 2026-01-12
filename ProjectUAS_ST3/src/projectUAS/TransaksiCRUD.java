package projectUAS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransaksiCRUD {
    // --- VARIABEL GLOBAL (STATIC) ---
    // Dipindahkan ke sini agar bisa diakses oleh simpanKeDatabase
    private static TableView<Transaksi.KeranjangItem> tableKeranjang = new TableView<>();
    private static ObservableList<Transaksi.KeranjangItem> dataKeranjang = FXCollections.observableArrayList();
    private static Label lblTotal = new Label("Total: Rp 0");
    private static TextField txtCustomer = new TextField(); // <--- PINDAHKAN INI KE SINI
    private static double totalBayar = 0;

    public static void show() {
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Reset Textfield setiap kali menu dibuka
        txtCustomer.clear(); 
        
        // Input Area
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        // txtCustomer sudah dideklarasikan di atas, jadi langsung pakai saja
        ComboBox<Barang> cbBarang = new ComboBox<>(loadBarang());
        TextField txtJumlah = new TextField();
        Button btnTambah = new Button("Tambah ke Keranjang");

        grid.add(new Label("Nama Customer:"), 0, 0); grid.add(txtCustomer, 1, 0);
        grid.add(new Label("Pilih Barang:"), 0, 1);    grid.add(cbBarang, 1, 1);
        grid.add(new Label("Jumlah:"), 0, 2);          grid.add(txtJumlah, 1, 2);
        grid.add(btnTambah, 1, 3);

        // Table Keranjang
        setupTable();

        // Logika Tambah ke Keranjang
        btnTambah.setOnAction(e -> {
            try {
                Barang b = cbBarang.getValue();
                int qty = Integer.parseInt(txtJumlah.getText());
                if (b != null && qty > 0) {
                    Transaksi.KeranjangItem item = new Transaksi.KeranjangItem(b.getIdBarang(), b.getNamaBarang(), b.getHarga(), qty);
                    dataKeranjang.add(item);
                    updateTotal();
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Jumlah harus angka!");
                alert.show();
            }
        });

        Button btnSimpan = new Button("Proses Transaksi");
        btnSimpan.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // Kirim teks customer ke method simpan
        btnSimpan.setOnAction(e -> simpanKeDatabase(txtCustomer.getText(), stage));

        root.getChildren().addAll(new Label("FORM TRANSAKSI"), grid, new Label("Keranjang Belanja:"), tableKeranjang, lblTotal, btnSimpan);
        stage.setScene(new Scene(root, 600, 600));
        stage.setTitle("Transaksi Penjualan HW");
        stage.show();
    }

    private static void setupTable() {
        tableKeranjang.getColumns().clear();
        TableColumn<Transaksi.KeranjangItem, String> colNama = new TableColumn<>("Barang");
        colNama.setCellValueFactory(d -> d.getValue().namaBarangProperty());

        TableColumn<Transaksi.KeranjangItem, Integer> colQty = new TableColumn<>("Qty");
        colQty.setCellValueFactory(d -> d.getValue().jumlahProperty().asObject());

        TableColumn<Transaksi.KeranjangItem, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> d.getValue().subtotalProperty().asObject());

        tableKeranjang.getColumns().addAll(colNama, colQty, colSub);
        tableKeranjang.setItems(dataKeranjang);
    }

    private static ObservableList<Barang> loadBarang() {
        ObservableList<Barang> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM barang")) {
            while (rs.next()) {
                list.add(new Barang(rs.getInt("id_barang"), rs.getString("nama_barang"), rs.getDouble("harga"), rs.getInt("stok")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private static void updateTotal() {
        totalBayar = dataKeranjang.stream().mapToDouble(Transaksi.KeranjangItem::getSubtotal).sum();
        lblTotal.setText("Total: Rp " + totalBayar);
    }

    private static void simpanKeDatabase(String customer, Stage stage) {
        // --- Validasi Stok ---
        String pesanError = cekValidasiStok();
        if (pesanError != null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Stok Tidak Cukup");
            alert.setHeaderText(null);
            alert.setContentText(pesanError);
            alert.showAndWait();
            return;
        }

        // --- Persiapan Data untuk Database & Struk ---
        StringBuilder sbNamaBarang = new StringBuilder();
        int totalJumlahBarang = 0;
        
        // PENTING: Salin data belanjaan ke List baru sebelum dataKeranjang di-clear
        List<Transaksi.KeranjangItem> itemsBelanja = new ArrayList<>(dataKeranjang);

        for (Transaksi.KeranjangItem item : dataKeranjang) {
            if (sbNamaBarang.length() > 0) sbNamaBarang.append(", ");
            sbNamaBarang.append(item.getNamaBarang()); 
            totalJumlahBarang += item.getJumlah();
        }
        
        String gabunganNamaBarang = sbNamaBarang.toString();
        // ----------------------------------------------------------------------

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); 

            // 1. Simpan Header Transaksi
            String sqlTrans = "INSERT INTO transaksi (nama_customer, total_bayar, nama_barang, jumlah_barang) VALUES (?, ?, ?, ?)";
            PreparedStatement psTrans = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS);
            psTrans.setString(1, customer);
            psTrans.setDouble(2, totalBayar);
            psTrans.setString(3, gabunganNamaBarang); 
            psTrans.setInt(4, totalJumlahBarang);     
            
            psTrans.executeUpdate();

            ResultSet rsKeys = psTrans.getGeneratedKeys();
            if (rsKeys.next()) {
                int idTransaksi = rsKeys.getInt(1); 

                // 2. Simpan Detail & Potong Stok
                String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_barang, jumlah, subtotal) VALUES (?, ?, ?, ?)";
                String sqlUpdateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";
                
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                PreparedStatement psStok = conn.prepareStatement(sqlUpdateStok);

                for (Transaksi.KeranjangItem item : dataKeranjang) {
                    psDetail.setInt(1, idTransaksi);
                    psDetail.setInt(2, item.getIdBarang());
                    psDetail.setInt(3, item.getJumlah());
                    psDetail.setDouble(4, item.getSubtotal());
                    psDetail.addBatch();

                    psStok.setInt(1, item.getJumlah());
                    psStok.setInt(2, item.getIdBarang());
                    psStok.addBatch();
                }
                
                psDetail.executeBatch();
                psStok.executeBatch();
                
                conn.commit(); 
                
                // --- TAMPILKAN STRUK ---
                tampilkanStruk(idTransaksi, customer, itemsBelanja, totalBayar);
            }

            // Bersihkan form
            dataKeranjang.clear();
            lblTotal.setText("Total: Rp 0"); 
            txtCustomer.clear(); // SEKARANG INI TIDAK AKAN ERROR LAGI
            stage.close(); 
            
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Gagal menyimpan transaksi: " + e.getMessage());
            alert.show();
        }
    }
    
    // --- METHOD TAMPILKAN STRUK ---
    private static void tampilkanStruk(int idTransaksi, String customer, List<Transaksi.KeranjangItem> items, double total) {
        Stage strukStage = new Stage();
        strukStage.setTitle("Struk Belanja");
        strukStage.initModality(Modality.APPLICATION_MODAL); 

        // Layout Struk 
        VBox layoutStruk = new VBox(5);
        layoutStruk.setPadding(new Insets(10));
        layoutStruk.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1px;");
        layoutStruk.setAlignment(Pos.TOP_CENTER);
        
        // 1. Header
        Label lblToko = new Label("TOKO HARDWARE JAYA");
        lblToko.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblAlamat = new Label("Jl. Teknologi No. 123");
        Label lblLine = new Label("================================");
        
        // 2. Info Transaksi
        VBox infoBox = new VBox(2);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        String tanggal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        infoBox.getChildren().addAll(
            new Label("ID Trx   : " + idTransaksi),
            new Label("Tanggal : " + tanggal),
            new Label("Customer: " + customer),
            new Label("--------------------------------")
        );

        // 3. List Barang
        VBox itemBox = new VBox(2);
        for (Transaksi.KeranjangItem item : items) {
            Label lblNama = new Label(item.getNamaBarang());
            lblNama.setStyle("-fx-font-weight: bold;");
            
            String detail = String.format("%d x %.0f = Rp %.0f", 
                            item.getJumlah(), item.getHarga(), item.getSubtotal());
            Label lblDetail = new Label(detail);
            
            VBox itemRow = new VBox(lblNama, lblDetail);
            itemBox.getChildren().add(itemRow);
        }

        // 4. Total & Footer
        Label lblLine2 = new Label("--------------------------------");
        Label lblTotal = new Label(String.format("TOTAL: Rp %.0f", total));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        Label lblTerimaKasih = new Label("TERIMA KASIH");
        lblTerimaKasih.setPadding(new Insets(10,0,0,0));

        layoutStruk.getChildren().addAll(lblToko, lblAlamat, lblLine, infoBox, itemBox, lblLine2, lblTotal, lblTerimaKasih);

        // --- Container Utama dengan Tombol Print ---
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        
        Button btnPrint = new Button("Cetak Struk");
        btnPrint.setOnAction(e -> {
            printNode(layoutStruk, strukStage); 
        });

        root.getChildren().addAll(layoutStruk, btnPrint);
        
        Scene scene = new Scene(root, 350, 600);
        strukStage.setScene(scene);
        strukStage.show();
    }

    // --- METHOD PRINT NODE ---
    private static void printNode(Node node, Stage owner) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(owner)) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
            }
        }
    }

    private static String cekValidasiStok() {
        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT stok FROM barang WHERE id_barang = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            for (Transaksi.KeranjangItem item : dataKeranjang) {
                ps.setInt(1, item.getIdBarang());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int stokTersedia = rs.getInt("stok");
                    if (item.getJumlah() > stokTersedia) {
                        return "Stok '" + item.getNamaBarang() + "' kurang. Sisa: " + stokTersedia;
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null; 
    }
}