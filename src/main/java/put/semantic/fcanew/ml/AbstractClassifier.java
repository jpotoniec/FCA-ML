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

    protected List<String> attributes;
    protected String justification = "";

    public AbstractClassifier(List<String> attributes) {
        this.attributes = attributes;
    }

    protected double[] transform(Map<String, Double> features) {
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

}
