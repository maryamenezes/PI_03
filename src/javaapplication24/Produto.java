package javaapplication24;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Produto {
    private String descricao;
    private double preco;
    private String categoria;
    private String detalhes;

    public Produto(String descricao, double preco, String categoria, String detalhes) {
        this.descricao = descricao;
        this.preco = preco;
        this.categoria = categoria;
        this.detalhes = detalhes;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getPreco() {
        return preco;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public int getId() {
        int id = -1;
        String sql = "SELECT id FROM produto WHERE descricao = ? AND preco = ? AND categoria = ? LIMIT 1";
        try (Connection conn = new ConexaoPostgreSQL().conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, this.descricao);
            stmt.setDouble(2, this.preco);
            stmt.setString(3, this.categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao obter ID do produto: " + e.getMessage());
        }
        return id;
    }
}
