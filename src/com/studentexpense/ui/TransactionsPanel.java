package com.studentexpense.ui;

import com.studentexpense.model.*;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Transaction history panel with real-time keyword search,
 * multi-criteria filtering, CRUD operations, and CSV export in Indian Rupees (₹).
 */
public class TransactionsPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;

    private JTextField txtSearch;
    private JComboBox<String> cmbTypeFilter;
    private JComboBox<String> cmbCategoryFilter;
    private JComboBox<String> cmbDateFilter;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JPanel tableCard;
    private JScrollPane scrollPane;
    private JPanel emptyStatePanel;

    public TransactionsPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
        this.expenseManager = expenseManager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        initUI();
        refreshData();
    }

    private void initUI() {
        // --- 1. Top Header & Action Buttons ---
        JPanel topPanel = new JPanel(new BorderLayout(15, 12));
        topPanel.setOpaque(false);

        JPanel headerLeft = new JPanel(new BorderLayout());
        headerLeft.setOpaque(false);

        JLabel lblTitle = new JLabel("Transaction History");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("View, search, filter, and record all student expenses and income entries");
        lblSub.setFont(ModernUIUtils.FONT_BODY);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        headerLeft.add(lblTitle, BorderLayout.NORTH);
        headerLeft.add(lblSub, BorderLayout.SOUTH);

        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionButtons.setOpaque(false);

        JButton btnAdd = ModernUIUtils.createPrimaryButton("+ Add New");
        btnAdd.addActionListener(e -> mainFrame.showAddTransactionDialog(TransactionType.EXPENSE));

        JButton btnEdit = ModernUIUtils.createSecondaryButton("✏️ Edit");
        btnEdit.addActionListener(e -> handleEditSelected());

        JButton btnDelete = ModernUIUtils.createDangerButton("🗑️ Delete");
        btnDelete.addActionListener(e -> handleDeleteSelected());

        JButton btnExport = ModernUIUtils.createSecondaryButton("📥 Export CSV");
        btnExport.addActionListener(e -> handleExportCsv());

        actionButtons.add(btnAdd);
        actionButtons.add(btnEdit);
        actionButtons.add(btnDelete);
        actionButtons.add(btnExport);

        topPanel.add(headerLeft, BorderLayout.WEST);
        topPanel.add(actionButtons, BorderLayout.EAST);

        // --- 2. Filter & Search Bar ---
        JPanel filterCard = ModernUIUtils.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(ModernUIUtils.FONT_BODY_BOLD);
        txtSearch = ModernUIUtils.createStyledTextField();
        txtSearch.setPreferredSize(new Dimension(180, 32));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        JLabel lblType = new JLabel("Type:");
        lblType.setFont(ModernUIUtils.FONT_BODY_BOLD);
        cmbTypeFilter = ModernUIUtils.createStyledComboBox(new String[]{"All Types", "Expense", "Income"});
        cmbTypeFilter.addActionListener(e -> applyFilters());

        JLabel lblCat = new JLabel("Category:");
        lblCat.setFont(ModernUIUtils.FONT_BODY_BOLD);
        String[] catOptions = new String[Category.values().length + 1];
        catOptions[0] = "All Categories";
        for (int i = 0; i < Category.values().length; i++) {
            catOptions[i + 1] = Category.values()[i].getDisplayName();
        }
        cmbCategoryFilter = ModernUIUtils.createStyledComboBox(catOptions);
        cmbCategoryFilter.addActionListener(e -> applyFilters());

        JLabel lblDate = new JLabel("Period:");
        lblDate.setFont(ModernUIUtils.FONT_BODY_BOLD);
        cmbDateFilter = ModernUIUtils.createStyledComboBox(new String[]{"All Time", "This Month", "Last 30 Days"});
        cmbDateFilter.addActionListener(e -> applyFilters());

        JButton btnReset = ModernUIUtils.createSecondaryButton("Reset Filters");
        btnReset.addActionListener(e -> resetFilters());

        filterCard.add(lblSearch);
        filterCard.add(txtSearch);
        filterCard.add(lblType);
        filterCard.add(cmbTypeFilter);
        filterCard.add(lblCat);
        filterCard.add(cmbCategoryFilter);
        filterCard.add(lblDate);
        filterCard.add(cmbDateFilter);
        filterCard.add(btnReset);

        JPanel northBlock = new JPanel();
        northBlock.setLayout(new BoxLayout(northBlock, BoxLayout.Y_AXIS));
        northBlock.setOpaque(false);
        northBlock.add(topPanel);
        northBlock.add(Box.createVerticalStrut(12));
        northBlock.add(filterCard);

        add(northBlock, BorderLayout.NORTH);

        // --- 3. Table in Card Panel ---
        tableCard = ModernUIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        String[] columnNames = {"ID", "Date", "Title", "Type", "Category", "Payment Method", "Amount (₹)", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        ModernUIUtils.styleTable(transactionTable);

        transactionTable.getColumnModel().getColumn(0).setMaxWidth(80);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(180);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(85);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(160);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        transactionTable.getColumnModel().getColumn(6).setPreferredWidth(105);
        transactionTable.getColumnModel().getColumn(7).setPreferredWidth(180);

        transactionTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String val = String.valueOf(value);
                if ("Income".equalsIgnoreCase(val)) {
                    setForeground(ModernUIUtils.COLOR_SUCCESS);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                } else {
                    setForeground(ModernUIUtils.COLOR_DANGER);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                }
                return c;
            }
        });

        transactionTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                String val = String.valueOf(value);
                if (val.startsWith("+")) {
                    setForeground(ModernUIUtils.COLOR_SUCCESS);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                } else {
                    setForeground(ModernUIUtils.COLOR_DANGER);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                }
                return c;
            }
        });

        sorter = new TableRowSorter<>(tableModel);
        transactionTable.setRowSorter(sorter);

        scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        emptyStatePanel = ModernUIUtils.createEmptyStatePanel(
                "💳",
                "No Transactions Recorded Yet",
                "Start managing your money by adding your first college expense or income entry.",
                "+ Add New Transaction",
                () -> mainFrame.showAddTransactionDialog(TransactionType.EXPENSE)
        );

        tableCard.add(scrollPane, BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = expenseManager.getTransactions();

        if (transactions.isEmpty()) {
            tableCard.removeAll();
            tableCard.add(emptyStatePanel, BorderLayout.CENTER);
        } else {
            tableCard.removeAll();
            tableCard.add(scrollPane, BorderLayout.CENTER);
            for (Transaction t : transactions) {
                String amountStr = (t.getType() == TransactionType.INCOME ? "+₹" : "-₹") + String.format("%.2f", t.getAmount());
                tableModel.addRow(new Object[]{
                        t.getId(),
                        t.getFormattedDate(),
                        t.getTitle(),
                        t.getType().getDisplayName(),
                        t.getCategory().getDisplayName(),
                        t.getPaymentMethod().getDisplayName(),
                        amountStr,
                        t.getNotes()
                });
            }
            applyFilters();
        }

        tableCard.revalidate();
        tableCard.repaint();
    }

    private void applyFilters() {
        if (sorter == null) return;
        String search = txtSearch.getText().trim().toLowerCase();
        String typeFilter = (String) cmbTypeFilter.getSelectedItem();
        String catFilter = (String) cmbCategoryFilter.getSelectedItem();
        String dateFilter = (String) cmbDateFilter.getSelectedItem();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String id = entry.getStringValue(0).toLowerCase();
                String date = entry.getStringValue(1);
                String title = entry.getStringValue(2).toLowerCase();
                String type = entry.getStringValue(3);
                String category = entry.getStringValue(4);
                String notes = entry.getStringValue(7).toLowerCase();

                if (!search.isEmpty() && !title.contains(search) && !notes.contains(search) && !id.contains(search)) {
                    return false;
                }

                if (typeFilter != null && !typeFilter.equals("All Types") && !type.equalsIgnoreCase(typeFilter)) {
                    return false;
                }

                if (catFilter != null && !catFilter.equals("All Categories") && !category.equalsIgnoreCase(catFilter)) {
                    return false;
                }

                if (dateFilter != null && !dateFilter.equals("All Time")) {
                    try {
                        LocalDate txDate = LocalDate.parse(date, Transaction.DATE_FORMATTER);
                        LocalDate today = LocalDate.now();
                        if (dateFilter.equals("This Month")) {
                            if (!YearMonth.from(txDate).equals(YearMonth.now())) return false;
                        } else if (dateFilter.equals("Last 30 Days")) {
                            if (txDate.isBefore(today.minusDays(30))) return false;
                        }
                    } catch (Exception ignored) {}
                }

                return true;
            }
        });
    }

    private void resetFilters() {
        txtSearch.setText("");
        cmbTypeFilter.setSelectedIndex(0);
        cmbCategoryFilter.setSelectedIndex(0);
        cmbDateFilter.setSelectedIndex(0);
        if (sorter != null) sorter.setRowFilter(null);
    }

    private void handleEditSelected() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
        String id = (String) tableModel.getValueAt(modelRow, 0);

        Transaction target = expenseManager.getTransactions().stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (target != null) {
            mainFrame.showEditTransactionDialog(target);
        }
    }

    private void handleDeleteSelected() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = transactionTable.convertRowIndexToModel(selectedRow);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete transaction:\n\"" + title + "\" (ID: " + id + ")?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            expenseManager.deleteTransaction(id);
            JOptionPane.showMessageDialog(this, "Transaction deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleExportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("student_expenses_export_" + LocalDate.now() + ".csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("ID,Date,Title,Type,Category,PaymentMethod,Amount,Notes\n");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    sb.append(String.format("%s,%s,\"%s\",%s,\"%s\",%s,%s,\"%s\"\n",
                            tableModel.getValueAt(i, 0),
                            tableModel.getValueAt(i, 1),
                            tableModel.getValueAt(i, 2),
                            tableModel.getValueAt(i, 3),
                            tableModel.getValueAt(i, 4),
                            tableModel.getValueAt(i, 5),
                            tableModel.getValueAt(i, 6),
                            tableModel.getValueAt(i, 7)));
                }
                expenseManager.getPersistenceService().exportFinancialStatement(sb.toString(), fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Transactions exported successfully to:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
