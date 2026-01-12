package projectUAS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.Optional;

public class BarangCRUD {
    private static TableView<Barang> table = new TableView<>();
    private static ObservableList<Barang> barangList = FXCollections.observableArrayList();

    public static void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        // Setup Tabel
        table.getColumns().clear();
        TableColumn<Barang, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> d.getValue().idBarangProperty().asObject());
        
        TableColumn<Barang, String> colNama = new TableColumn<>("Nama Barang");
        colNama.setCellValueFactory(d -> d.getValue().namaBarangProperty());

        TableColumn<Barang, Double> colHarga = new TableColumn<>("Harga");
        colHarga.setCellValueFactory(d -> d.getValue().hargaProperty().asObject());

        TableColumn<Barang, Integer> colStok = new TableColumn<>("Stok");
        colStok.setCellValueFactory(d -> d.getValue().stokProperty().asObject());

        table.getColumns().addAll(colId, colNama, colHarga, colStok);
        table.setItems(barangList);

        // --- TOMBOL TAMBAH ---
        Button btnAdd = new Button("Tambah");
        btnAdd.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;");
        btnAdd.setOnAction(e -> showForm(null));

        // --- TOMBOL EDIT ---
        Button btnEdit = new Button("Edit");
        btnEdit.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnEdit.setOnAction(e -> {
            Barang selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) showForm(selected);
            else peringatanPilih();
        });

        // --- TOMBOL HAPUS (BARU) ---
        Button btnDelete = new Button("Hapus");
        btnDelete.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        btnDelete.setOnAction(e -> {
            Barang selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                hapusBarang(selected);
            } else {
                peringatanPilih();
            }
        });

        HBox buttonBox = new HBox(10, btnAdd, btnEdit, btnDelete);

        root.getChildren().addAll(new Label("Manajemen Stok Barang"), table, buttonBox);
        stage.setScene(new Scene(root, 600, 450));
        stage.setTitle("Data Barang");
        stage.show();
        loadData();
    }

    // Logika Hapus Barang
    private static void hapusBarang(Barang barang) {
        // 1. Tanya Konfirmasi dulu
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Konfirmasi Hapus");
        alert.setHeaderText("Hapus barang: " + barang.getNamaBarang() + "?");
        alert.setContentText("Data yang dihapus tidak bisa dikembalikan.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 2. Jika OK, Lakukan penghapusan di Database
            try (Connection conn = DBConnection.getConnection()) {
                String sql = "DELETE FROM barang WHERE id_barang = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, barang.getIdBarang());
                ps.executeUpdate();
                
                loadData(); // Refresh tabel
                
            } catch (SQLException ex) {
                // Error handling jika barang sudah pernah dipakai transaksi
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Gagal Hapus");
                error.setContentText("Tidak bisa menghapus barang ini karena sudah tercatat dalam riwayat transaksi penjualan.");
                error.show();
            }
        }
    }

    private static void peringatanPilih() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText("Silakan pilih barang di tabel terlebih dahulu!");
        alert.show();
    }

    private static void loadData() {
        barangList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM barang")) {
            while (rs.next()) {
                barangList.add(new Barang(rs.getInt("id_barang"), rs.getString("nama_barang"), 
                                          rs.getDouble("harga"), rs.getInt("stok")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private static void showForm(Barang barangEdit) {
        Stage formStage = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8); grid.setHgap(10);

        TextField txtNama = new TextField();
        TextField txtHarga = new TextField();
        TextField txtStok = new TextField();

        if (barangEdit != null) {
            txtNama.setText(barangEdit.getNamaBarang());
            txtHarga.setText(String.valueOf(barangEdit.getHarga()));
            txtStok.setText(String.valueOf(barangEdit.getStok()));
            formStage.setTitle("Edit Barang");
        } else {
            formStage.setTitle("Tambah Barang Baru");
        }

        grid.add(new Label("Nama:"), 0, 0); grid.add(txtNama, 1, 0);
        grid.add(new Label("Harga:"), 0, 1); grid.add(txtHarga, 1, 1);
        grid.add(new Label("Stok:"), 0, 2); grid.add(txtStok, 1, 2);

        Button btnSave = new Button("Simpan");
        btnSave.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection()) {
                String sql;
                PreparedStatement ps;

                if (barangEdit == null) {
                    sql = "INSERT INTO barang (nama_barang, harga, stok) VALUES (?,?,?)";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, txtNama.getText());
                    ps.setDouble(2, Double.parseDouble(txtHarga.getText()));
                    ps.setInt(3, Integer.parseInt(txtStok.getText()));
                } else {
                    sql = "UPDATE barang SET nama_barang=?, harga=?, stok=? WHERE id_barang=?";
                    ps = conn.prepareStatement(sql);
                    ps.setString(1, txtNama.getText());
                    ps.setDouble(2, Double.parseDouble(txtHarga.getText()));
                    ps.setInt(3, Integer.parseInt(txtStok.getText()));
                    ps.setInt(4, barangEdit.getIdBarang());
                }

                ps.executeUpdate();
                loadData();
                formStage.close();
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        VBox root = new VBox(10, grid, btnSave);
        root.setPadding(new Insets(10));
        formStage.setScene(new Scene(root, 300, 250));
        formStage.show();
    }
}