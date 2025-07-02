import javax.swing.*;
import java.awt.event.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class FormFilmDanJadwal extends JFrame {
    JTextField txtJudul, txtGenre, txtDurasi;
    DefaultListModel<String> jadwalListModel;
    JList<String> listJadwal;
    JButton btnTambahJadwal, btnSimpan;

    public FormFilmDanJadwal() {
        setTitle("Form Tambah Film & Jadwal");
        setSize(400, 450);
        setLayout(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel lblJudul = new JLabel("Judul:");
        JLabel lblGenre = new JLabel("Genre:");
        JLabel lblDurasi = new JLabel("Durasi (menit):");
        JLabel lblJadwal = new JLabel("Jadwal Tayang:");

        lblJudul.setBounds(20, 20, 100, 20);
        lblGenre.setBounds(20, 60, 100, 20);
        lblDurasi.setBounds(20, 100, 100, 20);
        lblJadwal.setBounds(20, 140, 200, 20);

        txtJudul = new JTextField(); txtJudul.setBounds(150, 20, 200, 25);
        txtGenre = new JTextField(); txtGenre.setBounds(150, 60, 200, 25);
        txtDurasi = new JTextField(); txtDurasi.setBounds(150, 100, 200, 25);

        jadwalListModel = new DefaultListModel<>();
        listJadwal = new JList<>(jadwalListModel);
        JScrollPane scrollPane = new JScrollPane(listJadwal);
        scrollPane.setBounds(20, 170, 330, 120);

        btnTambahJadwal = new JButton("Tambah Jadwal");
        btnTambahJadwal.setBounds(20, 300, 150, 30);
        btnSimpan = new JButton("Simpan Semua");
        btnSimpan.setBounds(200, 300, 150, 30);

        add(lblJudul); add(txtJudul);
        add(lblGenre); add(txtGenre);
        add(lblDurasi); add(txtDurasi);
        add(lblJadwal);
        add(scrollPane);
        add(btnTambahJadwal); add(btnSimpan);

        btnTambahJadwal.addActionListener(e -> showInputJadwal());
        btnSimpan.addActionListener(e -> simpanFilmDanJadwal());

        setVisible(true);
    }

    private void showInputJadwal() {
        JTextField txtTanggal = new JTextField();
        JTextField txtJam = new JTextField();
        JTextField txtStudio = new JTextField();

        Object[] message = {
            "Tanggal (YYYY-MM-DD):", txtTanggal,
            "Jam (HH:MM:SS):", txtJam,
            "Studio:", txtStudio
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Tambah Jadwal", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            // Validasi input
            if (!txtTanggal.getText().matches("\\d{4}-\\d{2}-\\d{2}") ||
                !txtJam.getText().matches("\\d{2}:\\d{2}:\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Format tanggal atau jam salah!\nContoh: 2025-07-01, 14:30:00");
                return;
            }

            String jadwalStr = txtTanggal.getText() + "," + txtJam.getText() + "," + txtStudio.getText();
            jadwalListModel.addElement(jadwalStr);
        }
    }

    private void simpanFilmDanJadwal() {
        String judul = txtJudul.getText();
        String genre = txtGenre.getText();
        int durasi;

        try {
            durasi = Integer.parseInt(txtDurasi.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Durasi harus angka!");
            return;
        }

        try (Connection conn = KoneksiDB.getConnection()) {
            // Simpan film
            String sqlFilm = "INSERT INTO film (judul, genre, durasi) VALUES (?, ?, ?)";
            PreparedStatement psFilm = conn.prepareStatement(sqlFilm, Statement.RETURN_GENERATED_KEYS);
            psFilm.setString(1, judul);
            psFilm.setString(2, genre);
            psFilm.setInt(3, durasi);
            psFilm.executeUpdate();

            ResultSet rs = psFilm.getGeneratedKeys();
            int id_film = -1;
            if (rs.next()) {
                id_film = rs.getInt(1);
            }

            // Simpan semua jadwal
            String sqlJadwal = "INSERT INTO jadwal_film (id_film, tanggal, jam, studio) VALUES (?, ?, ?, ?)";
            PreparedStatement psJadwal = conn.prepareStatement(sqlJadwal);

            for (int i = 0; i < jadwalListModel.size(); i++) {
                String[] parts = jadwalListModel.get(i).split(",");
                psJadwal.setInt(1, id_film);
                psJadwal.setDate(2, Date.valueOf(parts[0].trim()));
                psJadwal.setTime(3, Time.valueOf(parts[1].trim()));
                psJadwal.setString(4, parts[2].trim());
                psJadwal.addBatch();
            }

            psJadwal.executeBatch();

            JOptionPane.showMessageDialog(this, "Film & Jadwal berhasil disimpan!");
            dispose();
        } catch (SQLException e) {
            e.printStackTrace(); // debugging
            JOptionPane.showMessageDialog(this, "Gagal simpan: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new FormFilmDanJadwal();
    }
}
