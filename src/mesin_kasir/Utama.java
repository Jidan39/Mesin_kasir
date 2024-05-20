/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package mesin_kasir;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author USER
 */
public class Utama extends javax.swing.JFrame {
    private DefaultTableModel model = null;
    private PreparedStatement stat;
    private ResultSet rs;
    koneksi k = new koneksi();

    /**
     * Creates new form 
     */
    public Utama() {
        initComponents();
        k.connect();
        refreshCombo();
        refreshTable();
    }
    
    class transaksi extends Utama {
        int id_transaksi, id_barang, harga, jumlah_beli, total_bayar;
        String nama_pembeli, nama_barang;
        
        public transaksi() {
            this.nama_pembeli = text_nama_pembeli.getText();
            this.jumlah_beli = Integer.parseInt(text_jumlah_bayar.getText());
            this.total_bayar = this.harga * this.jumlah_beli;
        }
    }
    
   public void refreshTable() {
        model = new DefaultTableModel();
        model.addColumn("ID Barang");
        model.addColumn("Nama Barang");
        model.addColumn("Harga");
        model.addColumn("Jumlah Beli");
        model.addColumn("Total Bayar");
        tabel_transaksi.setModel(model);
        try {
            this.stat = k.getCon().prepareStatement("SELECT * FROM transaksi");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                Object[] data = {
                    rs.getInt("id_barang"),
                    rs.getString("nama_barang"),
                    rs.getInt("harga"),
                    rs.getInt("jumlah_beli"),
                    rs.getInt("total_bayar")
                };
                model.addRow(data);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        text_jumlah_barang.setText("");
    }


    public void refreshCombo() {
        dropdown_barang.removeAllItems();
        try {
            k.connect(); // Pastikan koneksi terhubung
            this.stat = k.getCon().prepareStatement("SELECT id_barang, nama_barang, harga FROM barang");
            this.rs = this.stat.executeQuery();
            while (rs.next()) {
                dropdown_barang.addItem(rs.getString("id_barang") + " : " + rs.getString("nama_barang") + " : " + rs.getString("harga"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }
    
    public void tambahBarangKeTransaksi() {
        try {
            String selectedItem = dropdown_barang.getSelectedItem().toString();
            String[] parts = selectedItem.split(" : ");
            int idBarang = Integer.parseInt(parts[0]);
            String namaBarang = parts[1];
            int harga = Integer.parseInt(parts[2]);
            int jumlahBarang = Integer.parseInt(text_jumlah_barang.getText());
            int totalBayar = harga * jumlahBarang;
            String query = "INSERT INTO transaksi (id_barang, nama_barang, harga, jumlah_beli, total_bayar) VALUES (?, ?, ?, ?, ?)";
            stat = k.getCon().prepareStatement(query);
            stat.setInt(1, idBarang);
            stat.setString(2, namaBarang);
            stat.setInt(3, harga);
            stat.setInt(4, jumlahBarang);
            stat.setInt(5, totalBayar);
            stat.executeUpdate();
            refreshTable();
            JOptionPane.showMessageDialog(null, "Barang berhasil ditambahkan ke transaksi!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }
    
    public int hitungTotalHarga() {
        int totalHarga = 0;
        try {
            String query = "SELECT SUM(total_bayar) AS total_harga FROM transaksi";
            stat = k.getCon().prepareStatement(query);
            rs = stat.executeQuery();
            if (rs.next()) {
                totalHarga = rs.getInt("total_harga");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
        return totalHarga;
    }

    public void hitungKembalian() {
        try {
            int jumlahBayar = Integer.parseInt(text_bayar_pembeli.getText());
            int totalHarga = hitungTotalHarga();
            int kembalian = jumlahBayar - totalHarga;
            text_kembalian.setText(String.valueOf(kembalian));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Masukkan angka yang valid untuk pembayaran.");
        }
    }
    
    // Method to save transaction history
    public void simpanRiwayatTransaksi() {
        try {
            String namaPembeli = text_nama_pembeli.getText();
            Date date = text_tanggal.getDate();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String tanggal = dateFormat.format(date);

            int totalHarga = hitungTotalHarga();
            int uangDibayarkan = Integer.parseInt(text_bayar_pembeli.getText());
            int kembalian = Integer.parseInt(text_kembalian.getText());

            // Insert into riwayat_transaksi
            String queryRiwayat = "INSERT INTO riwayat_transaksi (nama_pembeli, tanggal, total_harga, uang_dibayarkan, kembalian) VALUES (?, ?, ?, ?, ?)";
            stat = k.getCon().prepareStatement(queryRiwayat, PreparedStatement.RETURN_GENERATED_KEYS);
            stat.setString(1, namaPembeli);
            stat.setString(2, tanggal);
            stat.setInt(3, totalHarga);
            stat.setInt(4, uangDibayarkan);
            stat.setInt(5, kembalian);
            stat.executeUpdate();

            // Retrieve the generated key for riwayat_transaksi
            ResultSet rsKey = stat.getGeneratedKeys();
            int idRiwayat = 0;
            if (rsKey.next()) {
                idRiwayat = rsKey.getInt(1);
            }

            // Insert all rows from transaksi into riwayat_transaksi
            String queryTransaksi = "INSERT INTO riwayat_transaksi (nama_barang, harga, jumlah_beli, total_bayar, id_riwayat_transaksi) SELECT nama_barang, harga, jumlah_beli, total_bayar, ? FROM transaksi";
            stat = k.getCon().prepareStatement(queryTransaksi);
            stat.setInt(1, idRiwayat);
            stat.executeUpdate();

            JOptionPane.showMessageDialog(null, "Riwayat transaksi berhasil disimpan!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
        }
    }


    
    
    


    
    


        

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        btn_tambah_barang = new javax.swing.JButton();
        text_jumlah_bayar = new javax.swing.JTextField();
        text_bayar_pembeli = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        btn_jumlah = new javax.swing.JButton();
        btn_bayar = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        text_jumlah_barang = new javax.swing.JTextField();
        btn_selesaikan_pesanan = new javax.swing.JButton();
        text_tanggal = new com.toedter.calendar.JDateChooser();
        dropdown_barang = new javax.swing.JComboBox<>();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        text_nama_pembeli = new javax.swing.JTextField();
        text_kembalian = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tabel_transaksi = new javax.swing.JTable();
        btn_tambah_barang1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        setForeground(java.awt.Color.darkGray);

        jLabel1.setFont(new java.awt.Font("Yu Gothic", 1, 24)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("MESIN KASIR NGAWI JUNIOR");
        jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("BARANG");

        btn_tambah_barang.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btn_tambah_barang.setText("TAMBAH BARANG");
        btn_tambah_barang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tambah_barangActionPerformed(evt);
            }
        });

        text_jumlah_bayar.setEditable(false);
        text_jumlah_bayar.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        text_jumlah_bayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_jumlah_bayarActionPerformed(evt);
            }
        });

        text_bayar_pembeli.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        text_bayar_pembeli.setToolTipText("");
        text_bayar_pembeli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_bayar_pembeliActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("KEMBALIAN");

        btn_jumlah.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btn_jumlah.setText("JUMLAH");
        btn_jumlah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_jumlahActionPerformed(evt);
            }
        });

        btn_bayar.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btn_bayar.setText("BAYAR");
        btn_bayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_bayarActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("JUMLAH BARANG");

        text_jumlah_barang.setFont(new java.awt.Font("Segoe UI Semibold", 0, 14)); // NOI18N
        text_jumlah_barang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_jumlah_barangActionPerformed(evt);
            }
        });

