package projectUAS;

import javafx.beans.property.*;

public class Transaksi {
    // Model untuk rincian barang yang sedang dipilih di keranjang
    public static class KeranjangItem {
        private IntegerProperty idBarang;
        private StringProperty namaBarang;
        private DoubleProperty harga;
        private IntegerProperty jumlah;
        private DoubleProperty subtotal;

        public KeranjangItem(int id, String nama, double harga, int jumlah) {
            this.idBarang = new SimpleIntegerProperty(id);
            this.namaBarang = new SimpleStringProperty(nama);
            this.harga = new SimpleDoubleProperty(harga);
            this.jumlah = new SimpleIntegerProperty(jumlah);
            this.subtotal = new SimpleDoubleProperty(harga * jumlah);
        }

        public int getIdBarang() { return idBarang.get(); }
        public String getNamaBarang() { return namaBarang.get(); }
        public double getHarga() { return harga.get(); }
        public int getJumlah() { return jumlah.get(); }
        public double getSubtotal() { return subtotal.get(); }
        public DoubleProperty subtotalProperty() { return subtotal; }
        public StringProperty namaBarangProperty() { return namaBarang; }
        public IntegerProperty jumlahProperty() { return jumlah; }
    }
}
