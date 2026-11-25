package student.expenses;

import java.sql.*;
import java.util.*;
import java.io.Console;
import java.security.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class StudentExpenseTracker {
    private static final Scanner IN = new Scanner(System.in);
    private static final Random RANDOM = new SecureRandom();
    private static final String DB_URL = "jdbc:sqlite:student_expenses.db";
    private static final int ITERATIONS = 120000;
    private static final int KEY_BITS = 256;
    private static final List<String> CATEGORIES = List.of(
        "Food", "Transport", "Books", "Fees", "Hostel", "Shopping", "Entertainment", "Health", "Other");

    public static void main(String[] args) {
        try (Connection db = DriverManager.getConnection(DB_URL)) {
            initialize(db);
            System.out.println("====================================");
            System.out.println("       STUDENT EXPENSE TRACKER      ");
            System.out.println("          Backend Console           ");
            System.out.println("====================================");
            while (true) {
                System.out.println("\nMAIN MENU\n1. Register\n2. Login\n3. Exit");
                switch (prompt("Enter your choice: ").trim()) {
                    case "1" -> register(db);
                    case "2" -> login(db);
                    case "3" -> { System.out.println("Thank you. Goodbye!"); return; }
                    default -> System.out.println("Invalid choice.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            System.out.println("Check that lib/sqlite-jdbc.jar is available.");
        }
    }

    private static void initialize(Connection db) throws SQLException {
        try (Statement s = db.createStatement()) {
            s.execute("PRAGMA foreign_keys = ON");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL UNIQUE, full_name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password_hash TEXT NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS expenses (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, description TEXT NOT NULL, category TEXT NOT NULL, amount REAL NOT NULL CHECK(amount > 0), expense_date TEXT NOT NULL, note TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS budgets (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, month TEXT NOT NULL, amount REAL NOT NULL CHECK(amount > 0), UNIQUE(user_id, month))");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS idx_expenses_user_date ON expenses(user_id, expense_date)");
        }
    }

    private static void register(Connection db) {
        String username = nonEmpty("Username: ");
        String name = nonEmpty("Full name: ");
        String email = nonEmpty("Email: ");
        String password = passwordPrompt("Password (minimum 8 characters): ");
        if (password.length() < 8) { System.out.println("Password must contain at least 8 characters."); return; }
        String sql = "INSERT INTO users(username,full_name,email,password_hash) VALUES(?,?,?,?)";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            p.setString(1, username); p.setString(2, name); p.setString(3, email); p.setString(4, hashPassword(password));
            p.executeUpdate(); System.out.println("Registration successful. Please log in.");
        } catch (SQLException e) { System.out.println("Username or email already exists."); }
    }

    private static void login(Connection db) throws SQLException {
        String username = nonEmpty("Username: ");
        String password = passwordPrompt("Password: ");
        String sql = "SELECT id,full_name,password_hash FROM users WHERE username=?";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            p.setString(1, username);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next() || !verifyPassword(password, r.getString("password_hash"))) {
                    System.out.println("Invalid username or password."); return;
                }
                int userId = r.getInt("id");
                System.out.println("\nWelcome, " + r.getString("full_name") + "!");
                userMenu(db, userId);
            }
        }
    }

    private static void userMenu(Connection db, int userId) throws SQLException {
        while (true) {
            System.out.println("\nSTUDENT MENU\n1. Dashboard\n2. Add Expense\n3. View All Expenses\n4. Search / Filter Expenses\n5. Delete Expense\n6. Set Monthly Budget\n7. View Monthly Budget\n8. Reports\n9. Logout");
            switch (prompt("Enter your choice: ").trim()) {
                case "1" -> dashboard(db, userId);
                case "2" -> addExpense(db, userId);
                case "3" -> listExpenses(db, userId, null, null);
                case "4" -> searchExpenses(db, userId);
                case "5" -> deleteExpense(db, userId);
                case "6" -> setBudget(db, userId);
                case "7" -> showBudget(db, userId, currentMonth());
                case "8" -> reports(db, userId);
                case "9" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void addExpense(Connection db, int userId) {
        String description = nonEmpty("Expense description: ");
        System.out.println("Categories: " + String.join(", ", CATEGORIES));
        String category = nonEmpty("Category: ");
        if (CATEGORIES.stream().noneMatch(c -> c.equalsIgnoreCase(category))) {
            System.out.println("Invalid category. Choose one from the category list."); return;
        }
        double amount = readAmount("Amount (Rs.): ");
        if (amount <= 0) { System.out.println("Amount must be greater than zero."); return; }
        String date = nonEmpty("Date (YYYY-MM-DD): ");
        try { java.time.LocalDate.parse(date); }
        catch (Exception e) { System.out.println("Invalid date. Use YYYY-MM-DD."); return; }
        String note = prompt("Note (optional): ").trim();
        Expense expense = new Expense(description, normalizeCategory(category), amount, date, note);
        String sql = "INSERT INTO expenses(user_id,description,category,amount,expense_date,note) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            p.setInt(1, userId); p.setString(2, expense.getDescription()); p.setString(3, expense.getCategory());
            p.setDouble(4, expense.getAmount()); p.setString(5, expense.getDate()); p.setString(6, expense.getNote());
            p.executeUpdate(); System.out.println("Expense added successfully.");
            warnBudget(db, userId, date.substring(0, 7));
        } catch (SQLException e) { System.out.println("Could not save expense: " + e.getMessage()); }
    }

    private static void listExpenses(Connection db, int userId, String category, String month) {
        StringBuilder sql = new StringBuilder("SELECT id,description,category,amount,expense_date,note FROM expenses WHERE user_id=?");
        if (category != null) sql.append(" AND lower(category)=lower(?)");
        if (month != null) sql.append(" AND substr(expense_date,1,7)=?");
        sql.append(" ORDER BY expense_date DESC,id DESC");
        try (PreparedStatement p = db.prepareStatement(sql.toString())) {
            int i = 1; p.setInt(i++, userId); if (category != null) p.setString(i++, category); if (month != null) p.setString(i, month);
            try (ResultSet r = p.executeQuery()) {
                System.out.println("\nID   DATE        DESCRIPTION                 CATEGORY          AMOUNT");
                System.out.println("--------------------------------------------------------------------------");
                boolean found = false; double total = 0;
                while (r.next()) { found = true; total += r.getDouble("amount");
                    System.out.printf("%-4d %-11s %-27.27s %-16s Rs. %.2f%n", r.getInt("id"), r.getString("expense_date"), r.getString("description"), r.getString("category"), r.getDouble("amount"));
                    if (r.getString("note") != null && !r.getString("note").isBlank()) System.out.println("     Note: " + r.getString("note"));
                }
                if (!found) System.out.println("No expenses found.");
                System.out.printf("Total: Rs. %.2f%n", total);
            }
        } catch (SQLException e) { System.out.println("Unable to load expenses."); }
    }

    private static void searchExpenses(Connection db, int userId) {
        System.out.println("1. Filter by category\n2. Filter by month\n3. Search description");
        switch (prompt("Choose filter: ").trim()) {
            case "1" -> listExpenses(db, userId, nonEmpty("Category: "), null);
            case "2" -> { String m = nonEmpty("Month (YYYY-MM): "); if (m.matches("\\d{4}-\\d{2}")) listExpenses(db,userId,null,m); else System.out.println("Invalid month format."); }
            case "3" -> searchDescription(db, userId, nonEmpty("Search text: "));
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void searchDescription(Connection db, int userId, String q) {
        String sql = "SELECT id,description,category,amount,expense_date,note FROM expenses WHERE user_id=? AND (lower(description) LIKE lower(?) OR lower(category) LIKE lower(?) OR lower(coalesce(note,'')) LIKE lower(?)) ORDER BY expense_date DESC";
        try (PreparedStatement p = db.prepareStatement(sql)) {
            String term = "%" + q + "%"; p.setInt(1,userId); p.setString(2,term); p.setString(3,term); p.setString(4,term);
            try (ResultSet r = p.executeQuery()) { printRows(r); }
        } catch (SQLException e) { System.out.println("Search failed."); }
    }

    private static void printRows(ResultSet r) throws SQLException {
        boolean found=false; double total=0;
        System.out.println("\nID   DATE        DESCRIPTION                 CATEGORY          AMOUNT");
        while(r.next()){found=true; total+=r.getDouble("amount"); System.out.printf("%-4d %-11s %-27.27s %-16s Rs. %.2f%n",r.getInt("id"),r.getString("expense_date"),r.getString("description"),r.getString("category"),r.getDouble("amount"));}
        if(!found)System.out.println("No matching expenses found."); System.out.printf("Total: Rs. %.2f%n",total);
    }

    private static void deleteExpense(Connection db, int userId) {
        int id = readInt("Expense ID to delete: ");
        try (PreparedStatement p = db.prepareStatement("DELETE FROM expenses WHERE id=? AND user_id=?")) {
            p.setInt(1,id); p.setInt(2,userId); int count=p.executeUpdate();
            System.out.println(count==1 ? "Expense deleted successfully." : "Expense not found.");
        } catch(SQLException e){System.out.println("Delete failed.");}
    }

    private static void setBudget(Connection db, int userId) {
        String month = nonEmpty("Month (YYYY-MM): ");
        if (!month.matches("\\d{4}-(0[1-9]|1[0-2])")) { System.out.println("Enter month in YYYY-MM format."); return; }
        double amount=readAmount("Monthly budget (Rs.): "); if(amount<=0){System.out.println("Budget must be greater than zero.");return;}
        String sql="INSERT INTO budgets(user_id,month,amount) VALUES(?,?,?) ON CONFLICT(user_id,month) DO UPDATE SET amount=excluded.amount";
        try(PreparedStatement p=db.prepareStatement(sql)){p.setInt(1,userId);p.setString(2,month);p.setDouble(3,amount);p.executeUpdate();System.out.println("Budget saved successfully.");showBudget(db,userId,month);}
        catch(SQLException e){System.out.println("Could not save budget.");}
    }

    private static void showBudget(Connection db, int userId, String month) {
        try {
            double budget=0; try(PreparedStatement p=db.prepareStatement("SELECT amount FROM budgets WHERE user_id=? AND month=?")){p.setInt(1,userId);p.setString(2,month);try(ResultSet r=p.executeQuery()){if(r.next())budget=r.getDouble(1);}}
            double spent=monthTotal(db,userId,month);
            System.out.println("\nBUDGET SUMMARY - "+month); System.out.printf("Budget:    Rs. %.2f%nSpent:     Rs. %.2f%nRemaining: Rs. %.2f%n",budget,spent,budget-spent);
            if(budget==0)System.out.println("No budget set for this month.");else if(spent>budget)System.out.println("Warning: You exceeded your budget!");else System.out.printf("Budget used: %.1f%%%n",spent/budget*100);
        }catch(SQLException e){System.out.println("Unable to load budget.");}
    }

    private static void dashboard(Connection db, int userId) {
        String month=currentMonth();
        try(PreparedStatement p=db.prepareStatement("SELECT COUNT(*),COALESCE(SUM(amount),0),COALESCE(AVG(amount),0) FROM expenses WHERE user_id=? AND substr(expense_date,1,7)=?")){
            p.setInt(1,userId);p.setString(2,month);try(ResultSet r=p.executeQuery()){r.next();System.out.println("\nDASHBOARD - "+month);System.out.println("Number of expenses: "+r.getInt(1));System.out.printf("Total spent: Rs. %.2f%nAverage expense: Rs. %.2f%n",r.getDouble(2),r.getDouble(3));}
            showBudget(db,userId,month);System.out.println("\nRECENT EXPENSES");listRecent(db,userId);
        }catch(SQLException e){System.out.println("Unable to load dashboard.");}
    }

    private static void listRecent(Connection db,int userId)throws SQLException{
        try(PreparedStatement p=db.prepareStatement("SELECT id,description,category,amount,expense_date,note FROM expenses WHERE user_id=? ORDER BY expense_date DESC,id DESC LIMIT 5")){p.setInt(1,userId);try(ResultSet r=p.executeQuery()){printRows(r);}}
    }

    private static void reports(Connection db,int userId) {
        try(PreparedStatement p=db.prepareStatement("SELECT COUNT(*),COALESCE(SUM(amount),0),COALESCE(MAX(amount),0) FROM expenses WHERE user_id=?")){
            p.setInt(1,userId);try(ResultSet r=p.executeQuery()){r.next();System.out.println("\nEXPENSE REPORT");System.out.println("Total transactions: "+r.getInt(1));System.out.printf("All-time spending: Rs. %.2f%nLargest expense: Rs. %.2f%n",r.getDouble(2),r.getDouble(3));}
            System.out.println("\nCATEGORY-WISE SPENDING");
            try(PreparedStatement c=db.prepareStatement("SELECT category,SUM(amount) total FROM expenses WHERE user_id=? GROUP BY category ORDER BY total DESC")){c.setInt(1,userId);try(ResultSet r=c.executeQuery()){boolean any=false;while(r.next()){any=true;System.out.printf("%-18s Rs. %.2f%n",r.getString(1),r.getDouble(2));}if(!any)System.out.println("No expenses recorded.");}}
        }catch(SQLException e){System.out.println("Unable to generate report.");}
    }

    private static void warnBudget(Connection db,int userId,String month)throws SQLException{
        try(PreparedStatement p=db.prepareStatement("SELECT amount FROM budgets WHERE user_id=? AND month=?")){p.setInt(1,userId);p.setString(2,month);try(ResultSet r=p.executeQuery()){if(r.next()){double spent=monthTotal(db,userId,month),budget=r.getDouble(1);if(spent>budget)System.out.printf("WARNING: Monthly budget exceeded by Rs. %.2f%n",spent-budget);else if(spent>=budget*.8)System.out.println("Notice: You have used at least 80% of your monthly budget.");}}}
    }
    private static double monthTotal(Connection db,int userId,String month)throws SQLException{
        try(PreparedStatement p=db.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM expenses WHERE user_id=? AND substr(expense_date,1,7)=?")){p.setInt(1,userId);p.setString(2,month);try(ResultSet r=p.executeQuery()){r.next();return r.getDouble(1);}}
    }

    private static String normalizeCategory(String c){return CATEGORIES.stream().filter(x->x.equalsIgnoreCase(c)).findFirst().orElse(c);}
    private static String currentMonth(){return java.time.LocalDate.now().toString().substring(0,7);}
    private static String prompt(String message){System.out.print(message);return IN.nextLine();}
    private static String nonEmpty(String message){while(true){String v=prompt(message).trim();if(!v.isEmpty())return v;System.out.println("This field cannot be empty.");}}
    private static double readAmount(String message){while(true){try{return Double.parseDouble(prompt(message).trim());}catch(NumberFormatException e){System.out.println("Enter a valid amount.");}}}
    private static int readInt(String message){while(true){try{return Integer.parseInt(prompt(message).trim());}catch(NumberFormatException e){System.out.println("Enter a valid integer.");}}}
    private static String passwordPrompt(String message){Console c=System.console();if(c!=null)return new String(c.readPassword("%s",message));return prompt(message);}
    private static String hashPassword(String password){byte[] salt=new byte[16];RANDOM.nextBytes(salt);return Base64.getEncoder().encodeToString(salt)+":"+Base64.getEncoder().encodeToString(derive(password,salt));}
    private static boolean verifyPassword(String password,String stored){try{String[] a=stored.split(":");return MessageDigest.isEqual(Base64.getDecoder().decode(a[1]),derive(password,Base64.getDecoder().decode(a[0])));}catch(Exception e){return false;}}
    private static byte[] derive(String password,byte[] salt){PBEKeySpec spec=new PBEKeySpec(password.toCharArray(),salt,ITERATIONS,KEY_BITS);try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(Exception e){throw new IllegalStateException(e);}finally{spec.clearPassword();}}

    // Encapsulation: expense fields are private and validated through the constructor.
    private static class Expense {
        private final String description, category, date, note; private final double amount;
        Expense(String description,String category,double amount,String date,String note){if(description==null||description.isBlank()||amount<=0)throw new IllegalArgumentException("Invalid expense");this.description=description;this.category=category;this.amount=amount;this.date=date;this.note=note;}
        String getDescription(){return description;} String getCategory(){return category;} double getAmount(){return amount;} String getDate(){return date;} String getNote(){return note;}
    }
}
