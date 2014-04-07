/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 *
 * @author smaug
 */
public class LinearRegression extends AbstractClassifier {

    private class InstancesTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return examples.size();
        }

        @Override
        public int getColumnCount() {
            return attributes.size() + 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex < attributes.size()) {
                return examples.get(rowIndex)[columnIndex];
            } else {
                return decisions.get(rowIndex);
            }
        }

        @Override
        public String getColumnName(int column) {
            if (column < attributes.size()) {
                return attributes.get(column);
            } else {
                return "Decision";
            }
        }

    }

    private List<double[]> examples = new ArrayList<>();
    private List<Double> decisions = new ArrayList<>();
    private OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
    private InstancesTableModel tableModel = new InstancesTableModel();
    private int[] classDistribution = new int[2];

    @Override
    public void setup(List<String> attributes) {
        super.setup(attributes); //To change body of generated methods, choose Tools | Templates.
        tableModel.fireTableStructureChanged();
    }

    @Override
    public void addExample(Map<String, Double> features, boolean accept) {
        int decision = accept ? 1 : 0;
        examples.add(transform(features));
        decisions.add((double) decision);
        classDistribution[decision]++;
        tableModel.fireTableRowsInserted(examples.size() - 1, examples.size() - 1);
    }

    @Override
    public void updateModel() {
        if (examples.size() <= attributes.size()) {
            return;
        }
        double[] y = new double[decisions.size()];
        for (int i = 0; i < y.length; ++i) {
            y[i] = decisions.get(i);
        }
        regression.newSampleData(y, examples.toArray(new double[0][0]));
    }

    @Override
    public double classify(Map<String, Double> featuresMap) {
        justification = "";
        if (examples.size() <= attributes.size()) {
            return Double.NaN;
        }
        try {
            double[] features = transform(featuresMap);
            double[] params = regression.estimateRegressionParameters();
            assert features.length + 1 == params.length;
            double result = 0;
            for (int i = 0; i < features.length; ++i) {
                result += params[i] * features[i];
                justification += String.format("%f*%f + ", params[i], features[i]);
            }
            result += params[params.length - 1];
            justification += String.format("%f", params[params.length - 1]);
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Double.NaN;
        }
    }

    @Override
    public TableModel getExamplesTableModel() {
        return tableModel;
    }

    @Override
    public String toString() {
        return "Apache Commons Linear Regression";
    }

    @Override
    public void saveExamples(File f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void loadExamples(File f) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getClassDistribution() {
        return Arrays.copyOf(classDistribution, classDistribution.length);
    }

}
