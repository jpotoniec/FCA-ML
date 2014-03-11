/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import darrylbu.renderer.VerticalTableHeaderCellRenderer;
import java.awt.EventQueue;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.FCA;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.SimpleSetOfAttributes;
import put.semantic.fcanew.ml.Classifier;
import put.semantic.fcanew.ml.JavaML;
import put.semantic.fcanew.ml.LinearRegression;
import put.semantic.fcanew.ml.WekaClassifier;

/**
 *
 * @author smaug
 */
public class MainWindow extends javax.swing.JFrame {

    private PartialContext context;

    private List<? extends Attribute> createAttributes() {
        final OWLDataFactory f = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        List<ClassAttribute> attributes = new ArrayList<>();
        Set<OWLClass> namedClasses = model.getRootOntology().getClassesInSignature(true);
        for (OWLClass clazz : namedClasses) {
            if (!model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
        }
        Set<OWLObjectProperty> objectProperties = model.getRootOntology().getObjectPropertiesInSignature(true);
        for (OWLObjectProperty property : objectProperties) {
            OWLClassExpression clazz;
            clazz = f.getOWLObjectSomeValuesFrom(property, f.getOWLThing());
            if (!model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
            clazz = f.getOWLObjectSomeValuesFrom(f.getOWLObjectInverseOf(property), f.getOWLThing());
            if (!model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
        }
        Collections.sort(attributes, new Comparator<ClassAttribute>() {

            @Override
            public int compare(ClassAttribute a, ClassAttribute b) {
                if (model.isEntailed(f.getOWLSubClassOfAxiom(a.getOntClass(), b.getOntClass()))) {
                    return 1;
                }
                if (model.isEntailed(f.getOWLSubClassOfAxiom(b.getOntClass(), a.getOntClass()))) {
                    return -1;
                }
                return a.getOntClass().compareTo(b.getOntClass());
            }
        });
        return attributes;
    }

    private class GuiExpert implements Expert {

        private final Decision[] dec = new Decision[1];
        private Map<String, Double> lastFeatures;
        private Implication currentImplication;
//        private Classifier classifier = new LinearRegression("follows from KB", "support", "support (premises)", "support (conclusions)");
        private Classifier classifier;

        public GuiExpert() {
            classifier = new WekaClassifier();
            classifier.setup("follows from KB", "support", "support (premises)", "support (conclusions)");
        }

        private void setEnabled(boolean enabled) {
            acceptButton.setEnabled(enabled);
            rejectButton.setEnabled(enabled);
        }

        @Override
        public Decision verify(Implication impl) {
            this.currentImplication = impl;
            ask(impl);
            while (true) {
                try {
                    synchronized (dec) {
                        dec.wait();
                    }
                    System.out.println("Decision: " + dec[0]);
                    return dec[0];
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private void accept() {
            setEnabled(false);
            synchronized (dec) {
                classifier.addExample(lastFeatures, true);
                classifier.updateModel();
                dec[0] = Decision.ACCEPT;
                dec.notify();
            }
        }

        private void reject() {
            setEnabled(false);
            synchronized (dec) {
                classifier.addExample(lastFeatures, false);
                classifier.updateModel();
                dec[0] = Decision.REJECT;
                dec.notify();
            }
        }

        private int getSize() {
            return model.getRootOntology().getIndividualsInSignature().size();
        }

        private int support(OWLClassExpression expr) {
            return model.getInstances(expr, false).getFlattened().size();
        }

        private void getFeatures(OWLClassExpression expr, String id, Map<String, Double> result) {
            if (!id.isEmpty()) {
                id = " (" + id + ")";
            }
            double support = support(expr);
            double size = getSize();
            result.put("support" + id, support / size);
            result.put("sat" + id, model.isSatisfiable(expr) ? 1.0 : 0);
        }

        private Map<String, Double> getFeatures(Implication impl) {
            OWLOntologyManager manager = model.getRootOntology().getOWLOntologyManager();
            OWLDataFactory factory = manager.getOWLDataFactory();
            Map<String, Double> result = new TreeMap<>();
            OWLSubClassOfAxiom subclassAxiom = impl.toAxiom(model);
            result.put("follows from KB", model.isEntailed(subclassAxiom) ? 1.0 : 0);
            if (!model.getRootOntology().containsAxiom(subclassAxiom, true)) {
                manager.addAxiom(model.getRootOntology(), subclassAxiom);
                model.flush();
                result.put("consistent", model.isConsistent() ? 1.0 : 0);
                manager.removeAxiom(model.getRootOntology(), subclassAxiom);
                model.flush();
            }
            OWLClassExpression pclass = impl.getPremises().getClass(model);
            getFeatures(pclass, "premises", result);
            OWLClassExpression cclass = impl.getConclusions().getClass(model);
            getFeatures(cclass, "conclusions", result);
            getFeatures(factory.getOWLObjectIntersectionOf(pclass, cclass), "", result);
            return result;
        }

        private void ask(final Implication impl) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Map<String, Double> features = getFeatures(impl);
                    features.put("classifier", classifier.classify(features));
                    lastFeatures = features;
                    DefaultTableModel model = new DefaultTableModel(new String[]{"feature", "value"}, 0);
                    for (Map.Entry<String, Double> f : features.entrySet()) {
                        model.addRow(new Object[]{f.getKey(), f.getValue()});
                    }
                    featuresTable.setModel(model);
                    String s = "";
                    s += "<br>Justification: <br><pre>" + classifier.getJustification() + "</pre>";
                    implicationText.setText("<html>" + impl.toString() + "<br>" + s + "</html>");
                    setEnabled(true);
                }
            });
        }
    }
    private GuiExpert guiExpert = new GuiExpert();
    private OWLReasoner model;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() throws OWLOntologyCreationException {
        initComponents();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        HashSet<OWLOntology> ontologies = new HashSet<>();
        ontologies.add(m.loadOntologyFromOntologyDocument(IRI.create(new File("University0_0.owl"))));
        ontologies.add(m.loadOntologyFromOntologyDocument(IRI.create(new File("univ-bench.owl"))));
        OWLOntology o = m.createOntology(IRI.generateDocumentIRI(), ontologies);
//        model = new Reasoner.ReasonerFactory().createReasoner(o);
        model = new PelletReasoner(o, BufferingMode.BUFFERING);
        System.err.println("Model read");
        context = new PartialContext(new SimpleSetOfAttributes(createAttributes()), model);
        context.updateContext();
        contextTable.setModel(new ContextDataModel(context));
        Enumeration<TableColumn> e = contextTable.getColumnModel().getColumns();
        while (e.hasMoreElements()) {
            e.nextElement().setHeaderRenderer(new VerticalTableHeaderCellRenderer());
        }
//        contextTable.setCellEditor(new DefaultCellEditor(new JComboBox(new Object[]{"+", "-", " "})));
        final FCA fca = new FCA();
        fca.setContext(context);
        fca.setExpert(guiExpert);
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                fca.reset();
                fca.run();
                return null;
            }

            @Override
            protected void done() {
                implicationText.setText("Bye-bye");
            }

        }.execute();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane2 = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        implicationText = new javax.swing.JLabel();
        acceptButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        featuresTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        contextTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane1.setDividerLocation(369);

        implicationText.setText("jLabel1");

        acceptButton.setText("Accept");
        acceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acceptButtonActionPerformed(evt);
            }
        });

        rejectButton.setText("Reject");
        rejectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rejectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(implicationText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(acceptButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rejectButton)
                        .addGap(0, 173, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(implicationText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(acceptButton)
                    .addComponent(rejectButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane1.setLeftComponent(jPanel1);

        featuresTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(featuresTable);

        jSplitPane1.setRightComponent(jScrollPane2);

        jSplitPane2.setTopComponent(jSplitPane1);

        contextTable.setAutoCreateRowSorter(true);
        contextTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(contextTable);

        jSplitPane2.setRightComponent(jScrollPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1099, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 12, Short.MAX_VALUE)
                .addComponent(jSplitPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void acceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acceptButtonActionPerformed
        guiExpert.accept();
    }//GEN-LAST:event_acceptButtonActionPerformed

    private void rejectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rejectButtonActionPerformed
        guiExpert.reject();
    }//GEN-LAST:event_rejectButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new MainWindow().setVisible(true);
                } catch (OWLOntologyCreationException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(0);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JTable contextTable;
    private javax.swing.JTable featuresTable;
    private javax.swing.JLabel implicationText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JButton rejectButton;
    // End of variables declaration//GEN-END:variables
}
