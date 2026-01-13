package projectUAS;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TransaksiLaporan {

    public static void show() {
        Stage stage = new Stage();
        stage.setTitle("Laporan Penjualan HW");

        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        Label title = new Label("Riwayat Transaksi Penjualan");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TableView<LaporanItem> table = createReportTable();
        loadReportData(table.getItems());

        root.getChildren().addAll(title, table);

        Scene scene = new Scene(root, 650, 500);
        stage.setScene(scene);
        stage.show();
    }

    private static TableView<LaporanItem> createReportTable() {
        TableView<LaporanItem> table = new TableView<>();
        
        TableColumn<LaporanItem, Integer> colId = new TableColumn<>("ID Transaksi");
        colId.setCellValueFactory(data -> data.getValue().idProperty().asObject());

        TableColumn<LaporanItem, String> colCustomer = new TableColumn<>("Customer");
        colCustomer.setCellValueFactory(data -> data.getValue().customerProperty());

        TableColumn<LaporanItem, String> colTanggal = new TableColumn<>("Tanggal");
        colTanggal.setCellValueFactory(data -> data.getValue().tanggalProperty());

        TableColumn<LaporanItem, Double> colTotal = new TableColumn<>("Total Bayar");
        colTotal.setCellValueFactory(data -> data.getValue().totalProperty().asObject());
        
        table.getColumns().addAll(colId, colCustomer, colTanggal, colTotal);
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
                    rs.getDouble("total_bayar")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Model internal khusus untuk tampilan laporan
    public static class LaporanItem {
        private final javafx.beans.property.IntegerProperty id;
        private final javafx.beans.property.StringProperty customer;
        private final javafx.beans.property.StringProperty tanggal;
        private final javafx.beans.property.DoubleProperty total;

        public LaporanItem(int id, String customer, String tanggal, double total) {
            this.id = new javafx.beans.property.SimpleIntegerProperty(id);
            this.customer = new javafx.beans.property.SimpleStringProperty(customer);
            this.tanggal = new javafx.beans.property.SimpleStringProperty(tanggal);
            this.total = new javafx.beans.property.SimpleDoubleProperty(total);
        }

        public javafx.beans.property.IntegerProperty idProperty() { return id; }
        public javafx.beans.property.StringProperty customerProperty() { return customer; }
        public javafx.beans.property.StringProperty tanggalProperty() { return tanggal; }
        public javafx.beans.property.DoubleProperty totalProperty() { return total; }
    }
}
