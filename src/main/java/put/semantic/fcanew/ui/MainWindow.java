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

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import darrylbu.renderer.VerticalTableHeaderCellRenderer;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.FCA;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ProgressListener;
import put.semantic.fcanew.SimpleSetOfAttributes;
import put.semantic.fcanew.ml.Classifier;
import put.semantic.fcanew.ml.WekaClassifier;
import put.semantic.fcanew.ml.features.FeatureCalculator;
import put.semantic.fcanew.ml.features.impl.ConsistencyCalculator;
import put.semantic.fcanew.ml.features.impl.FollowingCalculators;
import put.semantic.fcanew.ml.features.impl.RuleCalculator;
import put.semantic.fcanew.ml.features.impl.SatCalculator;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

/**
 *
 * @author smaug
 */
public class MainWindow extends javax.swing.JFrame {

    private PartialContext context;

    private List<? extends Attribute> createAttributes() {
        DLSyntaxObjectRenderer r = new DLSyntaxObjectRenderer();
        final OWLDataFactory f = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory();
        List<ClassAttribute> attributes = new ArrayList<>();
        NodeSet<OWLClass> namedClasses = model.getSubClasses(model.getTopClassNode().getRepresentativeElement(), false);
        for (Node<OWLClass> node : namedClasses) {
            OWLClassExpression clazz = node.getRepresentativeElement();
            if (!model.getInstances(clazz, false).isEmpty()) {
                if (usePositiveNamedClasses.isSelected()) {
                    attributes.add(new ClassAttribute(clazz, model));
                }
                clazz = clazz.getComplementNNF();
                if (useNegativeNamedClasses.isSelected()) {
                    attributes.add(new ClassAttribute(clazz, model));
                }
            }
        }
        Set<OWLObjectProperty> objectProperties = model.getRootOntology().getObjectPropertiesInSignature(true);
        for (OWLObjectProperty property : objectProperties) {
            OWLClassExpression clazz;
            clazz = f.getOWLObjectSomeValuesFrom(property, f.getOWLThing());
            if (usePositiveDomains.isSelected() && !model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
            clazz = clazz.getComplementNNF();
            if (useNegativeDomains.isSelected() && !model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
            clazz = f.getOWLObjectSomeValuesFrom(f.getOWLObjectInverseOf(property), f.getOWLThing());
            if (usePositiveRanges.isSelected() && !model.getInstances(clazz, false).isEmpty()) {
                attributes.add(new ClassAttribute(clazz, model));
            }
            clazz = clazz.getComplementNNF();
            if (useNegativeRanges.isSelected() && !model.getInstances(clazz, false).isEmpty()) {
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
    private Font normalFont, boldFont;

    private void highlightButton(double p) {
        acceptButton.setFont(normalFont);
        rejectButton.setFont(normalFont);
        if (p > 0.6) {
            acceptButton.setFont(boldFont);
        } else if (p < 0.4) {
            rejectButton.setFont(boldFont);
        }
    }

    private class GuiExpert implements Expert {

        private final List<? extends FeatureCalculator> calculators = Arrays.asList(
                new RuleCalculator(true),
                new RuleCalculator(false),
                new FollowingCalculators(),
                new SatCalculator(),
                new ConsistencyCalculator());
        private final Object lock = new Object();
        private Decision dec;
        private Map<String, Double> lastFeatures;
        private Implication currentImplication;
//        private Classifier classifier = new LinearRegression("follows from KB", "support", "support (premises)", "support (conclusions)");
        private Classifier classifier;

        public GuiExpert() {
            classifier = new WekaClassifier();
            List<String> features = new ArrayList<>();
            for (FeatureCalculator calc : calculators) {
                features.addAll(calc.getNames());
            }
            classifier.setup(features.toArray(new String[0]));
            learningExamplesTable.setModel(classifier.getExamplesTableModel());
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
                    synchronized (lock) {
                        lock.wait();
                        System.out.println("Decision: " + dec);
                        return dec;
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        private void accept() {
            setEnabled(false);
            synchronized (lock) {
                classifier.addExample(lastFeatures, true);
                classifier.updateModel();
                dec = Decision.ACCEPT;
                lock.notify();
            }
        }

        private void reject() {
            setEnabled(false);
            synchronized (lock) {
                classifier.addExample(lastFeatures, false);
                classifier.updateModel();
                dec = Decision.REJECT;
                lock.notify();
            }
        }

        private Map<String, Double> getFeatures(Implication impl) {
            Map<String, Double> result = new TreeMap<>();
            for (FeatureCalculator calc : calculators) {
                System.err.println(calc.getClass());
                List<String> names = calc.getNames();
                List<? extends FeatureValue> values = calc.compute(impl, model, context);
                for (int i = 0; i < names.size(); ++i) {
                    result.put(names.get(i), ((NumericFeatureValue) values.get(i)).getValue());
                }
            }
            return result;
        }

        private void ask(final Implication impl) {
            final Map<String, Double> features = getFeatures(impl);
            double clResult = classifier.classify(features);
            highlightButton(clResult);
            features.put("classifier", clResult);
            synchronized (lock) {
                lastFeatures = features;
            }
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    currentImplication = impl;
                    ((ContextDataModel) contextTable.getModel()).setCurrentImplication(impl);
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

        public Implication getCurrentImplication() {
            return currentImplication;
        }

    }
    private GuiExpert guiExpert;
    private OWLReasoner model;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        normalFont = acceptButton.getFont();
        boldFont = normalFont.deriveFont(Font.BOLD);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        setupTab = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        usePositiveNamedClasses = new javax.swing.JCheckBox();
        useNegativeNamedClasses = new javax.swing.JCheckBox();
        usePositiveDomains = new javax.swing.JCheckBox();
        useNegativeDomains = new javax.swing.JCheckBox();
        usePositiveRanges = new javax.swing.JCheckBox();
        useNegativeRanges = new javax.swing.JCheckBox();
        startButton = new javax.swing.JButton();
        fcaTab = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        implicationText = new javax.swing.JLabel();
        acceptButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        updateProgressBar = new javax.swing.JProgressBar();
        addNewButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        featuresTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        contextTable = new javax.swing.JTable();
        classifierTab = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        learningExamplesTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Named classes");

        jLabel2.setText("Domains");

        jLabel3.setText("Ranges");

        usePositiveNamedClasses.setSelected(true);
        usePositiveNamedClasses.setText("Positive");

        useNegativeNamedClasses.setText("Negative");

        usePositiveDomains.setSelected(true);
        usePositiveDomains.setText("Positive");

        useNegativeDomains.setText("Negative");

        usePositiveRanges.setSelected(true);
        usePositiveRanges.setText("Positive");

        useNegativeRanges.setText("Negative");

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout setupTabLayout = new javax.swing.GroupLayout(setupTab);
        setupTab.setLayout(setupTabLayout);
        setupTabLayout.setHorizontalGroup(
            setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3))
                .addGap(18, 18, 18)
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(startButton)
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(usePositiveRanges)
                        .addGap(18, 18, 18)
                        .addComponent(useNegativeRanges))
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(usePositiveDomains)
                        .addGap(18, 18, 18)
                        .addComponent(useNegativeDomains))
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(usePositiveNamedClasses)
                        .addGap(18, 18, 18)
                        .addComponent(useNegativeNamedClasses)))
                .addContainerGap(771, Short.MAX_VALUE))
        );
        setupTabLayout.setVerticalGroup(
            setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(usePositiveNamedClasses)
                    .addComponent(useNegativeNamedClasses))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(usePositiveDomains)
                    .addComponent(useNegativeDomains))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(usePositiveRanges)
                    .addComponent(useNegativeRanges))
                .addGap(18, 18, 18)
                .addComponent(startButton)
                .addContainerGap(698, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Setup", setupTab);

        fcaTab.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        jSplitPane1.setDividerLocation(730);

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

        addNewButton.setText("Add new");
        addNewButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(implicationText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(acceptButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rejectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(addNewButton))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(implicationText)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(acceptButton)
                        .addComponent(rejectButton))
                    .addComponent(updateProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewButton))
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

        fcaTab.setTopComponent(jSplitPane1);

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

        fcaTab.setRightComponent(jScrollPane1);

        jTabbedPane1.addTab("FCA", fcaTab);

        learningExamplesTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(learningExamplesTable);

        javax.swing.GroupLayout classifierTabLayout = new javax.swing.GroupLayout(classifierTab);
        classifierTab.setLayout(classifierTabLayout);
        classifierTabLayout.setHorizontalGroup(
            classifierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(classifierTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1070, Short.MAX_VALUE)
                .addContainerGap())
        );
        classifierTabLayout.setVerticalGroup(
            classifierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(classifierTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 802, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Classifier", classifierTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 853, Short.MAX_VALUE)
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

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        Set<OWLOntology> ontologies = new HashSet<>();
        try {
            ontologies = m.getImportsClosure(m.loadOntology(IRI.create(new File("University0_0.owl"))));
            OWLOntology o = m.createOntology(IRI.generateDocumentIRI(), ontologies);
            //        model = new Reasoner.ReasonerFactory().createReasoner(o);
            model = new PelletReasoner(o, BufferingMode.BUFFERING);
//        model = new JFactFactory().createReasoner(o);
        } catch (OWLOntologyCreationException ex) {
            throw new RuntimeException(ex);
        }
        System.err.println("Model read");
        context = new PartialContext(new SimpleSetOfAttributes(createAttributes()), model);
        context.addProgressListener(new ProgressListener() {
            @Override
            public void reset(int max) {
                updateProgressBar.setMaximum(max);
            }

            @Override
            public void update(int status) {
                updateProgressBar.setValue(status);
            }
        });
        context.updateContext();
        contextTable.setModel(new ContextDataModel(context));
        contextTable.setDefaultRenderer(Object.class, new PODCellRenderer(model));
        Enumeration<TableColumn> e = contextTable.getColumnModel().getColumns();
        JComboBox comboBox = new JComboBox(new Object[]{"+", "-", " "});
        while (e.hasMoreElements()) {
            TableColumn col = e.nextElement();
            col.setHeaderRenderer(new VerticalTableHeaderCellRenderer());
            col.setCellEditor(new DefaultCellEditor(comboBox));
        }
        guiExpert = new GuiExpert();
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
        jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfComponent(setupTab), false);
        jTabbedPane1.setEnabledAt(jTabbedPane1.indexOfComponent(fcaTab), true);
        jTabbedPane1.setSelectedComponent(fcaTab);
    }//GEN-LAST:event_startButtonActionPerformed

    private void addNewButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewButtonActionPerformed
        String uri = JOptionPane.showInputDialog(this, "Enter individual URI");
        if (uri != null) {
            OWLOntologyManager m = model.getRootOntology().getOWLOntologyManager();
            OWLDataFactory f = m.getOWLDataFactory();
            OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(uri));
            if (!guiExpert.getCurrentImplication().getPremises().isEmpty()) {
                for (Attribute a : guiExpert.getCurrentImplication().getPremises()) {
                    m.addAxiom(model.getRootOntology(), f.getOWLClassAssertionAxiom(((ClassAttribute) a).getOntClass(), ind));
                }
            } else {
                m.addAxiom(model.getRootOntology(), f.getOWLClassAssertionAxiom(model.getTopClassNode().getRepresentativeElement(), ind));
            }
            context.updateContext();
        }
    }//GEN-LAST:event_addNewButtonActionPerformed

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
                new MainWindow().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton acceptButton;
    private javax.swing.JButton addNewButton;
    private javax.swing.JPanel classifierTab;
    private javax.swing.JTable contextTable;
    private javax.swing.JSplitPane fcaTab;
    private javax.swing.JTable featuresTable;
    private javax.swing.JLabel implicationText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable learningExamplesTable;
    private javax.swing.JButton rejectButton;
    private javax.swing.JPanel setupTab;
    private javax.swing.JButton startButton;
    private javax.swing.JProgressBar updateProgressBar;
    private javax.swing.JCheckBox useNegativeDomains;
    private javax.swing.JCheckBox useNegativeNamedClasses;
    private javax.swing.JCheckBox useNegativeRanges;
    private javax.swing.JCheckBox usePositiveDomains;
    private javax.swing.JCheckBox usePositiveNamedClasses;
    private javax.swing.JCheckBox usePositiveRanges;
    // End of variables declaration//GEN-END:variables
}
