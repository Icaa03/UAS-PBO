import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class PembayaranForm extends JFrame {
    JTextField txtIdPesanan, txtNama, txtTotal, txtBayar, txtKembali;
    JButton btnCek, btnBayar;

    public PembayaranForm() {
        setTitle("Form Pembayaran Tiket");
        setSize(420, 320);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel lblIdPesanan = new JLabel("ID Pesanan");
        JLabel lblNama = new JLabel("Nama Pemesan");
        JLabel lblTotal = new JLabel("Total Harga");
        JLabel lblBayar = new JLabel("Uang Dibayar");
        JLabel lblKembali = new JLabel("Kembalian");

        txtIdPesanan = new JTextField();
        txtNama = new JTextField(); txtNama.setEditable(false);
        txtTotal = new JTextField(); txtTotal.setEditable(false);
        txtBayar = new JTextField();
        txtKembali = new JTextField(); txtKembali.setEditable(false);

        btnCek = new JButton("Cek Pesanan");
        btnBayar = new JButton("Bayar");

        lblIdPesanan.setBounds(30, 20, 120, 20);
        txtIdPesanan.setBounds(150, 20, 200, 20);
        btnCek.setBounds(150, 50, 200, 25);

        lblNama.setBounds(30, 90, 120, 20);
        txtNama.setBounds(150, 90, 200, 20);

        lblTotal.setBounds(30, 120, 120, 20);
        txtTotal.setBounds(150, 120, 200, 20);

        lblBayar.setBounds(30, 150, 120, 20);
        txtBayar.setBounds(150, 150, 200, 20);

        lblKembali.setBounds(30, 180, 120, 20);
        txtKembali.setBounds(150, 180, 200, 20);

        btnBayar.setBounds(150, 220, 200, 30);

        add(lblIdPesanan); add(txtIdPesanan); add(btnCek);
        add(lblNama); add(txtNama);
        add(lblTotal); add(txtTotal);
        add(lblBayar); add(txtBayar);
        add(lblKembali); add(txtKembali);
        add(btnBayar);

        btnCek.addActionListener(e -> cekPesanan());
        btnBayar.addActionListener(e -> prosesBayar());

        txtBayar.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                hitungKembalian();
            }
        });

        setVisible(true);
    }

    private void cekPesanan() {
        try (Connection conn = KoneksiDB.getConnection()) {
            int id = Integer.parseInt(txtIdPesanan.getText());
            String sql = "SELECT nama_pemesan, total_harga FROM pesanan WHERE id_pesanan = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                txtNama.setText(rs.getString("nama_pemesan"));
                txtTotal.setText(String.valueOf(rs.getInt("total_harga")));
            } else {
                JOptionPane.showMessageDialog(this, "Pesanan tidak ditemukan.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void hitungKembalian() {
        try {
            int total = Integer.parseInt(txtTotal.getText());
            int bayar = Integer.parseInt(txtBayar.getText());
            int kembali = bayar - total;
            txtKembali.setText(String.valueOf(kembali));
        } catch (Exception e) {
            txtKembali.setText("");
        }
    }

    private String generateKodeTiket(int idPesanan) {
        String random = Integer.toHexString(new java.util.Random().nextInt(0xFFFF));
        return "TIKET-" + idPesanan + "-" + random.toUpperCase();
    }

    private void prosesBayar() {
        try (Connection conn = KoneksiDB.getConnection()) {
            int id = Integer.parseInt(txtIdPesanan.getText());
            int total = Integer.parseInt(txtTotal.getText());
            int bayar = Integer.parseInt(txtBayar.getText());
            int kembali = bayar - total;

            if (bayar < total) {
                JOptionPane.showMessageDialog(this, "Uang dibayar kurang dari total harga!");
                return;
            }

            String kodeTiket = generateKodeTiket(id);

            String sql = "INSERT INTO pembayaran (id_pesanan, total_harga, uang_dibayar, kembalian, kode_tiket) " +
                         "VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            pst.setInt(2, total);
            pst.setInt(3, bayar);
            pst.setInt(4, kembali);
            pst.setString(5, kodeTiket);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "✅ Pembayaran berhasil!\nKode Tiket Anda:\n" + kodeTiket,
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);

            // Kosongkan input
            txtIdPesanan.setText("");
            txtNama.setText("");
            txtTotal.setText("");
            txtBayar.setText("");
            txtKembali.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal bayar: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PembayaranForm();
    }
}
