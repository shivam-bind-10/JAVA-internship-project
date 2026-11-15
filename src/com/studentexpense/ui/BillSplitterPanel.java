package com.studentexpense.ui;

import com.studentexpense.model.*;
import com.studentexpense.service.ExpenseManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * College roommate and group bill splitter calculating fair shares
 * in Indian Rupees (₹) with smooth scroll sliders for all screen resolutions.
 */
public class BillSplitterPanel extends JPanel {

    private final ExpenseManager expenseManager;
    private final MainFrame mainFrame;

    private JTextField txtBillName;
    private JTextField txtTotalAmount;
    private JTextField txtPayer;
    private JTextField txtParticipants;

    private JTextArea txtSummaryResult;
    private DefaultTableModel splitTableModel;
    private JTable splitTable;

    private double lastMyShare = 0.0;
    private String lastBillName = "";

    public BillSplitterPanel(ExpenseManager expenseManager, MainFrame mainFrame) {
        this.expenseManager = expenseManager;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 10));
        setBackground(ModernUIUtils.COLOR_BG);
        setBorder(new EmptyBorder(10, 14, 10, 14));

        initUI();
    }

    private void initUI() {
        // --- 1. Top Header ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("⚡ Student Group Bill & Roommate Splitter");
        lblTitle.setFont(ModernUIUtils.FONT_TITLE);
        lblTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);

        JLabel lblSub = new JLabel("Fairly split shared food, hostel utilities, group project expenses, or cab fares in Rupees (₹)");
        lblSub.setFont(ModernUIUtils.FONT_BODY);
        lblSub.setForeground(ModernUIUtils.COLOR_TEXT_MUTED);

        topPanel.add(lblTitle, BorderLayout.NORTH);
        topPanel.add(lblSub, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // --- 2. Main Content: Left Form, Right Output ---
        JPanel contentGrid = new JPanel(new GridLayout(1, 2, 14, 0));
        contentGrid.setOpaque(false);

        // Left Form Card with Scroll Slider
        JPanel formCard = ModernUIUtils.createCardPanel();
        formCard.setLayout(new BorderLayout(0, 8));

        JLabel lblFormTitle = new JLabel("Bill Details");
        lblFormTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblFormTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        formCard.add(lblFormTitle, BorderLayout.NORTH);

        JPanel formCenter = new JPanel();
        formCenter.setLayout(new BoxLayout(formCenter, BoxLayout.Y_AXIS));
        formCenter.setOpaque(false);
        formCenter.setBorder(new EmptyBorder(4, 2, 8, 8));

        JPanel formFields = new JPanel(new GridLayout(4, 2, 8, 10));
        formFields.setOpaque(false);

        JLabel lblName = new JLabel("Bill Description:");
        lblName.setFont(ModernUIUtils.FONT_BODY_BOLD);
        txtBillName = ModernUIUtils.createStyledTextField();

        JLabel lblAmount = new JLabel("Total Bill Amount (₹):");
        lblAmount.setFont(ModernUIUtils.FONT_BODY_BOLD);
        txtTotalAmount = ModernUIUtils.createStyledTextField();

        JLabel lblPayer = new JLabel("Who Paid the Bill?:");
        lblPayer.setFont(ModernUIUtils.FONT_BODY_BOLD);
        txtPayer = ModernUIUtils.createStyledTextField();

        JLabel lblParts = new JLabel("Participants (Comma-separated):");
        lblParts.setFont(ModernUIUtils.FONT_BODY_BOLD);
        txtParticipants = ModernUIUtils.createStyledTextField();

        formFields.add(lblName);
        formFields.add(txtBillName);
        formFields.add(lblAmount);
        formFields.add(txtTotalAmount);
        formFields.add(lblPayer);
        formFields.add(txtPayer);
        formFields.add(lblParts);
        formFields.add(txtParticipants);

        JPanel btnActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnActions.setOpaque(false);

        JButton btnCalculate = ModernUIUtils.createPrimaryButton("⚡ Calculate Split");
        btnCalculate.addActionListener(e -> calculateSplit());

        JButton btnSample = ModernUIUtils.createSecondaryButton("Load Example");
        btnSample.addActionListener(e -> loadExampleData());

        JButton btnClear = ModernUIUtils.createSecondaryButton("Clear");
        btnClear.addActionListener(e -> clearForm());

        btnActions.add(btnSample);
        btnActions.add(btnClear);
        btnActions.add(btnCalculate);

        formCenter.add(formFields);
        formCenter.add(Box.createVerticalStrut(12));
        formCenter.add(btnActions);

        // Wrap left form in a JScrollPane with scroll slider
        JScrollPane formScrollPane = new JScrollPane(formCenter);
        formScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        formScrollPane.setBorder(BorderFactory.createEmptyBorder());
        formScrollPane.setOpaque(false);
        formScrollPane.getViewport().setOpaque(false);

        formCard.add(formScrollPane, BorderLayout.CENTER);

        // Right Results Card
        JPanel resultCard = ModernUIUtils.createCardPanel();
        resultCard.setLayout(new BorderLayout(0, 10));

        JLabel lblResultTitle = new JLabel("Split Breakdown & Balances");
        lblResultTitle.setFont(ModernUIUtils.FONT_SUBTITLE);
        lblResultTitle.setForeground(ModernUIUtils.COLOR_TEXT_DARK);
        resultCard.add(lblResultTitle, BorderLayout.NORTH);

        String[] cols = {"Participant", "Share (₹)", "Status / Balance"};
        splitTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        splitTable = new JTable(splitTableModel);
        ModernUIUtils.styleTable(splitTable);

        JScrollPane scrollTable = new JScrollPane(splitTable);
        scrollTable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollTable.getVerticalScrollBar().setUnitIncrement(16);
        scrollTable.setPreferredSize(new Dimension(300, 130));
        scrollTable.getViewport().setBackground(Color.WHITE);

        txtSummaryResult = new JTextArea();
        txtSummaryResult.setFont(ModernUIUtils.FONT_BODY);
        txtSummaryResult.setEditable(false);
        txtSummaryResult.setLineWrap(true);
        txtSummaryResult.setWrapStyleWord(true);
        txtSummaryResult.setText("Fill in the bill details on the left and click 'Calculate Split' to see fair-share breakdown.");
        txtSummaryResult.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ModernUIUtils.COLOR_BORDER, 1, true),
                new EmptyBorder(8, 8, 8, 8)));

        JScrollPane scrollSummary = new JScrollPane(txtSummaryResult);
        scrollSummary.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollSummary.getVerticalScrollBar().setUnitIncrement(16);

        JButton btnLogExpense = ModernUIUtils.createSuccessButton("➕ Record My Share as Expense");
        btnLogExpense.addActionListener(e -> recordMyShare());

        JPanel resultCenter = new JPanel(new BorderLayout(0, 8));
        resultCenter.setOpaque(false);
        resultCenter.add(scrollTable, BorderLayout.NORTH);
        resultCenter.add(scrollSummary, BorderLayout.CENTER);
        resultCenter.add(btnLogExpense, BorderLayout.SOUTH);

        resultCard.add(resultCenter, BorderLayout.CENTER);

        contentGrid.add(formCard);
        contentGrid.add(resultCard);

        add(contentGrid, BorderLayout.CENTER);
    }

    private void loadExampleData() {
        txtBillName.setText("Hostel Pizza & Snacks Treat");
        txtTotalAmount.setText("1200.00");
        txtPayer.setText("Me (Self)");
        txtParticipants.setText("Me (Self), Rahul, Aman, Sneha");
        calculateSplit();
    }

    private void clearForm() {
        txtBillName.setText("");
        txtTotalAmount.setText("");
        txtPayer.setText("");
        txtParticipants.setText("");
        splitTableModel.setRowCount(0);
        txtSummaryResult.setText("Fill in the bill details on the left and click 'Calculate Split' to see fair-share breakdown.");
        lastMyShare = 0.0;
        lastBillName = "";
    }

    private void calculateSplit() {
        String billName = txtBillName.getText().trim();
        String totalStr = txtTotalAmount.getText().trim();
        String payer = txtPayer.getText().trim();
        String partStr = txtParticipants.getText().trim();

        if (billName.isEmpty() || totalStr.isEmpty() || payer.isEmpty() || partStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all bill fields.", "Missing Fields", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double total;
        try {
            total = Double.parseDouble(totalStr);
            if (total <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid positive total amount.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] rawParticipants = partStr.split(",");
        List<String> participants = new ArrayList<>();
        for (String p : rawParticipants) {
            String clean = p.trim();
            if (!clean.isEmpty() && !participants.contains(clean)) {
                participants.add(clean);
            }
        }

        if (participants.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please specify at least 1 participant.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!participants.contains(payer)) {
            participants.add(0, payer);
        }

        BillSplit split = new BillSplit(billName, total, payer, participants, LocalDate.now());
        expenseManager.addBillSplit(split);

        splitTableModel.setRowCount(0);
        double share = split.getSharePerPerson();
        lastMyShare = share;
        lastBillName = billName;

        for (String person : participants) {
            String status;
            if (person.equalsIgnoreCase(payer)) {
                status = String.format("PAID TOTAL (Receives ₹%.2f back)", total - share);
            } else {
                status = String.format("OWES ₹%.2f to %s", share, payer);
            }
            splitTableModel.addRow(new Object[]{
                    person,
                    String.format("₹%.2f", share),
                    status
            });
        }

        txtSummaryResult.setText(split.getFormattedSummary());
    }

    private void recordMyShare() {
        if (lastMyShare <= 0) {
            JOptionPane.showMessageDialog(this, "Please calculate a valid split first.", "Notice", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Record your share (₹%.2f) for \"%s\" as a Food/Dining expense?", lastMyShare, lastBillName),
                "Log Expense", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Transaction t = new Transaction(
                    "Split Share: " + lastBillName,
                    lastMyShare,
                    TransactionType.EXPENSE,
                    Category.FOOD,
                    LocalDate.now(),
                    PaymentMethod.UPI,
                    "Generated from Roommate Bill Splitter");
            expenseManager.addTransaction(t);
            JOptionPane.showMessageDialog(this, "Expense recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
