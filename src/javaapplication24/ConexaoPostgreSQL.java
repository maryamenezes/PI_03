package javaapplication24;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexaoPostgreSQL {
    private String url;
    private String usuario;
    private String senha;

    public ConexaoPostgreSQL() {
        this.url = "jdbc:postgresql://localhost:5432/restaurante";
        this.usuario = "postgres";
        this.senha = "13042005i";
    }

    public Connection conectar() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, usuario, senha);
            System.out.println("Conectado ao banco de dados com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco de dados: " + e.getMessage());
        }
        return conn;
    }
}
