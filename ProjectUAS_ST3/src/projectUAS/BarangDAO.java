package projectUAS;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BarangDAO {

    // Menampilkan semua barang
    public List<Barang> getAllBarang() {
        List<Barang> listBarang = new ArrayList<>();
        String sql = "SELECT * FROM barang";
        
        try (Connection conn = DatabaseConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Barang b = new Barang(
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                );
                listBarang.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return listBarang;
    }

    // Menambah barang baru
    public void tambahBarang(String nama, double harga, int stok) {
        String sql = "INSERT INTO barang (nama_barang, harga, stok) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nama);
            ps.setDouble(2, harga);
            ps.setInt(3, stok);
            ps.executeUpdate();
            System.out.println(">> Barang berhasil disimpan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Mengambil satu barang berdasarkan ID (untuk cek stok & harga saat transaksi)
    public Barang getBarangById(int id) {
        String sql = "SELECT * FROM barang WHERE id_barang = ?";
        try (Connection conn = DatabaseConnect.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
             
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Barang(
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getDouble("harga"),
                    rs.getInt("stok")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
