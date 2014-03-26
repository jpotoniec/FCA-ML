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
import java.util.regex.Pattern;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableRowSorter;
import org.apache.commons.lang.StringUtils;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.FCA;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ProgressListener;
import put.semantic.fcanew.SimpleSetOfAttributes;
import put.semantic.fcanew.ml.Classifier;
import put.semantic.fcanew.ml.ConfusionMatrix;
import put.semantic.fcanew.ml.features.FeatureCalculator;
import put.semantic.fcanew.ml.features.impl.ConsistencyCalculator;
import put.semantic.fcanew.ml.features.impl.FollowingCalculators;
import put.semantic.fcanew.ml.features.impl.RuleCalculator;
import put.semantic.fcanew.ml.features.impl.SatCalculator;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;
import put.semantic.fcanew.preferences.PreferencesProvider;
import uk.ac.manchester.cs.jfact.JFactFactory;
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

    private void highlightButton(Boolean shouldAccept) {
        acceptButton.setFont(normalFont);
        rejectButton.setFont(normalFont);
        if (shouldAccept != null) {
            if (shouldAccept) {
                acceptButton.setFont(boldFont);
            } else {
                rejectButton.setFont(boldFont);
            }
        }
    }

    private class GuiExpert implements Expert {

        private final List<? extends FeatureCalculator> calculators;
        private final Object lock = new Object();
        private Decision dec;
        private Map<String, Double> lastFeatures;
        private Implication currentImplication;
        private final Classifier classifier;
        private Boolean shouldAccept;

        public GuiExpert(Classifier cl) {
            this.classifier = cl;
            this.calculators = availableCalculatorsModel.getChecked();
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
            ((ConfusionMatrix) confusionMatrix.getModel()).add(shouldAccept, true);
            synchronized (lock) {
                classifier.addExample(lastFeatures, true);
                classifier.updateModel();
                dec = Decision.ACCEPT;
                lock.notify();
            }
        }

        private void reject() {
            setEnabled(false);
            ((ConfusionMatrix) confusionMatrix.getModel()).add(shouldAccept, false);
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
            if (clResult > 0.6) {
                shouldAccept = true;
            } else if (clResult < 0.4) {
                shouldAccept = false;
            } else {
                shouldAccept = null;
            }
            highlightButton(shouldAccept);
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
            if (features.get("follows from KB") == 1) {
                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        acceptButton.doClick();
                    }
                });
            }
        }

        public Implication getCurrentImplication() {
            return currentImplication;
        }

    }
    private GuiExpert guiExpert;
    private OWLReasoner model;
    private List<? extends Attribute> attributes;
    private final CheckBoxListModel<FeatureCalculator> availableCalculatorsModel;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        availableCalculatorsModel = new CheckBoxListModel<FeatureCalculator>(Arrays.asList(
                new RuleCalculator(),
                new FollowingCalculators(),
                new SatCalculator(),
                new ConsistencyCalculator()), PreferencesProvider.getInstance().getCalculators());
        availableCalculatorsModel.addListDataListener(new ListDataListener() {

            private void save(ListDataEvent e) {
                boolean[] checked = ((CheckBoxListModel<FeatureCalculator>) e.getSource()).getCheckedAsArray();
                PreferencesProvider.getInstance().setCalculators(checked);
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                save(e);
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                save(e);
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                save(e);
            }
        });
        initComponents();
        normalFont = acceptButton.getFont();
        boldFont = normalFont.deriveFont(Font.BOLD);
        initFiles();
        ((CheckBoxList<FeatureCalculator>) availableCalculators).setTransformer(new CheckBoxList.Transformer<FeatureCalculator>() {

            @Override
            public String transform(FeatureCalculator calc) {
                return calc.getClass().getSimpleName() + ": " + StringUtils.join(calc.getNames(), ", ");
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        reasoners = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        setupTab = new javax.swing.JPanel();
        start = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        files = new javax.swing.JList();
        addFile = new javax.swing.JButton();
        removeFile = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        usePellet = new javax.swing.JRadioButton();
        useJFact = new javax.swing.JRadioButton();
        useHermit = new javax.swing.JRadioButton();
        jPanel4 = new javax.swing.JPanel();
        classifierToUse = new javax.swing.JComboBox();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        availableCalculators = new CheckBoxList<FeatureCalculator>();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        usePositiveNamedClasses = new javax.swing.JCheckBox();
        useNegativeNamedClasses = new javax.swing.JCheckBox();
        usePositiveDomains = new javax.swing.JCheckBox();
        useNegativeDomains = new javax.swing.JCheckBox();
        usePositiveRanges = new javax.swing.JCheckBox();
        useNegativeRanges = new javax.swing.JCheckBox();
        generateAttributes = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        forcedAttributes = new CheckBoxList<Attribute>();
        fcaTab = new javax.swing.JSplitPane();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        implicationText = new javax.swing.JLabel();
        acceptButton = new javax.swing.JButton();
        rejectButton = new javax.swing.JButton();
        updateProgressBar = new javax.swing.JProgressBar();
        addNewButton = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        filterText = new javax.swing.JTextField();
        resetFilter = new javax.swing.JButton();
        applyFilter = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        featuresTable = new javax.swing.JTable();
        jScrollPane1 = new javax.swing.JScrollPane();
        contextTable = new javax.swing.JTable();
        classifierTab = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        learningExamplesTable = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        confusionMatrix = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        start.setFont(start.getFont().deriveFont(start.getFont().getSize()+10f));
        start.setForeground(new java.awt.Color(255, 0, 0));
        start.setText("Start");
        start.setEnabled(false);
        start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Files"));

        files.setModel(new FilesListModel());
        jScrollPane5.setViewportView(files);

        addFile.setText("Add");
        addFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileActionPerformed(evt);
            }
        });

        removeFile.setText("Remove");
        removeFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 724, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(addFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(removeFile)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addFile)
                    .addComponent(removeFile))
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Reasoner"));

        reasoners.add(usePellet);
        usePellet.setSelected(true);
        usePellet.setText("Pellet");

        reasoners.add(useJFact);
        useJFact.setText("JFact (Fact++)");

        reasoners.add(useHermit);
        useHermit.setText("HermiT");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usePellet)
                    .addComponent(useJFact)
                    .addComponent(useHermit))
                .addContainerGap(10, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(usePellet)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useJFact)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(useHermit)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Classifier"));

        classifierToUse.setModel(new put.semantic.fcanew.ui.AvailableClassifiersModel());

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(classifierToUse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(classifierToUse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Feature calculators"));

        availableCalculators.setModel(availableCalculatorsModel);
        jScrollPane7.setViewportView(availableCalculators);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Attributes"));

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

        generateAttributes.setText("Generate attributes");
        generateAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateAttributesActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 386, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel1)
                        .addComponent(jLabel2)
                        .addComponent(jLabel3))
                    .addGap(18, 18, 18)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(generateAttributes)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(usePositiveRanges)
                            .addGap(18, 18, 18)
                            .addComponent(useNegativeRanges))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(usePositiveDomains)
                            .addGap(18, 18, 18)
                            .addComponent(useNegativeDomains))
                        .addGroup(jPanel6Layout.createSequentialGroup()
                            .addComponent(usePositiveNamedClasses)
                            .addGap(18, 18, 18)
                            .addComponent(useNegativeNamedClasses)))
                    .addContainerGap(63, Short.MAX_VALUE)))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
            .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel6Layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel1)
                        .addComponent(usePositiveNamedClasses)
                        .addComponent(useNegativeNamedClasses))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(usePositiveDomains)
                        .addComponent(useNegativeDomains))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel3)
                        .addComponent(usePositiveRanges)
                        .addComponent(useNegativeRanges))
                    .addGap(18, 18, 18)
                    .addComponent(generateAttributes)
                    .addContainerGap(13, Short.MAX_VALUE)))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Forced attributes"));

        forcedAttributes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(forcedAttributes);

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 362, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout setupTabLayout = new javax.swing.GroupLayout(setupTab);
        setupTab.setLayout(setupTabLayout);
        setupTabLayout.setHorizontalGroup(
            setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(start))
                .addGap(18, 18, 18)
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        setupTabLayout.setVerticalGroup(
            setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setupTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(setupTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(setupTabLayout.createSequentialGroup()
                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(start)))
                .addContainerGap(330, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Setup", setupTab);

        fcaTab.setDividerLocation(150);
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

        jLabel4.setText("Filter:");

        filterText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFilterActionPerformed(evt);
            }
        });

        resetFilter.setText("Reset");
        resetFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetFilterActionPerformed(evt);
            }
        });

        applyFilter.setText("Apply");
        applyFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFilterActionPerformed(evt);
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
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(filterText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(applyFilter)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(resetFilter))
                    .addComponent(implicationText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(acceptButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rejectButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(updateProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(addNewButton)))
                .addContainerGap())
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(filterText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetFilter)
                    .addComponent(applyFilter))
                .addContainerGap())
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
        contextTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
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

        confusionMatrix.setModel(new ConfusionMatrix());
        jScrollPane6.setViewportView(confusionMatrix);

        javax.swing.GroupLayout classifierTabLayout = new javax.swing.GroupLayout(classifierTab);
        classifierTab.setLayout(classifierTabLayout);
        classifierTabLayout.setHorizontalGroup(
            classifierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(classifierTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(classifierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 1172, Short.MAX_VALUE)
                    .addGroup(classifierTabLayout.createSequentialGroup()
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        classifierTabLayout.setVerticalGroup(
            classifierTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, classifierTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 854, Short.MAX_VALUE)
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
                .addComponent(jTabbedPane1)
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

    private void generateAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateAttributesActionPerformed
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        Set<OWLOntology> ontologies = new HashSet<>();
        try {
            for (File f : ((FilesListModel) files.getModel()).getFiles()) {
                ontologies.addAll(m.getImportsClosure(m.loadOntology(IRI.create(f))));
            }
            OWLOntology o = m.createOntology(IRI.generateDocumentIRI(), ontologies);
            if (useHermit.isSelected()) {
                model = new Reasoner.ReasonerFactory().createReasoner(o);
            } else if (usePellet.isSelected()) {
                model = new PelletReasoner(o, BufferingMode.BUFFERING);
            } else if (useJFact.isSelected()) {
                model = new JFactFactory().createReasoner(o);
            } else {
                throw new RuntimeException("Impoosible, no reasoner is selected");
            }
        } catch (OWLOntologyCreationException ex) {
            throw new RuntimeException(ex);
        }
        System.err.println("Model read");
        attributes = createAttributes();
        start.setEnabled(attributes != null);
        forcedAttributes.setModel(new CheckBoxListModel(attributes));
    }//GEN-LAST:event_generateAttributesActionPerformed

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

    private List<String> getPossibleLabels(OWLNamedIndividual ind) {
        OWLAnnotationProperty rdfsLabel = model.getRootOntology().getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());
        List<String> result = new ArrayList<>();
        for (OWLAnnotation ann : ind.getAnnotations(model.getRootOntology(), rdfsLabel)) {
            if (ann.getValue() instanceof OWLLiteral) {
                OWLLiteral literal = (OWLLiteral) ann.getValue();
                if (!literal.getLiteral().isEmpty()) {
                    result.add(literal.getLiteral());
                }
            }
        }
        IRI iri = ind.getIRI();
        result.add(iri.toString());
        return result;
    }

    private void applyFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFilterActionPerformed
        try {
            final Pattern p = Pattern.compile(filterText.getText(), Pattern.CASE_INSENSITIVE);
            RowFilter<ContextDataModel, Object> rf = new RowFilter<ContextDataModel, Object>() {

                @Override
                public boolean include(RowFilter.Entry<? extends ContextDataModel, ? extends Object> entry) {
                    Object id = entry.getValue(0);
                    if (!(id instanceof OWLNamedIndividual)) {
                        return true;
                    }
                    for (String s : getPossibleLabels((OWLNamedIndividual) id)) {
                        if (p.matcher(s).find()) {
                            return true;
                        }
                    }
                    return false;
                }
            };
            ((TableRowSorter) contextTable.getRowSorter()).setRowFilter(rf);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
    }//GEN-LAST:event_applyFilterActionPerformed

    private void resetFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetFilterActionPerformed
        ((TableRowSorter) contextTable.getRowSorter()).setRowFilter(null);
    }//GEN-LAST:event_resetFilterActionPerformed

    private void startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startActionPerformed
        final List<Attribute> forced = ((CheckBoxListModel) forcedAttributes.getModel()).getChecked();
        Collections.sort(attributes, new Comparator<Attribute>() {

            @Override
            public int compare(Attribute o1, Attribute o2) {
                boolean f1 = forced.contains(o1);
                boolean f2 = forced.contains(o2);
                if (f1 != f2) {
                    return f1 ? -1 : 1;
                }
                return ((ClassAttribute) o1).getOntClass().compareTo(((ClassAttribute) o2).getOntClass());
            }
        });
        context = new PartialContext(new SimpleSetOfAttributes(attributes), model);
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
        contextTable.setRowSorter(new TableRowSorter<>());
        contextTable.setModel(new ContextDataModel(context));
        contextTable.setDefaultRenderer(Object.class, new PODCellRenderer(model));
        Enumeration<TableColumn> e = contextTable.getColumnModel().getColumns();
        JComboBox comboBox = new JComboBox(new Object[]{"+", "-", " "});
        while (e.hasMoreElements()) {
            TableColumn col = e.nextElement();
            col.setHeaderRenderer(new VerticalTableHeaderCellRenderer());
            col.setCellEditor(new DefaultCellEditor(comboBox));
            if (col.getModelIndex() >= 1) {
                col.setPreferredWidth(20);
            }
        }
        guiExpert = new GuiExpert((Classifier) classifierToUse.getSelectedItem());
        final FCA fca = new FCA();
        fca.setContext(context);
        fca.setExpert(guiExpert);
        new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                fca.reset(forced.size());
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
    }//GEN-LAST:event_startActionPerformed

    private JFileChooser fileChooser = null;

    private void initFiles() {
        fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "RDF/OWL", "rdf", "owl", "ttl"));
        fileChooser.setMultiSelectionEnabled(true);
    }

    private void addFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFileActionPerformed
        assert fileChooser != null;
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            ((FilesListModel) files.getModel()).add(fileChooser.getSelectedFiles());
        }
    }//GEN-LAST:event_addFileActionPerformed

    private void removeFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFileActionPerformed
        for (int i : files.getSelectedIndices()) {
            ((FilesListModel) files.getModel()).remove(i);
        }
    }//GEN-LAST:event_removeFileActionPerformed

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
    private javax.swing.JButton addFile;
    private javax.swing.JButton addNewButton;
    private javax.swing.JButton applyFilter;
    private javax.swing.JList availableCalculators;
    private javax.swing.JPanel classifierTab;
    private javax.swing.JComboBox classifierToUse;
    private javax.swing.JTable confusionMatrix;
    private javax.swing.JTable contextTable;
    private javax.swing.JSplitPane fcaTab;
    private javax.swing.JTable featuresTable;
    private javax.swing.JList files;
    private javax.swing.JTextField filterText;
    private javax.swing.JList forcedAttributes;
    private javax.swing.JButton generateAttributes;
    private javax.swing.JLabel implicationText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable learningExamplesTable;
    private javax.swing.ButtonGroup reasoners;
    private javax.swing.JButton rejectButton;
    private javax.swing.JButton removeFile;
    private javax.swing.JButton resetFilter;
    private javax.swing.JPanel setupTab;
    private javax.swing.JButton start;
    private javax.swing.JProgressBar updateProgressBar;
    private javax.swing.JRadioButton useHermit;
    private javax.swing.JRadioButton useJFact;
    private javax.swing.JCheckBox useNegativeDomains;
    private javax.swing.JCheckBox useNegativeNamedClasses;
    private javax.swing.JCheckBox useNegativeRanges;
    private javax.swing.JRadioButton usePellet;
    private javax.swing.JCheckBox usePositiveDomains;
    private javax.swing.JCheckBox usePositiveNamedClasses;
    private javax.swing.JCheckBox usePositiveRanges;
    // End of variables declaration//GEN-END:variables
}
