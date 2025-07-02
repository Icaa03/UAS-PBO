import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class PenukaranTiketForm extends JFrame {
    JTextField txtKodeTiket;
    JButton btnTukar;

    public PenukaranTiketForm() {
        setTitle("Penukaran Tiket Bioskop");
        setSize(400, 200);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel lblKode = new JLabel("Masukkan Kode Tiket:");
        txtKodeTiket = new JTextField();
        btnTukar = new JButton("Tukar Tiket");

        lblKode.setBounds(30, 30, 150, 20);
        txtKodeTiket.setBounds(30, 60, 300, 25);
        btnTukar.setBounds(30, 100, 300, 30);

        add(lblKode);
        add(txtKodeTiket);
        add(btnTukar);

        btnTukar.addActionListener(e -> tukarTiket());

        setVisible(true);
    }

    private void tukarTiket() {
        String kode = txtKodeTiket.getText().trim();

        if (kode.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Kode tiket harus diisi!");
            return;
        }

        try (Connection conn = KoneksiDB.getConnection()) {
            String cekSql = "SELECT status_penukaran FROM pembayaran WHERE kode_tiket = ?";
            PreparedStatement cekStmt = conn.prepareStatement(cekSql);
            cekStmt.setString(1, kode);
            ResultSet rs = cekStmt.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Kode tiket tidak ditemukan.");
                return;
            }

            String status = rs.getString("status_penukaran");
            if (status.equalsIgnoreCase("Sudah")) {
                JOptionPane.showMessageDialog(this, "Tiket sudah pernah ditukar!");
                return;
            }

            // Proses penukaran
            String updateSql = "UPDATE pembayaran SET status_penukaran = 'Sudah' WHERE kode_tiket = ?";
            PreparedStatement updStmt = conn.prepareStatement(updateSql);
            updStmt.setString(1, kode);
            updStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "✅ Tiket berhasil ditukar!");
            txtKodeTiket.setText("");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PenukaranTiketForm();
    }
}
