package javaapplication24;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Venda {
    private int numeroMesa;
    private int numeroComanda;
    private List<Pedido> pedidos;
    private double total;
    private Date dataHora;
    private String formaPagamento;
    private double troco;
    private List<Pagamento> pagamentos;

    public Venda(int numeroMesa, int numeroComanda, List<Pedido> pedidos, double total, Date dataHora, String formaPagamento, double troco) {
        this.numeroMesa = numeroMesa;
        this.numeroComanda = numeroComanda;
        this.pedidos = new ArrayList<>(pedidos);
        this.total = total;
        this.dataHora = dataHora;
        this.formaPagamento = formaPagamento;
        this.troco = troco;
        this.pagamentos = new ArrayList<>();
    }

    public void adicionarPagamento(Pagamento pagamento) {
        pagamentos.add(pagamento);
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public int getNumeroComanda() {
        return numeroComanda;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public double getTotal() {
        return total;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public String getFormaPagamento() {
        return formaPagamento;
    }

    public double getTroco() {
        return troco;
    }
    
    public List<Pagamento> getPagamentos() {
        return pagamentos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Mesa: ").append(numeroMesa).append(", Comanda: ").append(numeroComanda).append(", Data: ").append(dataHora)
          .append(", Forma de Pagamento: ").append(formaPagamento);
        if (formaPagamento.equals("Dinheiro")) {
            sb.append(", Troco: ").append(String.format("%.2f", troco));
        }
        sb.append("\nPedidos:");
        for (Pedido pedido : pedidos) {
            sb.append("\n").append(pedido.getProduto().getDescricao()).append(" - ").append(pedido.getQuantidade()).append(" x ").append(String.format("%.2f", pedido.getProduto().getPreco()))
              .append(" = ").append(String.format("%.2f", pedido.getProduto().getPreco() * pedido.getQuantidade()));
        }
        sb.append("\nTotal: ").append(String.format("%.2f", total));
        sb.append("\nPagamentos:");
        for (Pagamento pagamento : pagamentos) {
            sb.append("\n").append(pagamento);
        }
        return sb.toString();
    }
}
