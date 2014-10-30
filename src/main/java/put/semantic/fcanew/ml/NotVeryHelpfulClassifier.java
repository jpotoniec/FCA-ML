/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 *
 * @author smaug
 */
public class NotVeryHelpfulClassifier extends AbstractClassifier {

    @Override
    public void addExample(Map<String, Double> features, boolean accept) {
    }

    @Override
    public void updateModel() {
    }

    @Override
    public double classify(Map<String, Double> featuresMap) {
        return 0.5;
    }

    @Override
    public TableModel getExamplesTableModel() {
        return new DefaultTableModel();
    }

    @Override
    public void saveExamples(File f) throws IOException {
    }

    @Override
    public void loadExamples(File f) throws IOException {
    }

    @Override
    public int[] getClassDistribution() {
        return new int[0];
    }

    @Override
    public String getConfiguration() {
        return "";
    }

    @Override
    public void setConfiguration(String cfg) throws Exception {
    }

    @Override
    public String getConfigurationHelp() {
        return "This classifier can not be configured at all";
    }

    @Override
    public String toString() {
        return "Not very helpful classifier";
    }

}
