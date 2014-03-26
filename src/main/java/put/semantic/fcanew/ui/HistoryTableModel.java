/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.Implication;

/**
 *
 * @author smaug
 */
public class HistoryTableModel extends AbstractTableModel {

    protected static class Entry {

        public Implication i;
        public double p;
        public Expert.Decision d;

        public Entry(Implication i, double p, Expert.Decision d) {
            this.i = i;
            this.p = p;
            this.d = d;
        }
    }

    protected final List<Entry> entries = new ArrayList<>();

    public void add(Implication i, double p, Expert.Decision d) {
        entries.add(new Entry(i, p, d));
        fireTableRowsInserted(entries.size() - 1, entries.size() - 1);
    }

    @Override
    public int getRowCount() {
        return entries.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Implication";
            case 1:
                return "Classifier";
            case 2:
                return "Decision";
        }
        return super.getColumnName(column);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Implication.class;
            case 1:
                return Double.class;
            case 2:
                return Expert.Decision.class;
        }
        return super.getColumnClass(columnIndex); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Entry e = entries.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return e.i;
            case 1:
                return e.p;
            case 2:
                return e.d;
        }
        throw new IllegalArgumentException();
    }

}
