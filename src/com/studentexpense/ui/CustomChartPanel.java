package com.studentexpense.ui;

import com.studentexpense.model.Category;
import com.studentexpense.service.AnalyticsService.CategoryBudgetStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Custom-rendered Java 2D chart panel rendering Donut Pie Charts
 * and Budget vs Actual Bar Charts in Indian Rupees (₹).
 */
public class CustomChartPanel extends JPanel {

    public enum ChartType {
        DONUT_PIE,
        BUDGET_BAR
    }

    private ChartType chartType = ChartType.DONUT_PIE;
    private Map<Category, Double> categoryData;
    private List<CategoryBudgetStatus> budgetData;

    // Harmonious modern chart color palette
    private static final Color[] CHART_COLORS = {
            new Color(59, 130, 246),   // Blue
            new Color(16, 185, 129),   // Emerald Green
            new Color(245, 158, 11),   // Amber
            new Color(239, 68, 68),    // Red
            new Color(139, 92, 246),   // Purple
            new Color(236, 72, 153),   // Pink
            new Color(20, 184, 166),   // Teal
            new Color(249, 115, 22),   // Orange
            new Color(107, 114, 128)   // Gray
    };

    public CustomChartPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(420, 280));
    }

    public void setDonutData(Map<Category, Double> data) {
        this.chartType = ChartType.DONUT_PIE;
        this.categoryData = data;
        repaint();
    }

    public void setBarData(List<CategoryBudgetStatus> data) {
        this.chartType = ChartType.BUDGET_BAR;
        this.budgetData = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (chartType == ChartType.DONUT_PIE) {
            paintDonutChart(g2);
        } else {
            paintBarChart(g2);
        }

        g2.dispose();
    }

    private void paintDonutChart(Graphics2D g2) {
        if (categoryData == null || categoryData.isEmpty()) {
            drawEmptyState(g2, "No expenses recorded yet. Click '+ Add Expense' to begin.");
            return;
        }

        double total = categoryData.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total <= 0) {
            drawEmptyState(g2, "Total expenses: ₹0.00");
            return;
        }

        int width = getWidth();
        int height = getHeight();

        // Chart area on left, legend on right
        int chartSize = Math.min(width / 2, height - 30);
        int chartX = 20;
        int chartY = (height - chartSize) / 2;

        double curAngle = 90.0; // Start at 12 o'clock
        int colorIdx = 0;

        List<Map.Entry<Category, Double>> entries = new ArrayList<>(categoryData.entrySet());
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<Category, Double> entry : entries) {
            double value = entry.getValue();
            if (value <= 0) continue;

            double arcAngle = (value / total) * 360.0;
            Color color = CHART_COLORS[colorIdx % CHART_COLORS.length];
            g2.setColor(color);
            g2.fill(new Arc2D.Double(chartX, chartY, chartSize, chartSize, curAngle, -arcAngle, Arc2D.PIE));

            curAngle -= arcAngle;
            colorIdx++;
        }

        // Draw donut center hole
        int holeSize = (int) (chartSize * 0.58);
        int holeX = chartX + (chartSize - holeSize) / 2;
        int holeY = chartY + (chartSize - holeSize) / 2;
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));
        g2.setColor(ModernUIUtils.COLOR_BORDER);
        g2.draw(new Ellipse2D.Double(holeX, holeY, holeSize, holeSize));

        // Center text: Total
        g2.setColor(ModernUIUtils.COLOR_TEXT_MUTED);
        g2.setFont(ModernUIUtils.FONT_SMALL);
        FontMetrics fmSmall = g2.getFontMetrics();
        String totalLabel = "TOTAL SPENT";
        g2.drawString(totalLabel, holeX + (holeSize - fmSmall.stringWidth(totalLabel)) / 2, holeY + holeSize / 2 - 6);

        g2.setColor(ModernUIUtils.COLOR_TEXT_DARK);
        g2.setFont(ModernUIUtils.FONT_BODY_BOLD);
        FontMetrics fmBold = g2.getFontMetrics();
        String totalAmountStr = String.format("₹%.2f", total);
        g2.drawString(totalAmountStr, holeX + (holeSize - fmBold.stringWidth(totalAmountStr)) / 2, holeY + holeSize / 2 + 14);

        // Draw Legend on right
        int legendX = chartX + chartSize + 25;
        int legendY = 30;
        colorIdx = 0;

        g2.setFont(ModernUIUtils.FONT_SMALL);
        for (Map.Entry<Category, Double> entry : entries) {
            if (legendY + 16 > height - 10) break;

            Color color = CHART_COLORS[colorIdx % CHART_COLORS.length];
            double pct = (entry.getValue() / total) * 100.0;

            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(legendX, legendY - 9, 10, 10, 3, 3));

            g2.setColor(ModernUIUtils.COLOR_TEXT_DARK);
            String itemText = String.format("%s: ₹%.2f (%.1f%%)",
                    truncate(entry.getKey().getDisplayName(), 16), entry.getValue(), pct);
            g2.drawString(itemText, legendX + 16, legendY);

            legendY += 22;
            colorIdx++;
        }
    }

    private void paintBarChart(Graphics2D g2) {
        if (budgetData == null || budgetData.isEmpty()) {
            drawEmptyState(g2, "No budgets configured yet. Click '+ Set / Edit Budget' to begin.");
            return;
        }

        int width = getWidth();
        int height = getHeight();
        int paddingLeft = 140;
        int paddingRight = 30;
        int paddingTop = 20;
        int barHeight = 18;
        int gap = 14;

        int y = paddingTop;
        int availableWidth = width - paddingLeft - paddingRight;

        for (CategoryBudgetStatus status : budgetData) {
            if (y + barHeight > height - 10) break;

            g2.setFont(ModernUIUtils.FONT_SMALL);
            g2.setColor(ModernUIUtils.COLOR_TEXT_DARK);
            String catName = truncate(status.getCategory().getDisplayName(), 18);
            g2.drawString(catName, 10, y + 13);

            g2.setColor(new Color(241, 245, 249));
            g2.fill(new RoundRectangle2D.Float(paddingLeft, y, availableWidth, barHeight, 6, 6));

            double max = Math.max(status.getBudgetLimit(), status.getAmountSpent());
            int spentWidth = max > 0 ? (int) ((status.getAmountSpent() / max) * availableWidth) : 0;
            spentWidth = Math.min(availableWidth, Math.max(4, spentWidth));

            Color barColor = ModernUIUtils.COLOR_SUCCESS;
            if (status.isExceeded()) {
                barColor = ModernUIUtils.COLOR_DANGER;
            } else if (status.isWarning()) {
                barColor = ModernUIUtils.COLOR_WARNING;
            }

            g2.setColor(barColor);
            g2.fill(new RoundRectangle2D.Float(paddingLeft, y, spentWidth, barHeight, 6, 6));

            g2.setColor(ModernUIUtils.COLOR_TEXT_DARK);
            String amountStr = String.format("₹%.0f / ₹%.0f", status.getAmountSpent(), status.getBudgetLimit());
            g2.drawString(amountStr, paddingLeft + 8, y + 13);

            y += barHeight + gap;
        }
    }

    private void drawEmptyState(Graphics2D g2, String msg) {
        g2.setFont(ModernUIUtils.FONT_BODY);
        g2.setColor(ModernUIUtils.COLOR_TEXT_MUTED);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(msg)) / 2;
        int y = getHeight() / 2;
        g2.drawString(msg, Math.max(10, x), y);
    }

    private String truncate(String text, int max) {
        if (text.length() <= max) return text;
        return text.substring(0, max - 2) + "..";
    }
}
