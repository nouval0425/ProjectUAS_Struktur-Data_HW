package projectUAS;

import java.util.List;
import java.util.Scanner;

public class MainAplikasi {
    
    // Panggil class DAO
    static BarangDAO barangDAO = new BarangDAO();
    static TransaksiDAO transaksiDAO = new TransaksiDAO();
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== SISTEM INFORMASI HIZBUL WATHAN ===");
            System.out.println("1. Lihat Daftar Barang");
            System.out.println("2. Input Barang Baru");
            System.out.println("3. Transaksi Penjualan");
            System.out.println("4. Keluar");
            System.out.print("Pilih menu: ");
            
            int pilihan = scanner.nextInt();
            scanner.nextLine(); // Bersihkan buffer

            switch (pilihan) {
                case 1:
                    tampilkanBarang();
                    break;
                case 2:
                    inputBarang();
                    break;
                case 3:
                    prosesTransaksi();
                    break;
                case 4:
                    System.out.println("Program Selesai.");
                    System.exit(0);
                default:
                    System.out.println("Pilihan salah!");
            }
        }
    }

    static void tampilkanBarang() {
        List<Barang> list = barangDAO.getAllBarang();
        System.out.println("\n--- STOK BARANG GUDANG ---");
        System.out.printf("%-5s %-20s %-15s %-5s\n", "ID", "Nama Barang", "Harga", "Stok");
        System.out.println("--------------------------------------------------");
        for (Barang b : list) {
            System.out.printf("%-5d %-20s Rp%-13.0f %-5d\n", 
                b.getIdBarang(), b.getNamaBarang(), b.getHarga(), b.getStok());
        }
    }

    static void inputBarang() {
        System.out.print("Nama Barang: ");
        String nama = scanner.nextLine();
        System.out.print("Harga: ");
        double harga = scanner.nextDouble();
        System.out.print("Stok: ");
        int stok = scanner.nextInt();
        
        barangDAO.tambahBarang(nama, harga, stok);
    }

    static void prosesTransaksi() {
        System.out.println("\n--- KASIR PENJUALAN ---");
        System.out.print("Nama Pembeli/Customer: ");
        String pembeli = scanner.nextLine();

        // 1. Buat Transaksi Baru (Dapat ID)
        int idTrans = transaksiDAO.buatTransaksiBaru(pembeli);
        
        double totalBayar = 0;
        boolean lanjut = true;

        while (lanjut) {
            tampilkanBarang(); // Tampilkan barang agar kasir mudah memilih
            System.out.print("Masukkan ID Barang: ");
            int idBrg = scanner.nextInt();
            System.out.print("Jumlah Beli: ");
            int qty = scanner.nextInt();

            // Cek Data Barang
            Barang b = barangDAO.getBarangById(idBrg);
            if (b != null) {
                if (qty <= b.getStok()) {
                    double subtotal = b.getHarga() * qty;
                    totalBayar += subtotal;

                    // Simpan ke Detail & Kurangi Stok
                    transaksiDAO.tambahDetailTransaksi(idTrans, idBrg, qty, subtotal);
                    System.out.println(">> Item masuk keranjang: " + b.getNamaBarang());
                } else {
                    System.out.println(">> Gagal: Stok tidak cukup! (Sisa: " + b.getStok() + ")");
                }
            } else {
                System.out.println(">> Barang tidak ditemukan!");
            }

            System.out.print("Tambah item lagi? (y/n): ");
            String jawab = scanner.next();
            if (!jawab.equalsIgnoreCase("y")) lanjut = false;
        }

        // 2. Simpan Total Akhir
        transaksiDAO.simpanTotalTransaksi(idTrans, totalBayar);
        System.out.println("----------------------------------");
        System.out.println("Transaksi Selesai atas nama: " + pembeli);
        System.out.println("Total Tagihan: Rp " + totalBayar);
        System.out.println("----------------------------------");
    }
}
