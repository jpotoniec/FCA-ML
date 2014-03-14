/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 *
 * @author smaug
 */
public class PODCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String toolTip = "";
        Color c = Color.BLACK;
        if (value instanceof OWLNamedIndividual) {
            IRI iri = ((OWLNamedIndividual) value).getIRI();
            value = iri.getFragment();
            toolTip = iri.toString();
            ContextDataModel model = (ContextDataModel) table.getModel();
            ContextDataModel.PODState state = model.getState((OWLNamedIndividual) value);
            switch (state) {
                case IN_PREMISE:
                    System.err.println(iri);
                    c = Color.RED;
                    break;
                case IN_CONCLUSION:
                    c = Color.GREEN;
                    break;
                case IN_BOTH:
                    c = Color.ORANGE;
                    break;
            }
        }
        JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        result.setToolTipText(toolTip);
//        System.err.printf("(%d,%d)->%s\n", row, column, c);        
        result.setForeground(c);
        return result;
    }

}
