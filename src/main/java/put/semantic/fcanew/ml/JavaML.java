/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.util.Arrays;
import java.util.Map;
import libsvm.LibSVM;
import net.sf.javaml.classification.AbstractClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;

public class JavaML extends put.semantic.fcanew.ml.AbstractClassifier {

    private AbstractClassifier classifier = new LibSVM();
    private Dataset dataset = new DefaultDataset();

    public JavaML(String... attributes) {
        super(Arrays.asList(attributes));
    }

    @Override
    public void addExample(Map<String, Double> features, double decision) {
        dataset.add(new DenseInstance(transform(features), decision));
    }

    @Override
    public void updateModel() {
        classifier.buildClassifier(dataset);
    }

    @Override
    public double classify(Map<String, Double> featuresMap) {
        try {
            DenseInstance i = new DenseInstance(transform(featuresMap));
            Map<Object, Double> distr = classifier.classDistribution(i);
            justification = "";
            for (Map.Entry<Object, Double> e : distr.entrySet()) {
                justification += String.format("%s->%f ", e.getKey(), e.getValue());
            }
            Object clazz = classifier.classify(i);
            return (Double) clazz;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Double.NaN;
        }
    }

}
