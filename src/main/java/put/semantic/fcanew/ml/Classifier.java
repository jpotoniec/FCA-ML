/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.table.TableModel;

public interface Classifier {

    public void addExample(Map<String, Double> features, boolean accept);

    public void updateModel();

    public double classify(Map<String, Double> featuresMap);

    public String getJustification();

    public void setup(List<String> attributes);

    public void setup(String... attributes);

    public TableModel getExamplesTableModel();

    public void saveExamples(File f) throws IOException;

    public void loadExamples(File f) throws IOException;

    public int[] getClassDistribution();
}
