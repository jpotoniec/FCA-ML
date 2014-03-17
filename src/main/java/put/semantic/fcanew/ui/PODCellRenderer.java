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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 *
 * @author smaug
 */
public class PODCellRenderer extends DefaultTableCellRenderer {

    private OWLReasoner model;
    private OWLAnnotationProperty rdfsLabel;

    public PODCellRenderer(OWLReasoner model) {
        this.model = model;
        this.rdfsLabel = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        String toolTip = "";
        Color c = Color.BLACK;
        if (value instanceof OWLNamedIndividual) {
            OWLNamedIndividual ind = (OWLNamedIndividual) value;
            String label = "";
            for (OWLAnnotation ann : ind.getAnnotations(model.getRootOntology(), rdfsLabel)) {
                if (ann.getValue() instanceof OWLLiteral) {
                    OWLLiteral literal = (OWLLiteral) ann.getValue();
                    if (!literal.getLiteral().isEmpty()) {
                        label = literal.getLiteral();
                    }
                }
            }
            IRI iri = ind.getIRI();
            if (isEmpty(label)) {
                label = iri.getFragment();
            }
            if (isEmpty(label)) {
                label = iri.toString();
            }
            value = label;
            toolTip = iri.toString();
            ContextDataModel model = (ContextDataModel) table.getModel();
//            ContextDataModel.PODState state = model.getState((OWLNamedIndividual) value);
//            switch (state) {
//                case IN_PREMISE:
//                    System.err.println(iri);
//                    c = Color.RED;
//                    break;
//                case IN_CONCLUSION:
//                    c = Color.GREEN;
//                    break;
//                case IN_BOTH:
//                    c = Color.ORANGE;
//                    break;
//            }
        }
        JLabel result = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        result.setToolTipText(toolTip);
//        System.err.printf("(%d,%d)->%s\n", row, column, c);
        result.setForeground(c);
        return result;
    }

}