        btn_selesaikan_pesanan.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        btn_selesaikan_pesanan.setText("SELESAIKAN PESANAN");
        btn_selesaikan_pesanan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_selesaikan_pesananActionPerformed(evt);
            }
        });

        dropdown_barang.setForeground(new java.awt.Color(204, 204, 204));
        dropdown_barang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropdown_barangActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setText("TANGGAL");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("NAMA PEMBELI");

        text_kembalian.setEditable(false);
        text_kembalian.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        text_kembalian.setToolTipText("");
        text_kembalian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                text_kembalianActionPerformed(evt);
            }
        });

        tabel_transaksi.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(tabel_transaksi);

        btn_tambah_barang1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btn_tambah_barang1.setText("RIWAYAT TRANSAKSI");
        btn_tambah_barang1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tambah_barang1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btn_tambah_barang, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(text_jumlah_barang, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(dropdown_barang, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(184, 184, 184))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(text_tanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btn_selesaikan_pesanan)
                                    .addComponent(text_nama_pembeli, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(btn_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(text_jumlah_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(text_bayar_pembeli, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(text_kembalian, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(699, 699, 699)
                        .addComponent(btn_tambah_barang1)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
                    .addComponent(dropdown_barang))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(text_jumlah_barang, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_tambah_barang, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(13, 13, 13)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btn_jumlah, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(text_jumlah_bayar, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(text_tanggal, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btn_bayar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(text_bayar_pembeli)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(text_nama_pembeli, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_selesaikan_pesanan, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(32, 32, 32))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(text_kembalian))
                        .addGap(25, 25, 25)
                        .addComponent(btn_tambah_barang1, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_tambah_barangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tambah_barangActionPerformed
        // TODO add your handling code here:
        tambahBarangKeTransaksi();
    }//GEN-LAST:event_btn_tambah_barangActionPerformed

    private void text_jumlah_bayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_jumlah_bayarActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_jumlah_bayarActionPerformed

    private void text_bayar_pembeliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_bayar_pembeliActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_bayar_pembeliActionPerformed

    private void btn_jumlahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_jumlahActionPerformed
        // TODO add your handling code here:
        int totalHarga = hitungTotalHarga();
        text_jumlah_bayar.setText(String.valueOf(totalHarga));
    }//GEN-LAST:event_btn_jumlahActionPerformed

    private void btn_bayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_bayarActionPerformed
        // TODO add your handling code here:
        hitungKembalian();
    }//GEN-LAST:event_btn_bayarActionPerformed

    private void text_jumlah_barangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_jumlah_barangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_jumlah_barangActionPerformed

    private void btn_selesaikan_pesananActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_selesaikan_pesananActionPerformed
        // TODO add your handling code here:
        simpanRiwayatTransaksi();
        // Bersihkan tabel transaksi setelah disimpan ke riwayat
        model.setRowCount(0); // Menghapus semua baris dalam tabel transaksi
        text_jumlah_bayar.setText("");
        text_bayar_pembeli.setText("");
        text_kembalian.setText("");
        text_nama_pembeli.setText("");
        text_tanggal.setDate(null);
    }//GEN-LAST:event_btn_selesaikan_pesananActionPerformed

    private void dropdown_barangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropdown_barangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_dropdown_barangActionPerformed

    private void text_kembalianActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_text_kembalianActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_text_kembalianActionPerformed

    private void btn_tambah_barang1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tambah_barang1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btn_tambah_barang1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Utama.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Utama().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_bayar;
    private javax.swing.JButton btn_jumlah;
    private javax.swing.JButton btn_selesaikan_pesanan;
    private javax.swing.JButton btn_tambah_barang;
    private javax.swing.JButton btn_tambah_barang1;
    private javax.swing.JComboBox<String> dropdown_barang;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tabel_transaksi;
    private javax.swing.JTextField text_bayar_pembeli;
    private javax.swing.JTextField text_jumlah_barang;
    private javax.swing.JTextField text_jumlah_bayar;
    private javax.swing.JTextField text_kembalian;
    private javax.swing.JTextField text_nama_pembeli;
    private com.toedter.calendar.JDateChooser text_tanggal;
    // End of variables declaration//GEN-END:variables
}
