package projectUAS;

public class Barang {
    private int idBarang;
    private String namaBarang;
    private double harga;
    private int stok;

    // Constructor
    public Barang(int idBarang, String namaBarang, double harga, int stok) {
        this.idBarang = idBarang;
        this.namaBarang = namaBarang;
        this.harga = harga;
        this.stok = stok;
    }

    // Getters
    public int getIdBarang() { return idBarang; }
    public String getNamaBarang() { return namaBarang; }
    public double getHarga() { return harga; }
    public int getStok() { return stok; }
}
