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
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Clean, naive, and direct Expense & Income tracker panel.
 * Equipped with smooth vertical scroll slider for all input options,
 * ensuring zero clipping on any screen resolution.
 */
public class TrackerPanel extends JPanel {

    private final ExpenseManager expenseManager;

    // Form inputs
    private JRadioButton rbExpense;
    private JRadioButton rbIncome;
    private JTextField txtTitle;
    private JTextField txtAmount;
    private JComboBox<Category> cmbCategory;
    private JComboBox<PaymentMethod> cmbMethod;
    private JTextField txtDate;
    private JTextField txtNotes;
    private JLabel lblFormStatus;

    // Table & Filters
    private JTextField txtSearch;
    private JComboBox<String> cmbTypeFilter;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTableCount;

    public TrackerPanel(ExpenseManager expenseManager) {
        this.expenseManager = expenseManager;
        setLayout(new BorderLayout(14, 0));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        initUI();
        refreshData();
    }

    private void initUI() {
        // =========================================================================
        // --- LEFT PANEL: Add Entry Form with Smooth Vertical Scroll Slider ---
        // =========================================================================
        JPanel formCard = ModernUIUtils.createCardPanel();
        formCard.setPreferredSize(new Dimension(350, 0));
        formCard.setLayout(new BorderLayout(0, 8));

        JLabel lblFormHeading = new JLabel("➕ Add New Entry");
        lblFormHeading.setFont(ModernUIUtils.FONT_TITLE);
        lblFormHeading.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        formCard.add(lblFormHeading, BorderLayout.NORTH);

        // Scrollable content panel containing all input fields & options
        JPanel formFields = new JPanel();
        formFields.setLayout(new BoxLayout(formFields, BoxLayout.Y_AXIS));
        formFields.setOpaque(false);
        formFields.setBorder(new EmptyBorder(4, 2, 10, 8));

        // 1. Type selection: Radio buttons
        JLabel lblType = new JLabel("1. Select Entry Type:");
        lblType.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblType.setAlignmentX(Component.LEFT_ALIGNMENT);

        rbExpense = new JRadioButton("Expense (Spending)", true);
        rbIncome = new JRadioButton("Income (Money In)", false);
        rbExpense.setFont(ModernUIUtils.FONT_BODY_BOLD);
        rbIncome.setFont(ModernUIUtils.FONT_BODY_BOLD);
        rbExpense.setForeground(ModernUIUtils.COLOR_DANGER);
        rbIncome.setForeground(ModernUIUtils.COLOR_SUCCESS);
        rbExpense.setOpaque(false);
        rbIncome.setOpaque(false);

        ButtonGroup bgType = new ButtonGroup();
        bgType.add(rbExpense);
        bgType.add(rbIncome);

        JPanel typeRadioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        typeRadioPanel.setOpaque(false);
        typeRadioPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        typeRadioPanel.add(rbExpense);
        typeRadioPanel.add(rbIncome);

        rbExpense.addActionListener(e -> updateCategoryDropdown());
        rbIncome.addActionListener(e -> updateCategoryDropdown());

        // 2. Title
        JLabel lblTitle = new JLabel("2. Title / Description:");
        lblTitle.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTitle = ModernUIUtils.createStyledTextField();
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtTitle.addActionListener(e -> handleSaveEntry());

        // 3. Amount
        JLabel lblAmount = new JLabel("3. Amount in Rupees (₹):");
        lblAmount.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAmount = ModernUIUtils.createStyledTextField();
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtAmount.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtAmount.addActionListener(e -> handleSaveEntry());

        // 4. Category
        JLabel lblCat = new JLabel("4. Category:");
        lblCat.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblCat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbCategory = new JComboBox<>();
        cmbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbCategory.setMaximumRowCount(10); // Slider/scroll inside dropdown
        cmbCategory.setAlignmentX(Component.LEFT_ALIGNMENT);
        updateCategoryDropdown();

        // 5. Payment Mode
        JLabel lblMethod = new JLabel("5. Payment Mode:");
        lblMethod.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblMethod.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbMethod = new JComboBox<>(PaymentMethod.values());
        cmbMethod.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        cmbMethod.setMaximumRowCount(8);
        cmbMethod.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 6. Date
        JLabel lblDate = new JLabel("6. Date (yyyy-MM-dd):");
        lblDate.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDate = ModernUIUtils.createStyledTextField();
        txtDate.setText(LocalDate.now().toString());
        txtDate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtDate.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDate.addActionListener(e -> handleSaveEntry());

        // 7. Notes
        JLabel lblNotes = new JLabel("7. Notes / Remarks (Optional):");
        lblNotes.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNotes = ModernUIUtils.createStyledTextField();
        txtNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        txtNotes.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtNotes.addActionListener(e -> handleSaveEntry());

        // Save Button & Status Feedback inside form
        JButton btnSave = ModernUIUtils.createPrimaryButton("💾 Save Transaction");
        btnSave.setFont(new Font(ModernUIUtils.FONT_NAME, Font.BOLD, 14));
        btnSave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSave.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSave.addActionListener(e -> handleSaveEntry());

        lblFormStatus = new JLabel(" ");
        lblFormStatus.setFont(ModernUIUtils.FONT_SMALL);
        lblFormStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add all elements into scrollable panel
        formFields.add(lblType);
        formFields.add(typeRadioPanel);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblTitle);
        formFields.add(txtTitle);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblAmount);
        formFields.add(txtAmount);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblCat);
        formFields.add(cmbCategory);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblMethod);
        formFields.add(cmbMethod);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblDate);
        formFields.add(txtDate);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblNotes);
        formFields.add(txtNotes);
        formFields.add(Box.createVerticalStrut(12));
        formFields.add(btnSave);
        formFields.add(Box.createVerticalStrut(6));
        formFields.add(lblFormStatus);

        // Wrap the form in a JScrollPane so a scroll slider appears whenever needed!
        JScrollPane formScrollPane = new JScrollPane(formFields);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.setOpaque(false);
        formScrollPane.getViewport().setOpaque(false);

        formCard.add(formScrollPane, BorderLayout.CENTER);
        add(formCard, BorderLayout.WEST);

        // =========================================================================
        // --- RIGHT PANEL: Searchable Ledger Table with Table Scroll Slider ---
        // =========================================================================
        JPanel tableCard = ModernUIUtils.createCardPanel();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Header & Search Toolbar
        JPanel topToolbar = new JPanel(new BorderLayout(10, 0));
        topToolbar.setOpaque(false);

        JLabel lblLedgerTitle = new JLabel("All Recorded Transactions");
        lblLedgerTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblLedgerTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        topToolbar.add(lblLedgerTitle, BorderLayout.WEST);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchControls.setOpaque(false);

        JLabel lblS = new JLabel("Search:");
        lblS.setFont(ModernUIUtils.FONT_BODY_BOLD);

        txtSearch = ModernUIUtils.createStyledTextField();
        txtSearch.setPreferredSize(new Dimension(150, 30));
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilters(); }
            public void removeUpdate(DocumentEvent e) { applyFilters(); }
            public void changedUpdate(DocumentEvent e) { applyFilters(); }
        });

        cmbTypeFilter = ModernUIUtils.createStyledComboBox(new String[]{"All Entries", "Income Only", "Expense Only"});
        cmbTypeFilter.addActionListener(e -> applyFilters());

        searchControls.add(lblS);
        searchControls.add(txtSearch);
        searchControls.add(cmbTypeFilter);

        topToolbar.add(searchControls, BorderLayout.EAST);
        tableCard.add(topToolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Date", "Type", "Title", "Category", "Payment Mode", "Amount (₹)", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        ModernUIUtils.styleTable(table);

        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(85);
        table.getColumnModel().getColumn(2).setPreferredWidth(75);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(140);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(150);

        // Color coding: Green for Income, Red for Expense
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                String s = String.valueOf(val);
                if ("Income".equalsIgnoreCase(s)) {
                    setForeground(ModernUIUtils.COLOR_SUCCESS);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                } else {
                    setForeground(ModernUIUtils.COLOR_DANGER);
                    setFont(ModernUIUtils.FONT_BODY_BOLD);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean isSel, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, isSel, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                String s = String.valueOf(val);
                if (s.startsWith("+")) {
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
        table.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        tableCard.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Bar
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setOpaque(false);

        lblTableCount = new JLabel("Total Records: 0");
        lblTableCount.setFont(ModernUIUtils.FONT_BODY_BOLD);
        lblTableCount.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tableActions.setOpaque(false);

        JButton btnDelete = ModernUIUtils.createDangerButton("🗑️ Delete Selected");
        btnDelete.addActionListener(e -> handleDeleteSelected());

        JButton btnExport = ModernUIUtils.createSecondaryButton("📥 Export CSV");
        btnExport.addActionListener(e -> handleExportCsv());

        tableActions.add(btnDelete);
        tableActions.add(btnExport);

        bottomBar.add(lblTableCount, BorderLayout.WEST);
        bottomBar.add(tableActions, BorderLayout.EAST);

        tableCard.add(bottomBar, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private void updateCategoryDropdown() {
        cmbCategory.removeAllItems();
        boolean isInc = rbIncome.isSelected();
        List<Category> list = isInc ? Category.getIncomeCategories() : Category.getExpenseCategories();
        for (Category c : list) {
            cmbCategory.addItem(c);
        }
    }

    private void handleSaveEntry() {
        String title = txtTitle.getText().trim();
        String amtStr = txtAmount.getText().trim();
        String dateStr = txtDate.getText().trim();
        String notes = txtNotes.getText().trim();

        if (title.isEmpty()) {
            showFormError("Please enter a title (e.g. Pocket Money or Canteen)");
            txtTitle.requestFocus();
            return;
        }

        if (amtStr.isEmpty()) {
            showFormError("Please enter the amount in Rupees (₹)");
            txtAmount.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showFormError("Please enter a valid positive amount");
            txtAmount.requestFocus();
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, Transaction.DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            showFormError("Invalid date! Use format yyyy-MM-dd");
            txtDate.requestFocus();
            return;
        }

        TransactionType type = rbIncome.isSelected() ? TransactionType.INCOME : TransactionType.EXPENSE;
        Category category = (Category) cmbCategory.getSelectedItem();
        PaymentMethod method = (PaymentMethod) cmbMethod.getSelectedItem();

        Transaction tx = new Transaction(title, amount, type, category, date, method, notes);
        expenseManager.addTransaction(tx);

        // Reset form for next entry
        txtTitle.setText("");
        txtAmount.setText("");
        txtNotes.setText("");
        txtDate.setText(LocalDate.now().toString());
        txtTitle.requestFocus();

        lblFormStatus.setText("✅ Saved: " + title + " (₹" + String.format("%.2f", amount) + ")");
        lblFormStatus.setForeground(ModernUIUtils.COLOR_SUCCESS);

        Timer t = new Timer(3500, ev -> lblFormStatus.setText(" "));
        t.setRepeats(false);
        t.start();
    }

    private void showFormError(String msg) {
        lblFormStatus.setText("❌ " + msg);
        lblFormStatus.setForeground(ModernUIUtils.COLOR_DANGER);
    }

    private void handleDeleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an entry in the table to delete.", "Select Row", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String id = (String) tableModel.getValueAt(modelRow, 0);
        String title = (String) tableModel.getValueAt(modelRow, 3);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete transaction: \"" + title + "\" (ID: " + id + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            expenseManager.deleteTransaction(id);
        }
    }

    private void handleExportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("student_ledger_export_" + LocalDate.now() + ".csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                StringBuilder sb = new StringBuilder();
                sb.append("ID,Date,Type,Title,Category,PaymentMethod,Amount,Notes\n");
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    sb.append(String.format("%s,%s,%s,\"%s\",\"%s\",%s,%s,\"%s\"\n",
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
                JOptionPane.showMessageDialog(this, "Ledger exported to:\n" + fileToSave.getAbsolutePath(), "Export Done", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = expenseManager.getTransactions();

        for (Transaction t : transactions) {
            String amountStr = (t.getType() == TransactionType.INCOME ? "+₹" : "-₹") + String.format("%.2f", t.getAmount());
            tableModel.addRow(new Object[]{
                    t.getId(),
                    t.getFormattedDate(),
                    t.getType().getDisplayName(),
                    t.getTitle(),
                    t.getCategory().getDisplayName(),
                    t.getPaymentMethod().getDisplayName(),
                    amountStr,
                    t.getNotes()
            });
        }

        lblTableCount.setText("Total Records: " + transactions.size());
        applyFilters();
    }

    private void applyFilters() {
        if (sorter == null) return;
        String search = txtSearch.getText().trim().toLowerCase();
        String typeFilter = (String) cmbTypeFilter.getSelectedItem();

        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String id = entry.getStringValue(0).toLowerCase();
                String type = entry.getStringValue(2);
                String title = entry.getStringValue(3).toLowerCase();
                String category = entry.getStringValue(4).toLowerCase();
                String notes = entry.getStringValue(7).toLowerCase();

                if (!search.isEmpty() && !title.contains(search) && !category.contains(search) && !notes.contains(search) && !id.contains(search)) {
                    return false;
                }

                if (typeFilter != null) {
                    if (typeFilter.equals("Income Only") && !"Income".equalsIgnoreCase(type)) return false;
                    if (typeFilter.equals("Expense Only") && !"Expense".equalsIgnoreCase(type)) return false;
                }

                return true;
            }
        });
    }
}
