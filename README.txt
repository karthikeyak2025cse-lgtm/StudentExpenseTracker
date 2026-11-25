STUDENT EXPENSE TRACKER - BACKEND ONLY
Java console application with SQLite and JDBC. No HTML, CSS, JavaScript, or frontend.

REQUIREMENTS
- Java JDK 17 or newer
- sqlite-jdbc.jar (included in lib folder)

RUN ON WINDOWS
1. Extract the ZIP file.
2. Open Command Prompt inside the extracted StudentExpenseTrackerBackend folder.
3. Run:
   javac -cp "lib/sqlite-jdbc.jar" -d . StudentExpenseTracker.java
   java -cp ".;lib/sqlite-jdbc.jar" student.expenses.StudentExpenseTracker

RUN ON macOS / Linux
   javac -cp "lib/sqlite-jdbc.jar" -d . StudentExpenseTracker.java
   java -cp ".:lib/sqlite-jdbc.jar" student.expenses.StudentExpenseTracker

FEATURES
- Student registration and login
- Passwords stored using salted PBKDF2-HMAC-SHA256 hashes
- Add, view, search/filter and delete expenses
- Categories: Food, Transport, Books, Fees, Hostel, Shopping,
  Entertainment, Health and Other
- Set and view monthly budgets
- Dashboard with monthly total, count, average and recent expenses
- Budget usage warning at 80% and when budget is exceeded
- Reports: all-time spending, transaction count, largest expense,
  category-wise totals
- SQLite database is created automatically as student_expenses.db

DATABASE TABLES
users: student login and profile details
expenses: individual expense records
budgets: monthly budget per student

OOP / SECURITY
- Encapsulation: Expense class uses private fields and getters.
- JDBC PreparedStatements are used for database operations.
- Each student's records are restricted by their user ID.
- Passwords are salted and hashed; plain-text passwords are not stored.

This is an educational project. It does not connect to banks or payment services.
