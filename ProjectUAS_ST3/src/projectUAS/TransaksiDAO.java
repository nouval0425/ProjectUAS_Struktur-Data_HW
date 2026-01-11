package projectUAS;

import java.sql.*;

public class TransaksiDAO {

    // 1. Membuat Header Transaksi (Data Customer) -> Mengembalikan ID Transaksi
    public int buatTransaksiBaru(String namaCustomer) {
        int idTransaksi = -1;
        String sql = "INSERT INTO transaksi (nama_customer, total_bayar) VALUES (?, 0)";
        
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, namaCustomer);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idTransaksi = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return idTransaksi;
    }

    // 2. Memasukkan Item ke Detail Transaksi & Mengurangi Stok
    public void tambahDetailTransaksi(int idTransaksi, int idBarang, int qty, double subtotal) {
        String sqlDetail = "INSERT INTO detail_transaksi (id_transaksi, id_barang, jumlah, subtotal) VALUES (?, ?, ?, ?)";
        String sqlUpdateStok = "UPDATE barang SET stok = stok - ? WHERE id_barang = ?";

        try (Connection conn = DatabaseConnect.getConnection()) {
            // Insert Detail
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            psDetail.setInt(1, idTransaksi);
            psDetail.setInt(2, idBarang);
            psDetail.setInt(3, qty);
            psDetail.setDouble(4, subtotal);
            psDetail.executeUpdate();

            // Update Stok Barang
            PreparedStatement psStok = conn.prepareStatement(sqlUpdateStok);
            psStok.setInt(1, qty);
            psStok.setInt(2, idBarang);
            psStok.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3. Update Total Akhir Pembayaran
    public void simpanTotalTransaksi(int idTransaksi, double totalBelanja) {
        String sql = "UPDATE transaksi SET total_bayar = ? WHERE id_transaksi = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setDouble(1, totalBelanja);
            ps.setInt(2, idTransaksi);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
