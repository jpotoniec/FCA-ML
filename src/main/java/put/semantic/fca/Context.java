/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.mindswap.pellet.KnowledgeBase;
import put.semantic.putapi.Individual;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class Context implements TableModel {

    private List<Attribute> attributes;
    private List<Individual> individuals;
    private Reasoner kb;

    public Context(List<Attribute> attributes, Reasoner kb) {
        this.attributes = attributes;
        this.kb = kb;
        update();
    }

    public synchronized void update() {
        individuals = kb.getIndividuals();
        fireListeners(new TableModelEvent(this));
    }

    private void fireListeners(TableModelEvent e) {
        for (TableModelListener l : listeners) {
            l.tableChanged(e);
        }
    }

    @Override
    public synchronized int getRowCount() {
        return individuals.size();
    }

    @Override
    public synchronized int getColumnCount() {
        return attributes.size() + 1;
    }

    @Override
    public synchronized String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "";
        }        
//        String result = "";
        String name = attributes.get(columnIndex - 1).toString();
        return name;
//        for (int i = 0; i < name.length(); ++i) {
//            result += name.charAt(i) + "<br />";
//        }
//        return "<html>" + result + "</html>";
    }

    @Override
    public synchronized Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public synchronized boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public synchronized Object getValueAt(int rowIndex, int columnIndex) {
        Individual i = individuals.get(rowIndex);
        if (columnIndex == 0) {
            return i.getLocalName();
        }
        Attribute a = attributes.get(columnIndex - 1);
        if (a.isSatisfiedBy(i)) {
            return "x";
        } else if (a.isOppositeSatisfiedBy(i)) {
            return "-";
        } else {
            return "?";
        }
    }

    @Override
    public synchronized void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private List<TableModelListener> listeners = new ArrayList<>();

    @Override
    public synchronized void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public synchronized void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }
}
