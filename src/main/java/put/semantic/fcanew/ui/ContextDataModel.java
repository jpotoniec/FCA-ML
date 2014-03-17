/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import javax.swing.table.AbstractTableModel;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.POD;
import put.semantic.fcanew.PartialContext;

/**
 *
 * @author smaug
 */
public class ContextDataModel extends AbstractTableModel implements PartialContext.ContextChangedListener {

    private static final String SPECIAL_ATTRIBUTES[] = {"Individual", "Premises", "Conclusions"};
    private PartialContext context;
    private Implication currentImplication;

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
        return context.getAttributes().size() + SPECIAL_ATTRIBUTES.length;
    }

    public void setCurrentImplication(Implication currentImplication) {
        this.currentImplication = currentImplication;
        for (int row = 0; row < getRowCount(); ++row) {
            for (int col = 0; col < SPECIAL_ATTRIBUTES.length; ++col) {
                fireTableCellUpdated(row, col);
            }
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        POD pod = context.getPODs().get(rowIndex);
        if (!isAttributeColumn(columnIndex)) {
            switch (columnIndex) {
                case 0:
                    return pod.getId();
                case 1:
                    if (currentImplication != null) {
                        return pod.getPositive().containsAll(currentImplication.getPremises()) ? "+" : "-";
                    }
                    return "";
                case 2:
                    if (currentImplication != null) {
                        if (pod.getPositive().containsAll(currentImplication.getConclusions())) {
                            return "+";
                        }
                        if (pod.getNegative().containsAny(currentImplication.getConclusions())) {
                            return "-";
                        }
                    }
                    return "";
            }
        }
        Attribute a = context.getAttributes().get(getAttributeIndex(columnIndex));
        if (pod.getPositive().contains(a)) {
            return "+";
        } else if (pod.getNegative().contains(a)) {
            return "-";
        } else {
            return " ";
        }
    }

    protected int getAttributeIndex(int column) {
        return column - SPECIAL_ATTRIBUTES.length;
    }

    protected boolean isAttributeColumn(int column) {
        return getAttributeIndex(column) >= 0;
    }

    @Override
    public String getColumnName(int column) {
        if (!isAttributeColumn(column)) {
            return SPECIAL_ATTRIBUTES[column];
        }
        return context.getAttributes().get(getAttributeIndex(column)).toString();
    }

    @Override
    public void contextChanged(PartialContext context) {
        fireTableDataChanged();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return isAttributeColumn(columnIndex);
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isAttributeColumn(columnIndex)) {
            throw new IllegalArgumentException();
        }
        POD pod = context.getPODs().get(rowIndex);
        Attribute a = context.getAttributes().get(getAttributeIndex(columnIndex));
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
