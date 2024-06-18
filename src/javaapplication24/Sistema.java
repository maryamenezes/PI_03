package javaapplication24;

import java.util.Scanner;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Sistema {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Restaurante restaurante = new Restaurante(10, "jdbc:postgresql://localhost:5432/restaurante", "postgres", "13042005i"); // Supondo 10 mesas
        int opcao = -1;

        while (opcao != 0) {
            mostrarOpcoes();
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    restaurante.mostrarMapaDeMesas();
                    break;
                case 2:
                    int mesaAbrir = lerNumeroValido(scanner, "Informe o número da mesa para abrir:", 1, 10);
                    int ocupantes = lerNumeroValido(scanner, "Informe o número de ocupantes:", 1, 10);
                    restaurante.abrirMesa(mesaAbrir, ocupantes);
                    break;
                case 3:
                    int mesaPedido = lerNumeroValido(scanner, "Informe o número da mesa para adicionar pedido:", 1, 10);
                    boolean continuarAdicionando = true;

                    while (continuarAdicionando) {
                        boolean voltarParaCategorias = true;

                        while (voltarParaCategorias) {
                            String categoriaEscolhida = null;
                            while (categoriaEscolhida == null) {
                                restaurante.exibirCategorias();
                                int categoriaIndex = lerNumeroValido(scanner, "Escolha a categoria pelo número:", 0, restaurante.getCardapioPorCategoria().size());
                                if (categoriaIndex == 0) {
                                    voltarParaCategorias = false;
                                    continuarAdicionando = false;
                                    break;
                                }
                                categoriaEscolhida = obterCategoriaPorIndice(restaurante, categoriaIndex - 1);
                            }

                            if (!voltarParaCategorias || categoriaEscolhida == null) {
                                break;
                            }

                            int produtoIndex = -1;
                            while (produtoIndex < 0 || produtoIndex >= restaurante.getCardapioPorCategoria().get(categoriaEscolhida).size()) {
                                restaurante.exibirProdutosDaCategoria(categoriaEscolhida);
                                produtoIndex = lerNumeroValido(scanner, "Escolha o produto pelo número dentro da categoria " + categoriaEscolhida + ":", 0, restaurante.getCardapioPorCategoria().get(categoriaEscolhida).size());
                                if (produtoIndex == 0) {
                                    voltarParaCategorias = true;
                                    break;
                                }
                                produtoIndex--;
                                voltarParaCategorias = false;
                            }

                            if (!voltarParaCategorias) {
                                int quantidade = lerNumeroValido(scanner, "Informe a quantidade:", 1, 100);
                                restaurante.adicionarPedido(mesaPedido, categoriaEscolhida, produtoIndex, quantidade);
                            }
                        }

                        if (voltarParaCategorias) {
                            continuarAdicionando = false;
                        } else {
                            System.out.println("Deseja adicionar mais produtos a este pedido? (s/n)");
                            scanner.nextLine(); // Limpar o buffer do scanner
                            String resposta = scanner.nextLine().trim().toLowerCase();
                            if (!resposta.equals("s")) {
                                continuarAdicionando = false;
                            }
                        }
                    }
                    break;
                case 4:
                    int mesaFechar = lerNumeroValido(scanner, "Informe o número da mesa para fechar:", 1, 10);
                    restaurante.fecharMesa(mesaFechar);
                    break;
                case 5:
    int mesaPagamento = lerNumeroValido(scanner, "Informe o número da mesa para realizar o pagamento:", 1, 10);
    restaurante.exibirComanda(mesaPagamento, false);
    Mesa mesaParaPagamento = restaurante.getMesa(mesaPagamento);

    if (mesaParaPagamento != null && mesaParaPagamento.getOcupantes() > 1) {
        System.out.println("Deseja dividir a conta? (s/n)");
        scanner.nextLine(); // Limpar o buffer do scanner
        String respostaDividir = scanner.nextLine().trim().toLowerCase();
        if (respostaDividir.equals("s")) {
            restaurante.dividirConta(mesaPagamento, scanner);
        } else {
            realizarPagamento(scanner, restaurante, mesaPagamento);
        }
    } else {
        realizarPagamento(scanner, restaurante, mesaPagamento);
    }
    break;

                case 6:
                    gerarRelatorioFinanceiro(scanner, restaurante);
                    break;
                case 7:
                    int mesaComanda = lerNumeroValido(scanner, "Informe o número da mesa para ver a comanda:", 1, 10);
                    restaurante.exibirComanda(mesaComanda, true);
                    break;
                case 8:
                    int numeroComanda = lerNumeroValido(scanner, "Informe o número da comanda para ver uma comanda antiga:", 1, Integer.MAX_VALUE);
                    restaurante.exibirComandaAntiga(numeroComanda);
                    break;
                case 9:
                    int mesaCancelar = lerNumeroValido(scanner, "Informe o número da mesa para cancelar pedido:", 1, 10);
                    restaurante.cancelarPedido(mesaCancelar);
                    break;
                case 0:
                    System.out.println("Saindo do sistema...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
        scanner.close();
    }
    
    private static void realizarPagamento(Scanner scanner, Restaurante restaurante, int mesaPagamento) {
    System.out.println("Escolha a forma de pagamento:");
    System.out.println("1. Cartão de Crédito");
    System.out.println("2. Cartão de Débito");
    System.out.println("3. Dinheiro");
    System.out.println("4. PIX");
    int formaPagamentoIndex = lerNumeroValido(scanner, "Informe o número da forma de pagamento:", 1, 4);
    restaurante.realizarPagamento(mesaPagamento, formaPagamentoIndex, scanner);
}


    private static void mostrarOpcoes() {
        System.out.println("Escolha uma opção:");
        System.out.println("1. Ver Mapa de Mesas");
        System.out.println("2. Abrir Mesa");
        System.out.println("3. Adicionar Pedido");
        System.out.println("4. Fechar Mesa");
        System.out.println("5. Realizar Pagamento");
        System.out.println("6. Gerar Relatório Financeiro");
        System.out.println("7. Ver Comanda");
        System.out.println("8. Ver Comanda Antiga");
        System.out.println("9. Cancelar Pedido");
        System.out.println("0. Sair");
    }

    private static int lerNumeroValido(Scanner scanner, String mensagem, int min, int max) {
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
                scanner.next();
            }
        }
        return numero;
    }

    private static String obterCategoriaPorIndice(Restaurante restaurante, int categoriaIndex) {
        int i = 0;
        for (String categoria : restaurante.getCardapioPorCategoria().keySet()) {
            if (i == categoriaIndex) {
                return categoria;
            }
            i++;
        }
        return null;
    }

    private static void gerarRelatorioFinanceiro(Scanner scanner, Restaurante restaurante) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

        System.out.println("Informe a data inicial (dd/MM/yyyy HH:mm) ou deixe em branco para ignorar:");
        scanner.nextLine();
        String dataInicioStr = scanner.nextLine().trim();
        Date dataInicio = null;
        if (!dataInicioStr.isEmpty()) {
            try {
                dataInicio = sdf.parse(dataInicioStr);
            } catch (ParseException e) {
                System.out.println("Data inicial inválida. Ignorando filtro de data inicial.");
            }
        }

        System.out.println("Informe a data final (dd/MM/yyyy HH:mm) ou deixe em branco para ignorar:");
        String dataFimStr = scanner.nextLine().trim();
        Date dataFim = null;
        if (!dataFimStr.isEmpty()) {
            try {
                dataFim = sdf.parse(dataFimStr);
            } catch (ParseException e) {
                System.out.println("Data final inválida. Ignorando filtro de data final.");
            }
        }

        System.out.println("Informe o número da mesa ou deixe em branco para todas as mesas:");
        String mesaStr = scanner.nextLine().trim();
        int numeroMesa = -1;
        if (!mesaStr.isEmpty()) {
            try {
                numeroMesa = Integer.parseInt(mesaStr);
            } catch (NumberFormatException e) {
                System.out.println("Número da mesa inválido. Ignorando filtro de mesa.");
            }
        }

        restaurante.gerarRelatorioFinanceiro(dataInicio, dataFim, numeroMesa);
    }
}
