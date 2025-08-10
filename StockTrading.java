import java.awt.*;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;

public class StockTrading extends JFrame{
    JButton BuyStock,SellStock,ViewPortfolio,MarketOverview,Exit;
    JPanel mainPanel;
    CardLayout cardLayout;
    HashMap<String, Double>stockPrices=new HashMap<>();
    HashMap<String,Integer>portfolio=new HashMap<>();
    double cash=10000.0;
    private String fileName;
    public StockTrading(){
        setTitle("Stock Trading Platform");
        setSize(500,400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        stockPrices.put("AAPL",190.5);
        stockPrices.put("GOOGL",2720.3);
        stockPrices.put("AMZN",3450.1);
        stockPrices.put("TSLA",805.22);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(menuPanel(),"Menu");
        mainPanel.add(marketPanel(),"Market");
        mainPanel.add(buyPanel(),"Buy");
        mainPanel.add(sellPanel(),"Sell");
        mainPanel.add(portfolioPanel(),"Portfolio");
        add(mainPanel);
        cardLayout.show(mainPanel,"Menu");
    }
    final JPanel menuPanel(){
        JPanel panel =new JPanel(new GridLayout(5,1,10,10));
        panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
        MarketOverview = new JButton("View Market");
        BuyStock = new JButton("Buy Stock");
        SellStock = new JButton("Sell Stock");
        ViewPortfolio= new JButton("View Portfolio");
        Exit = new JButton("Exit");
        MarketOverview.addActionListener(e -> cardLayout.show(mainPanel, "Market"));
        BuyStock.addActionListener(e -> cardLayout.show(mainPanel, "Buy"));
        SellStock.addActionListener(e -> cardLayout.show(mainPanel, "Sell"));
        ViewPortfolio.addActionListener(e -> cardLayout.show(mainPanel, "Portfolio"));
        Exit.addActionListener(e -> System.exit(0));
        panel.add(MarketOverview);
        panel.add(BuyStock);
        panel.add(SellStock);
        panel.add(ViewPortfolio);
        panel.add(Exit);
        return panel;
    }
    final  JPanel marketPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setText("--- Market Stocks ---\n");

        for (String symbol : stockPrices.keySet()) {
            area.append(symbol + " : $" + stockPrices.get(symbol) + "\n");
        }

        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        panel.add(back, BorderLayout.SOUTH);
        return panel;
    }

    final JPanel buyPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1));
        JTextField symbolField = new JTextField();
        JTextField qtyField = new JTextField();
        JLabel result = new JLabel(" ");

        JButton buy = new JButton("Buy");
        buy.addActionListener(e -> {
            String symbol = symbolField.getText().toUpperCase();
            try {
                int qty = Integer.parseInt(qtyField.getText());
                if (!stockPrices.containsKey(symbol)) {
                    result.setText("Invalid symbol.");
                    return;
                }
                double cost = stockPrices.get(symbol) * qty;
                if (cost > cash) {
                    result.setText("Insufficient funds.");
                    return;
                }
                cash -= cost;
                portfolio.put(symbol, portfolio.getOrDefault(symbol, 0) + qty);
                saveToFile();
                result.setText("Bought " + qty + " " + symbol);
            } catch (NumberFormatException ex) {
                result.setText("Invalid input.");
            }
        });

        panel.add(new JLabel("Enter stock symbol:"));
        panel.add(symbolField);
        panel.add(new JLabel("Enter quantity:"));
        panel.add(qtyField);
        panel.add(buy);
        panel.add(result);

        JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        panel.add(back);

        return panel;
    }

    final JPanel sellPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1));
        JTextField symbolField = new JTextField();
        JTextField qtyField = new JTextField();
        JLabel result = new JLabel(" ");

        JButton sell = new JButton("Sell");
        sell.addActionListener(e -> {
            String symbol = symbolField.getText().toUpperCase();
            try {
                int qty = Integer.parseInt(qtyField.getText());
                int owned = portfolio.getOrDefault(symbol, 0);
                if (qty > owned) {
                    result.setText("Not enough shares.");
                    return;
                }
                double price = stockPrices.get(symbol);
                cash += price * qty;
                if (qty == owned) portfolio.remove(symbol);
                else portfolio.put(symbol, owned - qty);
                saveToFile();
                result.setText("Sold " + qty + " " + symbol);
            } catch (NumberFormatException ex) {
                result.setText("Invalid input.");
            }
        });

        panel.add(new JLabel("Enter stock symbol:"));
        panel.add(symbolField);
        panel.add(new JLabel("Enter quantity:"));
        panel.add(qtyField);
        panel.add(sell);
        panel.add(result);

        JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        panel.add(back);

        return panel;
    }

    final JPanel portfolioPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setEditable(false);

        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> {
            area.setText("Cash: $" + String.format("%.2f", cash) + "\n\n");
            if (portfolio.isEmpty()) {
                area.append("No stocks owned.\n");
                return;
            }
            double total = cash;
            for (String symbol : portfolio.keySet()) {
                int qty = portfolio.get(symbol);
                double price = stockPrices.get(symbol);
                double value = qty * price;
                total += value;
                area.append(symbol + " - " + qty + " shares @ $" + price + " = $" + String.format("%.2f", value) + "\n");
            }
            area.append("\nTotal Portfolio Value: $" + String.format("%.2f", total));
        });

        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        JPanel bottom = new JPanel();
        bottom.add(refresh);
        JButton back = new JButton("Back");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Menu"));
        bottom.add(back);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Cash: $" + cash);
            for (String stock : portfolio.keySet()) {
                writer.println(stock + ": " + portfolio.get(stock) + " shares");
            }
        } 
        catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StockTrading().setVisible(true));
    }

}
