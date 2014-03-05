/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import darrylbu.renderer.VerticalTableHeaderCellRenderer;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.FCA;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.POD;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.SimpleSetOfAttributes;
import put.semantic.fcanew.SubsetOfAttributes;

/**
 *
 * @author smaug
 */
public class MainWindow extends javax.swing.JFrame {

    private PartialContext context;

    private List<Attribute> createAttributes() {
        List<Attribute> attributes = new ArrayList<>();
        Set<OWLClass> namedClasses = model.getRootOntology().getClassesInSignature(true);
        for (OWLClass clazz : namedClasses) {
            if (!model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
        }
        return attributes;
    }

    private class GuiExpert implements Expert {

        private final Decision[] dec = new Decision[1];
        private Implication currentImplication;

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
            dec[0] = Decision.ACCEPT;
            synchronized (dec) {
                dec.notify();
            }
        }

        private void reject() {
            dec[0] = Decision.REJECT;
            synchronized (dec) {
                dec.notify();
            }
        }

        private void ask(final Implication impl) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    implicationText.setText("<html>" + impl.toString() + "</html>");
                }
            });
        }
    }
    private GuiExpert guiExpert = new GuiExpert();
    private OWLReasoner model;

    private void initContext() {
        model.precomputeInferences(InferenceType.CLASS_ASSERTIONS, InferenceType.CLASS_HIERARCHY);
        Map<OWLNamedIndividual, SubsetOfAttributes> p = new HashMap<>();
        Map<OWLNamedIndividual, SubsetOfAttributes> n = new HashMap<>();
        Set<OWLNamedIndividual> individuals = model.getRootOntology().getIndividualsInSignature(true);
        for (OWLNamedIndividual i : individuals) {
            p.put(i, new SubsetOfAttributes(context.getAttributes()));
            n.put(i, new SubsetOfAttributes(context.getAttributes()));
        }
        for (int x = 0; x < context.getAttributes().size(); ++x) {
            System.err.printf("Attribute %d/%d\n", x + 1, context.getAttributes().size());
            ClassAttribute attr = (ClassAttribute) context.getAttributes().get(x);
            Set<OWLNamedIndividual> instances;
            instances = model.getInstances(attr.getOntClass(), false).getFlattened();
            for (OWLNamedIndividual i : instances) {
                SubsetOfAttributes attrs = p.get(i);
                if (attrs != null) {
                    attrs.add(x);
                }
            }
            instances = model.getInstances(attr.getComplement(), false).getFlattened();
            for (OWLNamedIndividual i : instances) {
                SubsetOfAttributes attrs = n.get(i);
                if (attrs != null) {
                    attrs.add(x);
                }
            }
        }
        for (OWLNamedIndividual i : p.keySet()) {
            POD pod = new POD(i, context.getAttributes(), model, p.get(i), n.get(i));
            context.addPOD(pod);
        }
    }

    /**
     * Creates new form MainWindow
     */
    public MainWindow() throws OWLOntologyCreationException {
        initComponents();
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        HashSet<OWLOntology> ontologies = new HashSet<>();
        ontologies.add(m.loadOntologyFromOntologyDocument(IRI.create("file:///home/smaug/studia/Asparagus/Main/data/University0_0.owl")));
        ontologies.add(m.loadOntologyFromOntologyDocument(IRI.create("file:///home/smaug/studia/Asparagus/Main/data/univ-bench.owl")));
        OWLOntology o = m.createOntology(IRI.generateDocumentIRI(), ontologies);
        model = new Reasoner.ReasonerFactory().createReasoner(o);
        System.err.println("Model read");
        context = new PartialContext(new SimpleSetOfAttributes(createAttributes()), model);
        initContext();
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

        jScrollPane1 = new javax.swing.JScrollPane();
        contextTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        implicationText = new javax.swing.JLabel();
        acceptButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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
                        .addGap(0, 615, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(implicationText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 108, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(acceptButton)
                    .addComponent(rejectButton))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 810, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 350, Short.MAX_VALUE)
                .addContainerGap())
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
    private javax.swing.JLabel implicationText;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton rejectButton;
    // End of variables declaration//GEN-END:variables
}
