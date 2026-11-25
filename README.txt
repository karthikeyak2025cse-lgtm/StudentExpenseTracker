# Student Expense Tracker

## 1. Project Overview

The **Student Expense Tracker** is a Java-based backend application designed to help students manage and monitor their daily expenses effectively. Students often face difficulties in tracking their spending, managing monthly budgets, and understanding where their money is being spent.

This project provides a simple and organized way to record daily expenses, manage budgets, view expense details, and analyze spending activities. It helps students develop better financial management habits and control unnecessary spending.

The application is developed as a **backend-only project without a graphical user interface (Frontend)**. Users interact with the system through the console, while Java handles the application logic and SQLite is used to store the data.

## 2. Objectives of the Project

The main objectives of the Student Expense Tracker are:

- To help students record their daily expenses.
- To maintain expense details in a structured database.
- To manage monthly budgets effectively.
- To monitor daily and monthly spending.
- To categorize expenses for better understanding.
- To reduce unnecessary expenses.
- To generate expense summaries and reports.
- To improve students' financial planning and saving habits.

## 3. Features of the Project

### 3.1 User Registration

- Allows new users to create an account.
- Collects necessary user information.
- Stores user details securely in the database.
- Supports individual user accounts.

### 3.2 User Login

- Allows registered users to log in.
- Validates user credentials.
- Provides access to expense management features.
- Uses password hashing for improved password security.

### 3.3 Expense Management

- Add new expenses.
- View recorded expenses.
- Store expense amount, category, date, and description.
- Maintain expense records in the database.
- Track spending activities.

### 3.4 Budget Management

- Set a budget for a selected period.
- Monitor expenses against the allocated budget.
- View remaining budget.
- Identify when spending approaches or exceeds the budget.

### 3.5 Dashboard and Reports

- View expense summaries.
- Monitor total spending.
- Understand spending patterns.
- Review budget and expense information.

## 4. Technologies Used

| Technology | Purpose |
|---|---|
| Java | Backend programming |
| SQLite | Database management |
| JDBC | Database connectivity |
| SQL | Database operations |
| OOP Concepts | Code organization and reusability |
| PBKDF2-HMAC-SHA256 | Password hashing |

## 5. System Requirements

### Hardware Requirements

- Processor: Intel Core i3 or above
- RAM: Minimum 4 GB
- Storage: Minimum 500 MB free space
- System: Desktop or Laptop

### Software Requirements

- Operating System: Windows / Linux / macOS
- Programming Language: Java
- JDK: Java Development Kit
- Database: SQLite
- IDE: Eclipse / IntelliJ IDEA / Visual Studio Code
- JDBC Driver: SQLite JDBC Driver

## 6. Application Modules

The project is divided into the following modules:

1. **User Registration Module** – Creates a new user account.
2. **User Login Module** – Authenticates registered users.
3. **Expense Management Module** – Adds and displays expense records.
4. **Budget Management Module** – Creates and monitors budgets.
5. **Dashboard Module** – Displays an overview of financial activities.
6. **Reports Module** – Provides expense summaries and spending information.

## 7. System Architecture

The application follows a simple backend architecture.

**User → Java Application → Business Logic → JDBC → SQLite Database**

### Architecture Description

- **User Layer:** Accepts user input through the console.
- **Application Layer:** Handles registration, login, expenses, and budgets.
- **Business Logic Layer:** Processes user requests and performs calculations.
- **Database Connectivity Layer:** Uses JDBC to communicate with SQLite.
- **Database Layer:** Stores user, expense, and budget information.

## 8. Database Design

The application uses an SQLite database to store information.

### USERS Table

Stores registered user information.

| Field | Description |
|---|---|
| user_id | Unique user identification |
| name | User name |
| email | User email address |
| password_hash | Hashed password information |

### EXPENSES Table

Stores the expenses entered by users.

| Field | Description |
|---|---|
| expense_id | Unique expense identification |
| user_id | Associated user ID |
| amount | Expense amount |
| category | Expense category |
| date | Expense date |
| description | Expense details |

### BUDGETS Table

Stores budget information.

| Field | Description |
|---|---|
| budget_id | Unique budget identification |
| user_id | Associated user ID |
| amount | Allocated budget |
| period | Budget period |

## 9. Object-Oriented Programming Concepts

The project uses important Object-Oriented Programming concepts.

### Encapsulation
Data members are maintained within classes, and access is controlled through methods.

### Abstraction
Complex internal operations are separated from the operations performed by the user.

### Inheritance
Common functionality can be shared between related classes where required.

### Polymorphism
Methods can be implemented or used in different forms to support flexible application behaviour.

## 10. Security Features

- User passwords are stored as password hashes rather than plain text.
- PBKDF2-HMAC-SHA256 is used for password hashing.
- Salt is used to strengthen password protection.
- User authentication is performed before accessing account-related operations.
- Database operations are handled through JDBC.

## 11. Installation and Setup

Follow these steps to run the project:

**Step 1: Install Java**

Download and install the Java Development Kit (JDK) on your system.

**Step 2: Install an IDE**

Use Eclipse, IntelliJ IDEA, or Visual Studio Code to open the project.

**Step 3: Download the Project**

Clone the repository using:

```bash
git clone https://github.com/your-username/student-expense-tracker.git
```

**Step 4: Open the Project**

Open the downloaded project folder in your preferred Java IDE.

**Step 5: Configure SQLite JDBC**

Add the SQLite JDBC driver to the project's classpath or build configuration.

**Step 6: Configure the Database**

Ensure that the SQLite database path is correctly configured in the application.

**Step 7: Run the Application**

Run the main Java class to start the Student Expense Tracker application.

*Note: Update the repository URL, main class name, and database configuration according to your actual project files.*

## 12. Working Process

1. The user starts the application.
2. A new user can register with the required details.
3. The registered user logs in.
4. The user can add and view expenses.
5. The user can create and monitor a budget.
6. The application calculates and displays expense information.
7. Data is stored and retrieved from the SQLite database.

## 13. Advantages

- Easy to use and understand.
- Helps students control their spending.
- Maintains expense records systematically.
- Supports budget planning.
- Reduces manual expense calculations.
- Provides organized financial information.
- Uses a lightweight SQLite database.
- Demonstrates Java, JDBC, SQL, and OOP concepts.

## 14. Future Enhancements

The following features can be added in future versions:

- Develop a web-based user interface.
- Add graphical expense charts.
- Provide monthly and yearly financial reports.
- Add expense reminders.
- Introduce savings goal management.
- Export reports in PDF or Excel format.
- Add cloud database support.
- Develop a mobile application.
- Provide advanced spending analysis.

## 15. Conclusion

The Student Expense Tracker is a useful backend application that helps students manage their daily expenses and monthly budgets. It provides features such as user registration, login, expense management, budget planning, and expense reporting.

The project demonstrates the practical implementation of Java programming, Object-Oriented Programming concepts, JDBC connectivity, SQLite database management, and basic security techniques.

This application helps students understand their spending habits, maintain financial discipline, and make better financial decisions.

## 16. Author Details

**Project Title:** Student Expense Tracker

**Department:** Computer Science and Engineering

**Institution:** Rajalakshmi Engineering College, Thandalam

**Developed By:**

- G.E. HARI PRASADH – 250701226
- S.P. GIRINATH – 250701956
- S. VISHAL KUMAR – 250701957
- K. KARTHIKEYA – 250701959

---

## Thank You

**Student Expense Tracker – Manage Your Money, Track Your Expenses, and Plan Your Budget!**
