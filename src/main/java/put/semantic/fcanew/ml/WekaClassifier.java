/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import put.semantic.fcanew.preferences.PreferencesProvider;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.Utils;
import weka.core.converters.ArffLoader;
import weka.core.converters.ArffSaver;

public class WekaClassifier extends put.semantic.fcanew.ml.AbstractClassifier {

    private class InstancesTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return instances.numInstances();
        }

        @Override
        public int getColumnCount() {
            return attributes.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Instance i = instances.instance(rowIndex);
            return i.toString(columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            return instances.attribute(column).name();
        }

    }

    protected weka.classifiers.Classifier classifier;
    protected Instances instances;
    private InstancesTableModel tableModel = new InstancesTableModel();
    private static final FastVector classes;

    static {
        classes = new FastVector(2);
        classes.addElement("reject");
        classes.addElement("accept");
    }

    public WekaClassifier(Classifier classifier) {
        this.classifier = classifier;
        try {
            setConfiguration(PreferencesProvider.getInstance().getClassifierConfiguration(classifier.getClass().getName()));
        } catch (Exception ex) {
            //no stored configuration, using default
        }
    }

    @Override
    public void setup(List<String> names) {
        ArrayList<String> newNames = new ArrayList<>(names);
        newNames.add("__class");
        super.setup(newNames);
        FastVector attrs = new FastVector();
        for (String name : names) {
            Attribute attr = new Attribute(name);
            attrs.addElement(attr);
        }
        Attribute classAttr = new Attribute("__class", classes);
        attrs.addElement(classAttr);
        instances = new Instances("data", attrs, 0);
        instances.setClass(classAttr);
        tableModel.fireTableStructureChanged();
    }

    protected Instance makeInstance(Map<String, Double> features) {
        Instance result = new Instance(1, transform(features));
        result.setDataset(instances);
        return result;
    }

    @Override
    public void addExample(Map<String, Double> features, boolean accept) {
        Instance i = makeInstance(features);
        i.setClassValue(classes.elementAt(accept ? 1 : 0).toString());
        instances.add(i);
        tableModel.fireTableRowsInserted(instances.numInstances() - 1, instances.numInstances() - 1);
    }

    @Override
    public void updateModel() {
        justification = "";
        try {
            classifier.buildClassifier(instances);
            justification = classifier.toString();
        } catch (Exception ex) {
            Logger.getLogger(WekaClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public double classify(Map<String, Double> featuresMap) {
        try {
            Instance i = makeInstance(featuresMap);
            return classifier.distributionForInstance(i)[1];
        } catch (Exception ex) {
            Logger.getLogger(WekaClassifier.class.getName()).log(Level.SEVERE, null, ex);
            return Double.NaN;
        }
    }

    @Override
    public TableModel getExamplesTableModel() {
        return tableModel;
    }

    @Override
    public String toString() {
        return "Weka: " + classifier.getClass().getSimpleName();
    }

    public static Instance convert(Instance input, Instances src, Instances dst) {
        Instance result = new Instance(dst.numAttributes());
        result.setDataset(dst);
        for (int i = 0; i < dst.numAttributes(); ++i) {
            Attribute srcAttr = src.attribute(dst.attribute(i).name());
            if (srcAttr.isNumeric()) {
                double val = input.value(srcAttr);
                result.setValue(i, val);
            } else {
                String val = input.stringValue(srcAttr);
                result.setValue(i, val);
            }
        }
        return result;
    }

    @Override
    public void saveExamples(File f) throws IOException {
        ArffSaver s = new ArffSaver();
        s.setFile(f);
        s.setInstances(instances);
        s.writeBatch();
    }

    @Override
    public void loadExamples(File f) throws IOException {
        ArffLoader l = new ArffLoader();
        l.setFile(f);
        Instances structure = l.getStructure();
        Instance i;
        while ((i = l.getNextInstance(structure)) != null) {
            if (!instances.checkInstance(i)) {
                i = convert(i, structure, instances);
            } else {
                i.setDataset(instances);
            }
            if (instances.checkInstance(i)) {
                instances.add(i);
            } else {
                System.err.println("Ignoring incompatible instance");
            }
        }
        updateModel();
        tableModel.fireTableDataChanged();
    }

    @Override
    public int[] getClassDistribution() {
        return instances.attributeStats(instances.classIndex()).nominalCounts;
    }

    @Override
    public String getConfiguration() {
        return Utils.joinOptions(classifier.getOptions());
    }

    @Override
    public void setConfiguration(String cfg) throws Exception {
        classifier.setOptions(Utils.splitOptions(cfg));
        PreferencesProvider.getInstance().setClassifierConfiguration(classifier.getClass().getName(), getConfiguration());
    }

    @Override
    public String getConfigurationHelp() {
        String result = "";
        result += "<html><ul>";
        Enumeration e = classifier.listOptions();
        while (e.hasMoreElements()) {
            Option o = (Option) e.nextElement();
            result += String.format("<li>%s (%d arguments)<br><pre>%s</pre></li>", o.synopsis(), o.numArguments(), o.description().replace("\n", "<br>"));
        }
        result += "</ul></html>";
        return result;
    }

}
