package projectUAS;
import javafx.beans.property.*;

public class Barang {
    private IntegerProperty idBarang;
    private StringProperty namaBarang;
    private DoubleProperty harga;
    private IntegerProperty stok;

    public Barang(int id, String nama, double harga, int stok) {
        this.idBarang = new SimpleIntegerProperty(id);
        this.namaBarang = new SimpleStringProperty(nama);
        this.harga = new SimpleDoubleProperty(harga);
        this.stok = new SimpleIntegerProperty(stok);
    }

    public int getIdBarang() { return idBarang.get(); }
    public IntegerProperty idBarangProperty() { return idBarang; }
    public String getNamaBarang() { return namaBarang.get(); }
    public StringProperty namaBarangProperty() { return namaBarang; }
    public double getHarga() { return harga.get(); }
    public DoubleProperty hargaProperty() { return harga; }
    public int getStok() { return stok.get(); }
    public IntegerProperty stokProperty() { return stok; }

    @Override
    public String toString() {
        return getNamaBarang() + " (Rp " + getHarga() + ")";
    }
}
