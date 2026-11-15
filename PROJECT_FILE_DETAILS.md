# Detailed File Guide: Student Expense & Budget Management System

This project is a Java Swing desktop application for managing a student's income, expenses, monthly budgets, savings goals, and roommate bill splits. It uses Java 21, CSV-based storage, and pure core Java libraries.

---

## 1. Project Root Files

### `pom.xml`
- Maven configuration file.
- Defines the Java project, source directory, compiler settings, and executable JAR packaging.
- Sets Java 21 as the target language version.

### `compile.bat`
- Windows batch script to compile the source code from `src` into `bin`.
- Generates the executable JAR files.
- Used for easy local build setup.

### `run.bat`
- Launches the GUI desktop app using the compiled classes.
- Falls back to the packaged JAR if needed.

### `run_cli.bat`
- Runs the application in CLI mode for terminal/academic demonstration.

### `README.md`
- Overview of the project.
- Includes quick-start instructions, features, and architecture summary.

### `PROJECT_REPORT.md`
- Academic project report describing the project's objective, system architecture, modules, and testing.
- Good for college mini-project submission.

### `PROJECT_FILE_DETAILS.md`
- This file: explains what each component of the project contains.

### `StudentExpenseApp.jar`
- Build artifact containing the compiled Java app with a main class.
- Used as an executable application package.

### `StudentExpenseManager.jar`
- Duplicate packaged app used for launching the project in a simple way.

### `.gitignore`
- Tells Git which files and folders to ignore, such as IDE files and temporary artifacts.

---

## 2. Source Code Structure

```text
src/
└── com/studentexpense/
    ├── Main.java
    ├── cli/
    ├── model/
    ├── service/
    └── ui/
```

### `src/com/studentexpense/Main.java`
- Main entry point of the application.
- Decides whether to start in GUI mode or CLI mode.
- Initializes the app and the main expense manager.

---

## 3. CLI Module

### `src/com/studentexpense/cli/CommandLineInterface.java`
- Terminal-based interface for users who prefer command-line usage.
- Shows a menu with options like:
  - dashboard summary
  - list transactions
  - add expense/income
  - manage budgets
  - savings goals
  - roommate bill splitting
  - export statement
  - switch to GUI
- Useful for viva/demo and headless use cases.

---

## 4. Model Layer

The `model` package contains the business data classes and enums used across the app.

### `src/com/studentexpense/model/Category.java`
- Enum representing student spending categories.
- Examples include food, books, transport, rent, outings, utilities, etc.
- Each category may have a display name and icon/symbol.

### `src/com/studentexpense/model/PaymentMethod.java`
- Enum for cash, UPI, debit card, net banking, etc.
- Used when recording transaction payment methods.

### `src/com/studentexpense/model/TransactionType.java`
- Enum for transaction kind:
  - `INCOME`
  - `EXPENSE`

### `src/com/studentexpense/model/Transaction.java`
- Core transaction record model.
- Stores data such as:
  - title
  - amount
  - type (income or expense)
  - category
  - payment method
  - date
  - notes
- Also supports CSV conversion and parsing.

### `src/com/studentexpense/model/Budget.java`
- Represents the monthly budget set for a category.
- Stores budget details such as category, amount, and month.

### `src/com/studentexpense/model/SavingsGoal.java`
- Tracks personal savings targets.
- Stores:
  - goal name
  - target amount
  - current saved amount
  - deadline
  - category/icon information

### `src/com/studentexpense/model/BillSplit.java`
- Used to split a bill among multiple people.
- Calculates how much each contributor owes.
- Useful for hostel or roommate expense sharing.

---

## 5. Service Layer

The `service` package contains the logic that manages the app state and calculations.

### `src/com/studentexpense/service/ExpenseManager.java`
- Central manager of all financial data.
- Handles:
  - adding transactions
  - deleting/updating transactions
  - saving to CSV
  - loading from CSV
  - resetting data
  - notifying UI components when data changes
- It acts as the main application controller.

