package projectUAS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;

public class BarangCRUD {
    private static TableView<Barang> table = new TableView<>();
    private static ObservableList<Barang> barangList = FXCollections.observableArrayList();

    public static void show() {
        Stage stage = new Stage();
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

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

        Button btnAdd = new Button("Tambah Barang");
        btnAdd.setOnAction(e -> showForm());

        root.getChildren().addAll(new Label("Manajemen Stok Barang"), table, btnAdd);
        stage.setScene(new Scene(root, 600, 400));
        stage.setTitle("Data Barang");
        stage.show();
        loadData();
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

    private static void showForm() {
        Stage formStage = new Stage();
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(8); grid.setHgap(10);

        TextField txtNama = new TextField();
        TextField txtHarga = new TextField();
        TextField txtStok = new TextField();

        grid.add(new Label("Nama:"), 0, 0); grid.add(txtNama, 1, 0);
        grid.add(new Label("Harga:"), 0, 1); grid.add(txtHarga, 1, 1);
        grid.add(new Label("Stok:"), 0, 2); grid.add(txtStok, 1, 2);

        Button btnSave = new Button("Simman");
        btnSave.setOnAction(e -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO barang (nama_barang, harga, stok) VALUES (?,?,?)")) {
                ps.setString(1, txtNama.getText());
                ps.setDouble(2, Double.parseDouble(txtHarga.getText()));
                ps.setInt(3, Integer.parseInt(txtStok.getText()));
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