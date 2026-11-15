package com.studentexpense;

import com.studentexpense.cli.CommandLineInterface;
import com.studentexpense.service.ExpenseManager;
import com.studentexpense.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Main application entry point for Student Expense and Budget Management System.
 * Supports both modern Java Swing Graphical User Interface (GUI)
 * and interactive Command Line Interface (CLI) mode.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize core expense manager and persistence
        ExpenseManager expenseManager = new ExpenseManager();

        // Check if CLI mode was explicitly requested or if system is headless
        boolean cliMode = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                cliMode = true;
                break;
            }
        }

        if (cliMode || GraphicsEnvironment.isHeadless()) {
            CommandLineInterface cli = new CommandLineInterface(expenseManager);
            cli.start();
        } else {
            // Launch GUI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                try {
                    // Apply system look and feel for native rendering
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                    // Configure global font rendering hints
                    System.setProperty("awt.useSystemAAFontSettings", "on");
                    System.setProperty("swing.aatext", "true");
                } catch (Exception e) {
                    System.err.println("Note: Using default look and feel: " + e.getMessage());
                }

                MainFrame frame = new MainFrame(expenseManager);
                frame.setVisible(true);
            });
        }
    }
}

