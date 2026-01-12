package projectUAS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.lang.classfile.Label;
import java.sql.*;

import javax.swing.table.TableColumn;
import javax.swing.text.TableView;

public class TransaksiCRUD {
    private static TableView<Transaksi.KeranjangItem> tableKeranjang = new TableView<>();
    private static ObservableList<Transaksi.KeranjangItem> dataKeranjang = FXCollections.observableArrayList();
    private static Label lblTotal = new Label("Total: Rp 0");
    private static double totalBayar = 0;

    public static void show() {
        Stage stage = new Stage();
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Input Area
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);

        TextField txtCustomer = new TextField();
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
            Barang b = cbBarang.getValue();
            int qty = Integer.parseInt(txtJumlah.getText());
            Transaksi.KeranjangItem item = new Transaksi.KeranjangItem(b.getIdBarang(), b.getNamaBarang(), b.getHarga(), qty);
            dataKeranjang.add(item);
            updateTotal();
        });

        Button btnSimpan = new Button("Proses Transaksi");
        btnSimpan.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
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
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Transaksi Database dimulai

            // 1. Simpan ke tabel transaksi (Header)
            String sqlTrans = "INSERT INTO transaksi (nama_customer, total_bayar) VALUES (?, ?)";
            PreparedStatement psTrans = conn.prepareStatement(sqlTrans, Statement.RETURN_GENERATED_KEYS);
            psTrans.setString(1, customer);
            psTrans.setDouble(2, totalBayar);
            psTrans.executeUpdate();

            // Ambil ID Transaksi yang baru saja dibuat
            ResultSet rsKeys = psTrans.getGeneratedKeys();
            if (rsKeys.next()) {
                int idTransaksi = rsKeys.getInt(1);

                // 2. Siapkan Query untuk Detail Transaksi DAN Update Stok
                String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_barang, jumlah, subtotal) VALUES (?, ?, ?, ?)";
                String sqlUpdateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?"; // <--- BARIS BARU
                
                PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                PreparedStatement psStok = conn.prepareStatement(sqlUpdateStok); // <--- BARIS BARU

                for (Transaksi.KeranjangItem item : dataKeranjang) {
                    // Setting untuk Detail Transaksi
                    psDetail.setInt(1, idTransaksi);
                    psDetail.setInt(2, item.getIdBarang());
                    psDetail.setInt(3, item.getJumlah());
                    psDetail.setDouble(4, item.getSubtotal());
                    psDetail.addBatch();

                    // Setting untuk Update Stok (Kurangi Stok)
                    psStok.setInt(1, item.getJumlah()); // Mengurangi stok sebanyak jumlah beli
                    psStok.setInt(2, item.getIdBarang()); // Berdasarkan ID Barang
                    psStok.addBatch(); // <--- Masukkan ke antrean eksekusi
                }
                
                // Eksekusi semua antrean
                psDetail.executeBatch();
                psStok.executeBatch(); // <--- Eksekusi pengurangan stok
            }

            conn.commit(); // Simpan permanen semua perubahan (transaksi & stok)
            dataKeranjang.clear();
            stage.close();
            System.out.println("Transaksi Berhasil dan Stok Berkurang!");
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                System.out.println("Terjadi kesalahan, membatalkan semua perubahan...");
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }
