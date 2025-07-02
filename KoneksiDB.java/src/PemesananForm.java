import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;

public class PemesananForm extends JFrame {
    JComboBox<String> cbJadwal;
    JTextField txtNama, txtJumlah, txtTotal;
    JButton btnPesan;

    // Mapping dari teks jadwal ke ID jadwal
    Map<String, Integer> jadwalMap = new HashMap<>();

    public PemesananForm() {
        setTitle("Pemesanan Tiket Bioskop");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        txtNama = new JTextField();
        txtJumlah = new JTextField();
        txtTotal = new JTextField();
        txtTotal.setEditable(false);
        cbJadwal = new JComboBox<>();
        btnPesan = new JButton("Pesan");

        add(new JLabel("Nama Pemesan")).setBounds(20, 20, 120, 20);
        add(txtNama).setBounds(150, 20, 200, 20);

        add(new JLabel("Pilih Jadwal")).setBounds(20, 50, 120, 20);
        add(cbJadwal).setBounds(150, 50, 200, 20);

        add(new JLabel("Jumlah Tiket")).setBounds(20, 80, 120, 20);
        add(txtJumlah).setBounds(150, 80, 200, 20);

        add(new JLabel("Total Harga (Rp)")).setBounds(20, 110, 120, 20);
        add(txtTotal).setBounds(150, 110, 200, 20);

        add(btnPesan).setBounds(130, 160, 120, 30);

        loadJadwal();

        txtJumlah.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    int jumlah = Integer.parseInt(txtJumlah.getText());
                    int hargaPerTiket = 35000;
                    txtTotal.setText(String.valueOf(jumlah * hargaPerTiket));
                } catch (NumberFormatException ex) {
                    txtTotal.setText("");
                }
            }
        });

        btnPesan.addActionListener(e -> simpanPesanan());

        setVisible(true);
    }

    private void loadJadwal() {
        try (Connection con = KoneksiDB.getConnection()) {
            String sql = """
                SELECT j.id_jadwal, f.judul, j.tanggal, j.jam
                FROM jadwal_film j
                JOIN film f ON j.id_film = f.id_film
            """;
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            cbJadwal.removeAllItems();
            while (rs.next()) {
                int id = rs.getInt("id_jadwal");
                String label = rs.getString("judul") + " - " + rs.getDate("tanggal") + " " + rs.getTime("jam");
                jadwalMap.put(label, id);
                cbJadwal.addItem(label);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal load jadwal: " + e.getMessage());
        }
    }

    private void simpanPesanan() {
        String nama = txtNama.getText();
        String jadwalDipilih = (String) cbJadwal.getSelectedItem();
        if (jadwalDipilih == null) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal terlebih dahulu!");
            return;
        }

        int idJadwal = jadwalMap.get(jadwalDipilih);
        int jumlahTiket = Integer.parseInt(txtJumlah.getText());
        int totalHarga = Integer.parseInt(txtTotal.getText());

        try (Connection con = KoneksiDB.getConnection()) {
            String sql = "INSERT INTO pesanan (nama_pemesan, id_jadwal, jumlah_tiket, total_harga) VALUES (?, ?, ?, ?)";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, nama);
            pst.setInt(2, idJadwal);
            pst.setInt(3, jumlahTiket);
            pst.setInt(4, totalHarga);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Pesanan berhasil disimpan!");
            txtNama.setText("");
            txtJumlah.setText("");
            txtTotal.setText("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Gagal simpan: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new PemesananForm();
    }
}
