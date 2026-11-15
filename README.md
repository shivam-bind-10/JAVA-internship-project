# 🎓 Student Expense and Budget Management System
> **Academic Java Mini Project for College/University Submission**  
> *Developed using Core Java (Java 21), Java Swing GUI, 2D Graphics, and Layered Architecture.*
> **Currency:** Indian Rupee (₹) | **Default State:** Clean Zero Records (No mock data clutter)

---

## 🌟 Executive Summary
The **Student Expense and Budget Management System** is a standalone, full-featured desktop application designed specifically for college and university students in India to manage their personal finances, track pocket money and stipends in **Indian Rupees (₹)**, set monthly budget limits per category, visualize spending patterns through interactive Java 2D charts, track long-term savings goals, and split bills fairly among roommates and project groups.

### ✨ What's New in this Edition:
- **Clean Zero-Start State**: Starts completely clean with **₹0.00** across all cards and ledgers (no unwanted dummy data). An optional *"Load Demo Data"* button is available in the sidebar if you ever want to showcase sample data during a viva or project demonstration.
- **Modern Left-Sidebar Navigation**: Replaces old clunky tabs with a modern, intuitive sidebar layout (similar to modern fintech tools like Zerodha / Cred). Features permanent `+ Add Expense` and `+ Add Income` action buttons visible across all screens.
- **Indian Rupee (₹) Currency**: All calculations, inputs, metrics, tables, and reports are formatted in **₹ Rupees**.

---

## 🚀 Quick Start (One-Click Run)

### 1. Launch Modern GUI
Double-click:
```cmd
run.bat
```
*Or via terminal:*
```cmd
java -jar StudentExpenseManager.jar
```

### 2. Launch Terminal CLI Mode (Headless / Viva Demo)
Double-click:
```cmd
run_cli.bat
```
*Or via terminal:*
```cmd
java -jar StudentExpenseManager.jar --cli
```

### 3. Recompile from Source
Double-click:
```cmd
compile.bat
```

---

## 📊 Navigation & Key Modules

| Navigation Item | Description & Capabilities |
| :--- | :--- |
| **📊 Dashboard** | Live overview of financial health (Total Inflow ₹, Total Outflow ₹, Net Balance ₹, Health Score). Shows proactive budget warning banners and instant breakdown donut chart. |
| **💳 All Transactions** | Full ledger with real-time text search, type filters (Expense / Income), category filters, period filters (This Month, Last 30 Days), and CSV export. |
| **🎯 Monthly Budgets** | Set monthly spending limits per category (Food, Books, Hostel Rent, Transport, Outings, Utilities). Visual progress bars change color dynamically (🟢 Safe < 75%, 🟡 Warning 75–99%, 🔴 Exceeded ≥ 100%). |
| **📈 Visual Analytics** | Custom Java 2D **Donut Pie Chart** with legends, **Budget vs Actual Bar Chart**, daily burn rate (₹/day), and personalized financial advice tips. |
| **🏆 Savings Goals** | Track savings for items like *"Coding Laptop Upgrade"*, *"Semester Hackathon"*, or *"Emergency Buffer"*. Includes deadline countdown and deposit/withdrawal buttons. |
| **⚡ Split with Friends** | Enter bill amount (₹), payer, and friends' names. Instantly computes fair shares, shows who owes whom, and logs your share into your personal expense ledger in 1 click. |
| **⚡ Quick Actions (Sidebar)** | Always-accessible `+ Add Expense` and `+ Add Income` buttons right in the sidebar. |

---

## 🏗️ Project Architecture & OOP Principles

```
src/
└── com/studentexpense/
    ├── Main.java                         # Application Entry Point (CLI & GUI routing)
    ├── model/                            # Data Models & Business Entities (₹)
    │   ├── Category.java                 # Enum: College categories
    │   ├── PaymentMethod.java            # Enum: UPI, Cash, Debit, Net Banking
    │   ├── TransactionType.java          # Enum: INCOME vs EXPENSE
    │   ├── Transaction.java              # Primary Transaction model & CSV parser
    │   ├── Budget.java                   # Monthly Budget entity
    │   ├── SavingsGoal.java              # Savings Goal target entity
    │   └── BillSplit.java                # Group Bill Split calculator entity
    ├── service/                          # Business Logic & Persistence
    │   ├── ExpenseManager.java           # Central state manager (Observer Pattern)
    │   ├── AnalyticsService.java         # Financial calculations, KPIs, advice
    │   └── DataPersistenceService.java   # CSV Storage (Zero mock data default)
    ├── ui/                               # Presentation Layer (Java Swing)
    │   ├── MainFrame.java                # Modern Left-Sidebar Navigation & CardLayout
    │   ├── ModernUIUtils.java            # Aesthetic styles, palettes, empty states
    │   ├── CustomChartPanel.java         # Java 2D Graphics renderer (Donut & Bar in ₹)
    │   ├── DashboardPanel.java           # Summary KPIs & overview tab
    │   ├── TransactionsPanel.java        # Searchable transaction ledger tab
    │   ├── BudgetPanel.java              # Category budget monitoring tab
    │   ├── AnalyticsPanel.java           # Visual charts & insights tab
    │   ├── SavingsGoalsPanel.java        # Savings goals tracking tab
    │   └── BillSplitterPanel.java        # Roommate bill split tab
    └── cli/                              # Terminal Presentation Layer
        └── CommandLineInterface.java     # Interactive console menu for viva in ₹
```

---

## 📋 System Requirements
- **Operating System**: Windows 10/11, macOS, or Linux.
- **Java Runtime**: Java JDK / JRE 17 or higher (tested on Java 21 LTS).
- **Disk Space**: Less than 10 MB.
- **Dependencies**: None (100% Core Java Standard Library).

---

## 📄 License & Academic Integrity
This project is prepared as an academic submission for the Object-Oriented Programming (OOP) / Software Engineering Course.
