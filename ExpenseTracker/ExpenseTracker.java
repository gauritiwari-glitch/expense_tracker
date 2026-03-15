import java.sql.*;
import java.util.Scanner;

public class ExpenseTracker {

    static final String DB_URL = "jdbc:sqlite:expenses.db";
    static Connection conn;

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTable();
            System.out.println("===== Expense Tracker (SQLite) =====");
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("\n1. Add Expense");
                System.out.println("2. View All Expenses");
                System.out.println("3. View Total");
                System.out.println("4. Delete Expense");
                System.out.println("5. Exit");
                System.out.print("Choose option: ");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1": addExpense(sc); break;
                    case "2": viewExpenses(); break;
                    case "3": viewTotal(); break;
                    case "4": deleteExpense(sc); break;
                    case "5":
                        System.out.println("Goodbye!");
                        conn.close();
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            }
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }

    static void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS expenses (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                     "category TEXT NOT NULL, " +
                     "description TEXT, " +
                     "amount REAL NOT NULL, " +
                     "date TEXT DEFAULT (date('now')))";
        conn.createStatement().execute(sql);
    }

    static void addExpense(Scanner sc) throws SQLException {
        System.out.print("Category (e.g. Food, Travel): ");
        String category = sc.nextLine().trim();
        System.out.print("Description: ");
        String desc = sc.nextLine().trim();
        System.out.print("Amount (in Rs): ");
        double amount;
        try {
            amount = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        String sql = "INSERT INTO expenses (category, description, amount) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, category);
        ps.setString(2, desc);
        ps.setDouble(3, amount);
        ps.executeUpdate();
        System.out.println("Expense added successfully!");
    }

    static void viewExpenses() throws SQLException {
        String sql = "SELECT id, date, category, description, amount FROM expenses ORDER BY id";
        ResultSet rs = conn.createStatement().executeQuery(sql);
        System.out.println("\n--- All Expenses ---");
        System.out.printf("%-5s %-12s %-12s %-20s %s%n", "ID", "Date", "Category", "Description", "Amount (Rs)");
        System.out.println("-".repeat(65));
        boolean found = false;
        while (rs.next()) {
            found = true;
            System.out.printf("%-5d %-12s %-12s %-20s %.2f%n",
                rs.getInt("id"),
                rs.getString("date"),
                rs.getString("category"),
                rs.getString("description"),
                rs.getDouble("amount"));
        }
        if (!found) System.out.println("No expenses found.");
    }

    static void viewTotal() throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery(
            "SELECT category, SUM(amount) as total FROM expenses GROUP BY category");
        System.out.println("\n--- Total by Category ---");
        double grand = 0;
        while (rs.next()) {
            double t = rs.getDouble("total");
            System.out.printf("%-15s : Rs %.2f%n", rs.getString("category"), t);
            grand += t;
        }
        System.out.printf("%-15s : Rs %.2f%n", "GRAND TOTAL", grand);
    }

    static void deleteExpense(Scanner sc) throws SQLException {
        viewExpenses();
        System.out.print("\nEnter ID to delete (0 to cancel): ");
        int id;
        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }
        if (id == 0) return;
        PreparedStatement ps = conn.prepareStatement("DELETE FROM expenses WHERE id = ?");
        ps.setInt(1, id);
        int rows = ps.executeUpdate();
        if (rows > 0) System.out.println("Expense deleted.");
        else System.out.println("ID not found.");
    }
}
