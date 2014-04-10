/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 * @author smaug
 */
public abstract class AbstractClassifier implements Classifier {

    protected List<String> attributes = null;
    protected String justification = "";
    private double rejectedWeight;

    @Override
    public void setup(List<String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void setup(String... attributes) {
        setup(Arrays.asList(attributes));
    }

    protected double[] transform(Map<String, Double> features) {
        assert attributes != null;
        double[] example = new double[attributes.size()];
        for (int i = 0; i < example.length; ++i) {
            example[i] = Double.NaN;
        }
        for (Map.Entry<String, Double> f : features.entrySet()) {
            int n = attributes.indexOf(f.getKey());
            if (n >= 0) {
                example[n] = f.getValue();
            }
        }
        return example;
    }

    @Override
    public String getJustification() {
        return justification;
    }

    @Override
    public double getRejectedWeight() {
        return rejectedWeight;
    }

    @Override
    public void setRejectedWeight(double w) {
        this.rejectedWeight = w;
    }

}
