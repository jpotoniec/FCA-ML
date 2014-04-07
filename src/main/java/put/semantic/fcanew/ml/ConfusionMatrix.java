/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import javax.swing.table.AbstractTableModel;
import put.semantic.fcanew.Expert.Decision;
import put.semantic.fcanew.Expert.Suggestion;

/**
 *
 * @author smaug
 */
public class ConfusionMatrix extends AbstractTableModel {

    private static final String COLS[] = {"", "Accept", "Reject", "Undecied", "Sum"};
    private static final String ROWS[] = {"Accepted", "Rejected", "Sum"};

    private int[][] data = new int[3][2];

    public void add(Suggestion s, Decision d) {
        Boolean a;
        boolean b;
        switch (s) {
            case UNKNOWN:
                a = null;
                break;
            case ACCEPT:
                a = true;
                break;
            case REJECT:
                a = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
        switch (d) {
            case ACCEPT:
                b = true;
                break;
            case REJECT:
                b = false;
                break;
            default:
                throw new IllegalArgumentException();
        }
        add(a, b);
    }

    public void add(Boolean accept, boolean accepted) {
        int col;
        if (accept != null) {
            col = accept ? 0 : 1;
        } else {
            col = 2;
        }
        int row = accepted ? 0 : 1;
        data[col][row]++;
        fireTableCellUpdated(row, col + 1);
    }

    @Override
    public int getRowCount() {
        return ROWS.length;
    }

    @Override
    public int getColumnCount() {
        return COLS.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return ROWS[rowIndex];
        }
        if (columnIndex == getColumnCount() - 1 && rowIndex == getRowCount() - 1) {
            int r = 0;
            for (int i = 0; i < data.length; ++i) {
                for (int j = 0; j < data[i].length; ++j) {
                    r += data[i][j];
                }
            }
            return r;
        }
        if (columnIndex == COLS.length - 1) {
            int r = 0;
            for (int i = 0; i < data.length; ++i) {
                r += data[i][rowIndex];
            }
            return r;
        }
        if (rowIndex == getRowCount() - 1) {
            int r = 0;
            for (int j = 0; j < data[columnIndex - 1].length; ++j) {
                r += data[columnIndex - 1][j];
            }
            return r;
        }
        return data[columnIndex - 1][rowIndex];
    }

    @Override
    public String getColumnName(int column) {
        return COLS[column];
    }

}