### `src/com/studentexpense/service/AnalyticsService.java`
- Performs all calculations and summaries.
- Includes methods for:
  - total income
  - total expenses
  - net balance
  - monthly budget status
  - expense breakdown by category
  - financial health score
  - budget alerts
  - savings progress insights
  - daily burn rate

### `src/com/studentexpense/service/DataPersistenceService.java`
- Handles saving and reading data from CSV files.
- Keeps the app portable without database setup.
- Stores all data in the `data` folder.

---

## 6. UI Layer

The `ui` package holds the Swing graphical user interface.

### `src/com/studentexpense/ui/MainFrame.java`
- Main window of the app.
- Holds the top summary cards and tabbed interface.
- Shows global KPI cards such as:
  - total income
  - total expense
  - net balance
- Contains the main navigation tabs.

### `src/com/studentexpense/ui/ModernUIUtils.java`
- Contains reusable UI styling helpers.
- Includes colors, fonts, card shapes, buttons, empty states, and table styling.
- Helps the application look clean and professional.

### `src/com/studentexpense/ui/DashboardPanel.java`
- Main overview screen.
- Shows:
  - income vs expense cards
  - financial health score
  - welcome banners / warning alerts
  - donut chart breakdown
  - recent transactions table

### `src/com/studentexpense/ui/TrackerPanel.java`
- Transaction entry form and ledger table.
- Allows the user to:
  - add new income or expense
  - choose category and payment method
  - filter by type, category, and search text
  - edit or delete row entries
  - view current transaction history

### `src/com/studentexpense/ui/BudgetPanel.java`
- Monthly category budget planner.
- Shows total budget, spent amount, and remaining amount.
- Displays category-wise progress bars and warnings.

### `src/com/studentexpense/ui/AnalyticsPanel.java`
- Detailed analytics screen for reports and insights.
- Can show charts, trends, performance advice, and budget comparisons.

### `src/com/studentexpense/ui/CustomChartPanel.java`
- Custom Java 2D drawing panel used for charts.
- Creates donut charts and bar-style visualizations without external libraries.

### `src/com/studentexpense/ui/SavingsGoalsPanel.java`
- Displays savings goals with:
  - target amounts
  - current saved values
  - deadline countdown
  - quick add/withdraw actions
- Useful for long-term student financial planning.

### `src/com/studentexpense/ui/BillSplitterPanel.java`
- Interface for splitting bills among friends or roommates.
- Lets user enter total bill amount, participants, and payer names.
- Calculates equal or share-based dues.
- Can log the user's own share to the ledger.

### `src/com/studentexpense/ui/TransactionsPanel.java`
- Alternative transaction table panel for viewing all entries in a structured way.
- Supports filtering and exporting records.

---

## 7. Data Folder

### `data/transactions.csv`
- Stores all income and expense transactions.
- Each row represents a single transaction.

### `data/budgets.csv`
- Stores category budget limits.
- Example: food budget, travel budget, books budget, etc.

### `data/goals.csv`
- Stores savings goals and progress information.

---

## 8. Compiled Output and Build Artifacts

The `bin` folder contains compiled `.class` files generated by the Java compiler.

### `bin/`
- Contains runtime Java bytecode for all classes in the project.
- Not usually edited manually.
- Generated whenever the app is built.

---

## 9. What the App Does

This application helps students manage their money by letting them:

- record income and expenses
- track spending across categories
- compare budget vs actual usage
- save for future goals
- split shared bills with friends
- visualize spending in charts
- maintain data locally in CSV files

It is designed to be simple, lightweight, and useful for academic projects or personal daily use.

---

## 10. Typical User Flow

1. User launches the app using `run.bat` or the JAR file.
2. User adds income or expense entries.
3. Data is stored in CSV files under `data/`.
4. Dashboard updates in real time.
5. User can review budget warnings, savings goals, or bill-sharing breakdowns.
6. User can continue tracking the month’s financial health.

---

## 11. Summary

This project is a complete student finance tracker made with:
- Java Swing for desktop GUI
- Java 2D for visual charts
- CSV file storage for persistence
- layered OOP architecture
- modular design for models, services, and UI

It is suitable for a college mini-project, internship assignment, or personal finance dashboard demonstration.
