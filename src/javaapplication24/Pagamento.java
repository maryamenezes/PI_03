package javaapplication24;

import java.util.Date;

public class Pagamento {
    private String formaPagamento;
    private double valorPago;
    private double troco;
    private Date dataHora;

    public Pagamento(String formaPagamento, double valorPago, double troco, Date dataHora) {
        this.formaPagamento = formaPagamento;
        this.valorPago = valorPago;
        this.troco = troco;
        this.dataHora = dataHora;
    }

    @Override
    public String toString() {
        return "Forma de Pagamento: " + formaPagamento + ", Valor Pago: " + String.format("%.2f", valorPago) + ", Troco: " + String.format("%.2f", troco) + ", Data: " + dataHora;
    }
}
