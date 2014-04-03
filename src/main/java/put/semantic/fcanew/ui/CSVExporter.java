/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import au.com.bytecode.opencsv.CSVWriter;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;

/**
 *
 * @author smaug
 */
public class CSVExporter {

    private static final JFileChooser fileChooser;

    static {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
    }

    private static void addToAllTables(Container parent, JPopupMenu menu) {
        for (Component c : parent.getComponents()) {
            if (c instanceof JTable) {
                ((JTable) c).setComponentPopupMenu(menu);
            } else if (c instanceof Container) {
                addToAllTables((Container) c, menu);
            }
        }
    }

    public static void addToAllTables(final Container frame) {
        Action exportAction = new AbstractAction("Export to CSV") {

            @Override
            public void actionPerformed(ActionEvent e) {
                JTable table = null;
                Object src = e.getSource();
                if (src instanceof JMenuItem) {
                    src = ((JMenuItem) src).getParent();
                }
                if (src instanceof JPopupMenu) {
                    src = ((JPopupMenu) src).getInvoker();
                }
                if (src instanceof JTable) {
                    table = (JTable) src;
                }
                if (table != null) {
                    if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                        try {
                            export(table.getModel(), fileChooser.getSelectedFile());
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(frame, ex);
                            ex.printStackTrace();
                        }
                    }
                }

            }
        };
        JPopupMenu menu = new JPopupMenu();
        menu.add(exportAction);
        addToAllTables(frame, menu);
    }

    public static void export(TableModel data, File f) throws IOException {
        try (CSVWriter w = new CSVWriter(new FileWriter(f))) {
            String[] line = new String[data.getColumnCount()];
            for (int col = 0; col < line.length; ++col) {
                line[col] = data.getColumnName(col);
            }
            w.writeNext(line);
            for (int row = 0; row < data.getRowCount(); ++row) {
                for (int col = 0; col < line.length; ++col) {
                    line[col] = data.getValueAt(row, col).toString();
                }
                w.writeNext(line);
            }
        }
    }
}
