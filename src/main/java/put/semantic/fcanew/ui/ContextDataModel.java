/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import javax.swing.table.AbstractTableModel;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.POD;
import put.semantic.fcanew.PartialContext;

/**
 *
 * @author smaug
 */
public class ContextDataModel extends AbstractTableModel implements PartialContext.ContextChangedListener {

    private PartialContext context;

    public ContextDataModel(PartialContext context) {
        this.context = context;
        this.context.addContextChangedListener(this);
    }

    @Override
    public int getRowCount() {
        return context.getPODs().size();
    }

    @Override
    public int getColumnCount() {
        return context.getAttributes().size() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        POD pod = context.getPODs().get(rowIndex);
        if (columnIndex == 0) {
            return pod.getId();
        }
        Attribute a = context.getAttributes().get(columnIndex - 1);
        if (pod.getPositive().contains(a)) {
            return "+";
        } else if (pod.getNegative().contains(a)) {
            return "-";
        } else {
            return " ";
        }
    }

    @Override
    public String getColumnName(int column) {
        if (column == 0) {
            return "Individual";
        }
        return context.getAttributes().get(column - 1).toString();
    }

    @Override
    public void contextChanged(PartialContext context) {
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex > 0;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            throw new IllegalArgumentException();
        }
        POD pod = context.getPODs().get(rowIndex);
        Attribute a = context.getAttributes().get(columnIndex - 1);
        System.out.printf("'%s'\n", aValue.toString());
        switch (aValue.toString()) {
            case "+":
                pod.setPositive(a);
                break;
            case "-":
                pod.setNegative(a);
                break;
            default:
                pod.setUnknown(a);
                break;
        }
        fireTableRowsUpdated(rowIndex, rowIndex);
    }
}
