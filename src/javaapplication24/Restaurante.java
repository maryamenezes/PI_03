package javaapplication24;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Date;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Restaurante {
    private Map<Integer, Mesa> mesas;
    private Map<String, List<Produto>> cardapioPorCategoria;
    private List<Venda> vendas;
    private ConexaoPostgreSQL conexao;

    public Restaurante(int numeroDeMesas, String url, String usuario, String senha) {
        mesas = new HashMap<>();
        cardapioPorCategoria = new HashMap<>();
        vendas = new ArrayList<>();
        conexao = new ConexaoPostgreSQL();
        inicializarMesas(numeroDeMesas);
        inicializarCardapio();
        persistirCardapio();
    }

    private void inicializarMesas(int numeroDeMesas) {
        for (int i = 1; i <= numeroDeMesas; i++) {
            mesas.put(i, new Mesa(i));
        }
    }

    private void inicializarCardapio() {
        cardapioPorCategoria.put("Entradas", new ArrayList<>(List.of(
            new Produto("Batata Frita", 7.50, "Comida", "Deliciosas batatas fritas crocantes."),
            new Produto("Anéis de Cebola", 6.00, "Comida", "Anéis de cebola empanados e crocantes."),
            new Produto("Mozzarella Sticks", 8.00, "Comida", "Palitos de mozzarella empanados e fritos."),
            new Produto("Buffalo Wings", 12.00, "Comida", "Asas de frango ao molho buffalo."),
            new Produto("Nachos", 10.00, "Comida", "Nachos crocantes com queijo derretido.")
        )));
        cardapioPorCategoria.put("Hambúrgueres", new ArrayList<>(List.of(
            new Produto("Hambúrguer Clássico", 15.00, "Comida", "Hambúrguer clássico com alface, tomate e queijo."),
            new Produto("Hambúrguer Vegetariano", 17.00, "Comida", "Hambúrguer vegetariano feito com grão-de-bico."),
            new Produto("Hambúrguer de Bacon", 18.00, "Comida", "Hambúrguer com bacon crocante e queijo cheddar."),
            new Produto("Cheeseburger", 16.00, "Comida", "Hambúrguer com queijo derretido."),
            new Produto("Hambúrguer Duplo", 20.00, "Comida", "Hambúrguer duplo com queijo e molho especial.")
        )));
        cardapioPorCategoria.put("Bebidas", new ArrayList<>(List.of(
            new Produto("Refrigerante Lata", 5.00, "Bebida", "Refrigerante gelado de diversos sabores."),
            new Produto("Água Mineral", 3.00, "Bebida", "Água mineral sem gás."),
            new Produto("Suco Natural", 7.00, "Bebida", "Suco natural de frutas frescas."),
            new Produto("Chá Gelado", 6.00, "Bebida", "Chá gelado com limão."),
            new Produto("Cerveja", 8.00, "Bebida", "Cerveja gelada.")
        )));
        cardapioPorCategoria.put("Sobremesas", new ArrayList<>(List.of(
            new Produto("Sorvete", 10.00, "Sobremesa", "Sorvete de creme com cobertura de chocolate."),
            new Produto("Torta de Maçã", 12.00, "Sobremesa", "Torta de maçã servida com uma bola de sorvete."),
            new Produto("Brownie", 8.00, "Sobremesa", "Brownie de chocolate com nozes."),
            new Produto("Cheesecake", 15.00, "Sobremesa", "Cheesecake com calda de frutas vermelhas."),
            new Produto("Mousse de Maracujá", 7.00, "Sobremesa", "Mousse de maracujá decorada com hortelã.")
        )));
    }

    private void persistirCardapio() {
        try (Connection conn = conexao.conectar()) {
            for (List<Produto> produtos : cardapioPorCategoria.values()) {
                for (Produto produto : produtos) {
                    String sql = "INSERT INTO produto (descricao, preco, categoria, detalhes) VALUES (?, ?, ?, ?) ON CONFLICT DO NOTHING";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, produto.getDescricao());
                        stmt.setDouble(2, produto.getPreco());
                        stmt.setString(3, produto.getCategoria());
                        stmt.setString(4, produto.getDetalhes());
                        stmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao persistir o cardápio no banco de dados: " + e.getMessage());
        }
    }

    public void exibirCategorias() {
        System.out.println("Escolha uma categoria:");
        int i = 1;
        for (String categoria : cardapioPorCategoria.keySet()) {
            System.out.println(i + ". " + categoria);
            i++;
        }
        System.out.println("0. Voltar");
    }

    public void exibirProdutosDaCategoria(String categoria) {
        List<Produto> produtos = cardapioPorCategoria.get(categoria);
        if (produtos != null) {
            System.out.println("Categoria: " + categoria);
            for (int i = 0; i < produtos.size(); i++) {
                Produto produto = produtos.get(i);
                System.out.println((i + 1) + ". " + produto.getDescricao() + " - $" + produto.getPreco());
                System.out.println("    " + produto.getDetalhes());
            }
            System.out.println("0. Voltar");
        } else {
            System.out.println("Categoria não encontrada.");
        }
    }

    public void adicionarPedido(int numeroMesa, String categoria, int indiceProduto, int quantidade) {
        if (!mesas.containsKey(numeroMesa) || mesas.get(numeroMesa).getEstado() != Mesa.EstadoMesa.OCUPADA) {
            System.out.println("Mesa não encontrada ou não está ocupada.");
            return;
        }
        List<Produto> produtos = cardapioPorCategoria.get(categoria);
        if (produtos == null || indiceProduto < 0 || indiceProduto >= produtos.size()) {
            System.out.println("Produto não encontrado na categoria " + categoria + ".");
            return;
        }
        Produto produto = produtos.get(indiceProduto);
        Pedido novoPedido = new Pedido(produto, quantidade);
        mesas.get(numeroMesa).adicionarPedido(novoPedido);

        try (Connection conn = new ConexaoPostgreSQL().conectar()) {
            String sql = "INSERT INTO pedido (id_mesa, id_produto, quantidade) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, numeroMesa);
                stmt.setInt(2, produto.getId()); // Obter o ID do produto
                stmt.setInt(3, quantidade);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao adicionar o pedido no banco de dados: " + e.getMessage());
        }
    }

    public void cancelarPedido(int numeroMesa) {
        if (mesas.containsKey(numeroMesa)) {
            Mesa mesa = mesas.get(numeroMesa);
            if (mesa.getEstado() == Mesa.EstadoMesa.OCUPADA) {
                mesa.imprimirComanda(false); 
                Scanner scanner = new Scanner(System.in);
                int indicePedido = lerNumeroValido(scanner, "Informe o índice do pedido a cancelar:", 1, mesa.getPedidos().size()) - 1;
                Pedido pedidoCancelado = mesa.cancelarPedido(indicePedido);

                try (Connection conn = conexao.conectar()) {
                    String sql = "DELETE FROM pedido WHERE id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setInt(1, pedidoCancelado.getId());
                        stmt.executeUpdate();
                    }
                } catch (SQLException e) {
                    System.out.println("Erro ao cancelar o pedido no banco de dados: " + e.getMessage());
                }
            } else {
                System.out.println("Mesa " + numeroMesa + " não está ocupada.");
            }
        } else {
            System.out.println("Mesa não encontrada.");
        }
    }

    public void mostrarMapaDeMesas() {
        mesas.forEach((numero, mesa) -> {
            System.out.println("Mesa " + numero + ": " + mesa.getEstado());
        });
    }

    public void abrirMesa(int numeroMesa, int ocupantes) {
        if (mesas.containsKey(numeroMesa)) {
            mesas.get(numeroMesa).abrirMesa(ocupantes);
            System.out.println("Número da comanda da mesa " + numeroMesa + ": " + mesas.get(numeroMesa).getNumeroComanda());

            try (Connection conn = conexao.conectar()) {
                String sql = "INSERT INTO mesa (numero, estado, abertura, ocupantes) VALUES (?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, mesas.get(numeroMesa).getNumero());
                    stmt.setString(2, mesas.get(numeroMesa).getEstado().name());
                    stmt.setTimestamp(3, new java.sql.Timestamp(mesas.get(numeroMesa).getAbertura().getTime()));
                    stmt.setInt(4, mesas.get(numeroMesa).getOcupantes());
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao abrir a mesa no banco de dados: " + e.getMessage());
            }
        } else {
            System.out.println("Mesa não encontrada.");
        }
    }

    public void fecharMesa(int numeroMesa) {
        if (mesas.containsKey(numeroMesa)) {
            mesas.get(numeroMesa).fecharMesa();

            try (Connection conn = conexao.conectar()) {
                String sql = "UPDATE mesa SET estado = ?, fechamento = ? WHERE numero = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, mesas.get(numeroMesa).getEstado().name());
                    stmt.setTimestamp(2, new java.sql.Timestamp(mesas.get(numeroMesa).getFechamento().getTime()));
                    stmt.setInt(3, numeroMesa);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao fechar a mesa no banco de dados: " + e.getMessage());
            }
        } else {
            System.out.println("Mesa não encontrada.");
        }
    }

    public void realizarPagamento(int numeroMesa, int formaPagamentoIndex, Scanner scanner) {
    if (mesas.containsKey(numeroMesa)) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa.getEstado() == Mesa.EstadoMesa.AGUARDANDO_PAGAMENTO) {
            mesa.imprimirComanda(false);  // Imprime a comanda antes de pedir a forma de pagamento

            String formaPagamento;
            double troco = 0;
            switch (formaPagamentoIndex) {
                case 1:
                    formaPagamento = "Cartão de Crédito";
                    System.out.println("Deseja parcelar? (s/n)");
                    String resposta = scanner.next();
                    if (resposta.equalsIgnoreCase("s")) {
                        System.out.println("Informe o número de parcelas (até 12):");
                        int parcelas = scanner.nextInt();
                        if (parcelas < 1 || parcelas > 12) {
                            System.out.println("Número de parcelas inválido. Usando pagamento à vista.");
                            parcelas = 1;
                        }
                        double totalComParcelas = calcularTotalComParcelas(mesa.calcularTotalVendas(), parcelas);
                        System.out.println("Total com parcelas (" + parcelas + "x): " + String.format("%.2f", totalComParcelas));
                    }
                    break;
                case 2:
                    formaPagamento = "Cartão de Débito";
                    break;
                case 3:
                    formaPagamento = "Dinheiro";
                    double total = mesa.calcularTotalVendas();
                    double valorRecebido = 0;
                    while (valorRecebido < total) {
                        System.out.println("Informe o valor recebido:");
                        valorRecebido = scanner.nextDouble();
                        if (valorRecebido < total) {
                            System.out.println("Valor recebido é insuficiente para cobrir o total da comanda. Tente novamente.");
                        }
                    }
                    troco = valorRecebido - total;
                    System.out.println("Troco: " + String.format("%.2f", troco));
                    break;
                case 4:
                    formaPagamento = "PIX";
                    break;
                default:
                    System.out.println("Forma de pagamento inválida.");
                    return;
            }

            double totalFinal = mesa.calcularTotalVendas();
            List<Pedido> pedidos = new ArrayList<>(mesa.getPedidos());
            int numeroComanda = mesa.getNumeroComanda();
            mesa.realizarPagamentoInterno();  // Remove a impressão duplicada
            Venda venda = new Venda(numeroMesa, numeroComanda, pedidos, totalFinal, new Date(), formaPagamento, troco);
            venda.adicionarPagamento(new Pagamento(formaPagamento, totalFinal, troco, new Date()));
            vendas.add(venda);

            // Persistir a venda no banco de dados
            try (Connection conn = new ConexaoPostgreSQL().conectar()) {
                String sql = "INSERT INTO venda (id_mesa, total, data_hora, forma_pagamento, troco) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, numeroMesa);
                    stmt.setDouble(2, totalFinal);
                    stmt.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
                    stmt.setString(4, formaPagamento);
                    stmt.setDouble(5, troco);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                System.out.println("Erro ao realizar pagamento no banco de dados: " + e.getMessage());
            }

            System.out.println("Pagamento realizado para a mesa " + numeroMesa + ". Número da comanda: " + numeroComanda);
        } else {
            System.out.println("Mesa " + numeroMesa + " não está aguardando pagamento.");
        }
    } else {
        System.out.println("Mesa não encontrada.");
    }
}


    public void realizarPagamentoOcupante(int numeroMesa, int ocupanteIndex, List<Pedido> pedidos, double total, int formaPagamentoIndex, Scanner scanner) {
        String formaPagamento;
        double troco = 0;
        switch (formaPagamentoIndex) {
            case 1:
                formaPagamento = "Cartão de Crédito";
                System.out.println("Deseja parcelar? (s/n)");
                String resposta = scanner.next();
                if (resposta.equalsIgnoreCase("s")) {
                    System.out.println("Informe o número de parcelas (até 12):");
                    int parcelas = scanner.nextInt();
                    if (parcelas < 1 || parcelas > 12) {
                        System.out.println("Número de parcelas inválido. Usando pagamento à vista.");
                        parcelas = 1;
                    }
                    double totalComParcelas = calcularTotalComParcelas(total, parcelas);
                    System.out.println("Total com parcelas (" + parcelas + "x): " + String.format("%.2f", totalComParcelas));
                }
                break;
            case 2:
                formaPagamento = "Cartão de Débito";
                break;
            case 3:
                formaPagamento = "Dinheiro";
                System.out.println("Informe o valor recebido:");
                double valorRecebido = scanner.nextDouble();
                troco = valorRecebido - total;
                if (troco < 0) {
                    System.out.println("Valor recebido é insuficiente para cobrir o total da comanda.");
                    return;
                }
                System.out.println("Troco: " + String.format("%.2f", troco));
                break;
            case 4:
                formaPagamento = "PIX";
                break;
            default:
                System.out.println("Forma de pagamento inválida.");
                return;
        }

        Mesa mesa = mesas.get(numeroMesa);
        int numeroComanda = mesa.getNumeroComanda();
        Pagamento pagamento = new Pagamento(formaPagamento, total, troco, new Date());
        mesa.adicionarPagamento(pagamento);
        Venda venda = new Venda(numeroMesa, numeroComanda, pedidos, total, new Date(), formaPagamento, troco);
        venda.adicionarPagamento(pagamento);
        vendas.add(venda);

        try (Connection conn = conexao.conectar()) {
            String sql = "INSERT INTO venda (id_mesa, total, data_hora, forma_pagamento, troco) VALUES ((SELECT id FROM mesa WHERE numero = ?), ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, numeroMesa);
                stmt.setDouble(2, total);
                stmt.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
                stmt.setString(4, formaPagamento);
                stmt.setDouble(5, troco);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("Erro ao realizar pagamento no banco de dados: " + e.getMessage());
        }

        System.out.println("Pagamento realizado para o ocupante. Número da comanda: " + numeroComanda);
    }

    private double calcularTotalComParcelas(double total, int parcelas) {
        double taxaParcelamento = 0.05;
        return total * Math.pow(1 + taxaParcelamento, parcelas);
    }

    public void exibirComanda(int numeroMesa, boolean fechada) {
        if (mesas.containsKey(numeroMesa)) {
            mesas.get(numeroMesa).imprimirComanda(fechada);
        } else {
            System.out.println("Mesa não encontrada.");
        }
    }

    public void exibirComandaAntiga(int numeroComanda) {
        List<Venda> vendasComanda = vendas.stream()
                                          .filter(v -> v.getNumeroComanda() == numeroComanda)
                                          .collect(Collectors.toList());
        if (!vendasComanda.isEmpty()) {
            Venda venda = vendasComanda.get(0);
            System.out.println("Mesa: " + venda.getNumeroMesa() + ", Comanda: " + venda.getNumeroComanda() + ", Data: " + venda.getDataHora());
            System.out.println("Pedidos:");
            
            // Consolidar todos os pedidos
            Map<String, Integer> pedidosConsolidados = new HashMap<>();
            double total = 0;
            
            for (Venda v : vendasComanda) {
                for (Pedido pedido : v.getPedidos()) {
                    String descricao = pedido.getProduto().getDescricao();
                    int quantidade = pedido.getQuantidade();
                    pedidosConsolidados.put(descricao, pedidosConsolidados.getOrDefault(descricao, 0) + quantidade);
                    total += pedido.getProduto().getPreco() * quantidade;
                }
            }

            int index = 1;
            for (Map.Entry<String, Integer> entry : pedidosConsolidados.entrySet()) {
                String descricao = entry.getKey();
                int quantidade = entry.getValue();
                double preco = vendasComanda.stream()
                                            .flatMap(v -> v.getPedidos().stream())
                                            .filter(p -> p.getProduto().getDescricao().equals(descricao))
                                            .map(p -> p.getProduto().getPreco())
                                            .findFirst()
                                            .orElse(0.0);  // Ajuste para evitar exceção
                System.out.println(index++ + ". " + descricao + " - " + quantidade + " x " + String.format("%.2f", preco) + " = " + String.format("%.2f", preco * quantidade));
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

            System.out.println("Total: " + String.format("%.2f", total));

            System.out.println("Pagamentos:");
            for (Venda v : vendasComanda) {
                for (Pagamento pagamento : v.getPagamentos()) {
                    System.out.println(pagamento);
                }
            }
        } else {
            System.out.println("Comanda não encontrada.");
        }
    }

    public void gerarRelatorioFinanceiro(Date dataInicio, Date dataFim, int numeroMesa) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        List<Venda> vendasFiltradas = vendas.stream()
            .filter(v -> (dataInicio == null || !v.getDataHora().before(dataInicio)) &&
                         (dataFim == null || !v.getDataHora().after(dataFim)) &&
                         (numeroMesa == -1 || v.getNumeroMesa() == numeroMesa))
            .collect(Collectors.toList());

        double totalGeral = 0.0;
        Map<String, Integer> produtosVendidos = new HashMap<>();
        Map<String, Double> vendasPorDia = new HashMap<>();
        long duracaoTotalAberturaFechamento = 0;
        long duracaoTotalFechamentoPagamento = 0;

        System.out.println("Relatório Financeiro:");
        for (Venda venda : vendasFiltradas) {
            System.out.println(venda);
            totalGeral += venda.getTotal();

            for (Pedido pedido : venda.getPedidos()) {
                produtosVendidos.put(pedido.getProduto().getDescricao(), produtosVendidos.getOrDefault(pedido.getProduto().getDescricao(), 0) + pedido.getQuantidade());
            }

            String dataVenda = sdf.format(venda.getDataHora()).split(" ")[0];
            vendasPorDia.put(dataVenda, vendasPorDia.getOrDefault(dataVenda, 0.0) + venda.getTotal());

            Mesa mesa = getMesa(venda.getNumeroMesa());
            if (mesa != null) {
                if (mesa.getAbertura() != null && mesa.getFechamento() != null) {
                    duracaoTotalAberturaFechamento += mesa.getFechamento().getTime() - mesa.getAbertura().getTime();
                }
                if (mesa.getFechamento() != null && mesa.getPagamento() != null) {
                    duracaoTotalFechamentoPagamento += mesa.getPagamento().getTime() - mesa.getFechamento().getTime();
                }
            }
        }

        System.out.println("Total geral de vendas: " + totalGeral);

        System.out.println("\nProdutos mais vendidos:");
        produtosVendidos.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue() + " vendidos"));

        System.out.println("\nPrincipais dias de vendas:");
        vendasPorDia.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> System.out.println(entry.getKey() + ": R$ " + String.format("%.2f", entry.getValue())));

        System.out.println("\nTempo total das mesas:");
        System.out.println("Tempo de abertura até fechamento (total): " + formatarDuracao(duracaoTotalAberturaFechamento));
        System.out.println("Tempo de fechamento até pagamento (total): " + formatarDuracao(duracaoTotalFechamentoPagamento));
    }

    public void dividirConta(int numeroMesa, Scanner scanner) {
    if (mesas.containsKey(numeroMesa)) {
        Mesa mesa = mesas.get(numeroMesa);
        if (mesa.getEstado() == Mesa.EstadoMesa.AGUARDANDO_PAGAMENTO) {
            if (mesa.getOcupantes() <= 1) {
                System.out.println("Não há ocupantes suficientes para dividir a conta.");
                return;
            }

            List<Pedido> pedidos = new ArrayList<>(mesa.getPedidos());
            List<List<Pedido>> divisoes = new ArrayList<>();
            for (int i = 0; i < mesa.getOcupantes(); i++) {
                divisoes.add(new ArrayList<>());
            }

            for (int i = 0; i < pedidos.size(); i++) {
                Pedido pedido = pedidos.get(i);
                System.out.println((i + 1) + ". " + pedido.getProduto().getDescricao() + " - " +
                                   pedido.getQuantidade() + " x " +
                                   String.format("%.2f", pedido.getProduto().getPreco()));
                int ocupanteIndex = -1;
                while (ocupanteIndex < 0 || ocupanteIndex >= mesa.getOcupantes()) {
                    System.out.println("Informe o número do ocupante para este pedido (1 a " + mesa.getOcupantes() + "):");
                    ocupanteIndex = scanner.nextInt() - 1;
                    if (ocupanteIndex < 0 || ocupanteIndex >= mesa.getOcupantes()) {
                        System.out.println("Índice inválido. Por favor, insira um número entre 1 e " + mesa.getOcupantes() + ".");
                    }
                }
                divisoes.get(ocupanteIndex).add(pedido);
            }

            for (int i = 0; i < divisoes.size(); i++) {
                List<Pedido> divPedidos = divisoes.get(i);
                double total = 0;
                System.out.println("Conta do ocupante " + (i + 1) + ":");
                for (Pedido p : divPedidos) {
                    double subtotal = p.getProduto().getPreco() * p.getQuantidade();
                    System.out.println(p.getProduto().getDescricao() + " - " +
                                       p.getQuantidade() + " x " +
                                       String.format("%.2f", p.getProduto().getPreco()) + " = " +
                                       String.format("%.2f", subtotal));
                    total += subtotal;
                }
                double taxaServico = total * 0.10;
                System.out.println("Taxa de serviço (10%): " + String.format("%.2f", taxaServico));
                total += taxaServico;

                Calendar agora = Calendar.getInstance();
                int hora = agora.get(Calendar.HOUR_OF_DAY);
                if (hora >= 1) {
                    double desconto = total * 0.05;
                    System.out.println("Desconto (5%): -" + String.format("%.2f", desconto));
                    total -= desconto;
                }

                System.out.println("Total a pagar pelo ocupante " + (i + 1) + ": " + String.format("%.2f", total));
                System.out.println("Escolha a forma de pagamento para o ocupante " + (i + 1) + ":");
                System.out.println("1. Cartão de Crédito");
                System.out.println("2. Cartão de Débito");
                System.out.println("3. Dinheiro");
                System.out.println("4. PIX");
                int formaPagamentoIndex = lerNumeroValido(scanner, "Informe o número da forma de pagamento:", 1, 4);
                realizarPagamentoOcupante(numeroMesa, i + 1, divPedidos, total, formaPagamentoIndex, scanner);
            }
            mesa.realizarPagamentoInterno(); // Marca a mesa como livre após dividir a conta e realizar o pagamento
        } else {
            System.out.println("Mesa " + numeroMesa + " não está aguardando pagamento.");
        }
    } else {
        System.out.println("Mesa não encontrada.");
    }
}

    private String formatarDuracao(long duracaoMillis) {
        long minutos = TimeUnit.MILLISECONDS.toMinutes(duracaoMillis);
        long horas = minutos / 60;
        minutos = minutos % 60;
        return horas + " horas e " + minutos + " minutos";
    }

    public Map<String, List<Produto>> getCardapioPorCategoria() {
        return cardapioPorCategoria;
    }

    public Mesa getMesa(int numeroMesa) {
        return mesas.get(numeroMesa);
    }

    private int lerNumeroValido(Scanner scanner, String mensagem, int min, int max) {
    int numero;
    while (true) {
        System.out.println(mensagem);
        if (scanner.hasNextInt()) {
            numero = scanner.nextInt();
            if (numero >= min && numero <= max) {
                break;
            } else {
                System.out.println("Por favor, insira um número entre " + min + " e " + max + ".");
            }
        } else {
            System.out.println("Entrada inválida. Por favor, insira um número.");
            scanner.next(); // limpar a entrada inválida
        }
    }
    return numero;
}
}