package javaapplication24;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Mesa {
    private int numero;
    private int numeroComanda;
    private int ocupantes;
    private static int proximoNumeroComanda = 1;
    private EstadoMesa estado;
    private List<Pedido> pedidos;
    private Date abertura;
    private Date fechamento;
    private Date pagamento;
    private List<Pagamento> pagamentos = new ArrayList<>();

    public Mesa(int numero) {
        this.numero = numero;
        this.estado = EstadoMesa.LIVRE;
        this.pedidos = new ArrayList<>();
    }

    public void abrirMesa(int ocupantes) {
        if (this.estado == EstadoMesa.LIVRE) {
            this.estado = EstadoMesa.OCUPADA;
            this.numeroComanda = proximoNumeroComanda++;
            this.abertura = new Date();
            this.ocupantes = ocupantes;
            System.out.println("Mesa " + this.numero + " aberta. Número da comanda: " + this.numeroComanda + ", Ocupantes: " + this.ocupantes);
        } else {
            System.out.println("A Mesa " + this.numero + " já está ocupada ou aguardando pagamento.");
        }
    }

    public void fecharMesa() {
        if (this.estado == EstadoMesa.OCUPADA) {
            this.estado = EstadoMesa.AGUARDANDO_PAGAMENTO;
            this.fechamento = new Date();
            System.out.println("Mesa " + this.numero + " aguardando pagamento.");
        } else {
            System.out.println("A Mesa " + this.numero + " não está ocupada.");
        }
    }

    public void imprimirComanda(boolean fechada) {
        if (pedidos.isEmpty()) {
            System.out.println("Nenhum pedido realizado na Mesa " + numero + ".");
            return;
        }

        System.out.println("Comanda da Mesa " + numero + " (Número da comanda: " + numeroComanda + ", Ocupantes: " + ocupantes + "):");
        double total = 0;
        for (int i = 0; i < pedidos.size(); i++) {
            Pedido pedido = pedidos.get(i);
            double subtotal = pedido.getProduto().getPreco() * pedido.getQuantidade();
            System.out.println((i + 1) + ". " + pedido.getProduto().getDescricao() + " - " +
                               pedido.getQuantidade() + " x " +
                               String.format("%.2f", pedido.getProduto().getPreco()) + " = " +
                               String.format("%.2f", subtotal));
            total += subtotal;
        }

        double taxaServico = total * 0.10;
        System.out.println("Taxa de serviço (10%): " + String.format("%.2f", taxaServico));
        total += taxaServico;

        Calendar agora = Calendar.getInstance();
        int hora = agora.get(Calendar.HOUR_OF_DAY);
        if (hora >= 1 && hora < 6) {
            double desconto = total * 0.05;
            System.out.println("Desconto (5%): -" + String.format("%.2f", desconto));
            total -= desconto;
        }

        System.out.println("Total a pagar: " + String.format("%.2f", total));
    }

    public void realizarPagamentoInterno() {
        this.estado = EstadoMesa.LIVRE;
        this.pagamento = new Date();
        pedidos.clear();
    }

    public void adicionarPagamento(Pagamento pagamento) {
        pagamentos.add(pagamento);
    }

    public void adicionarPedido(Pedido pedido) {
        if (this.estado == EstadoMesa.OCUPADA) {
            this.pedidos.add(pedido);
            System.out.println("Pedido adicionado à Mesa " + this.numero + ".");
        } else {
            System.out.println("Não é possível adicionar pedido. Mesa " + this.numero + " não está ocupada.");
        }
    }

    public Pedido cancelarPedido(int indice) {
        if (indice >= 0 && indice < pedidos.size()) {
            Pedido pedido = pedidos.remove(indice);
            System.out.println("Pedido de " + pedido.getProduto().getDescricao() + " cancelado.");
            return pedido;
        } else {
            System.out.println("Índice de pedido inválido.");
            return null;
        }
    }

    public double calcularTotalVendas() {
        double total = 0.0;
        for (Pedido pedido : this.pedidos) {
            total += pedido.getProduto().getPreco() * pedido.getQuantidade();
        }

        double taxaServico = total * 0.10;
        total += taxaServico;

        Calendar agora = Calendar.getInstance();
        int hora = agora.get(Calendar.HOUR_OF_DAY);
        if (hora >= 1) {
            double desconto = total * 0.05;
            total -= desconto;
        }

        return total;
    }

    public List<Pedido> getPedidos() {
        return pedidos;
    }

    public int getNumero() {
        return numero;
    }

    public EstadoMesa getEstado() {
        return estado;
    }

    public int getNumeroComanda() {
        return numeroComanda;
    }

    public int getOcupantes() {
        return ocupantes;
    }

    public Date getAbertura() {
        return abertura;
    }

    public Date getFechamento() {
        return fechamento;
    }

    public Date getPagamento() {
        return pagamento;
    }

    public enum EstadoMesa {
        LIVRE, OCUPADA, AGUARDANDO_PAGAMENTO;
    }
}
