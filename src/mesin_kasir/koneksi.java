/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mesin_kasir;

import java.sql.DriverManager;
import java.sql.Connection;
import javax.swing.JOptionPane;


/**
 *
 * @author USER
 */
public class koneksi {
    private String url = "jdbc:mysql://localhost/kasir_db";
    private String username_xampp = "root";
    private String password_xampp = "";
    private Connection con;
    
    public void connect() {
        try {
            con = DriverManager.getConnection(url, username_xampp, password_xampp);
            System.out.println("Koneksi berhasil");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
    }

    public Connection getCon() {
        return con;
    }
    
}
