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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 *
 * @author smaug
 */
public class LinearRegression extends AbstractClassifier {

    private List<double[]> examples = new ArrayList<>();
    private List<Double> decisions = new ArrayList<>();
    private OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();

    @Override
    public void addExample(Map<String, Double> features, boolean accept) {
        double decision = accept ? 1 : 0;
        examples.add(transform(features));
        decisions.add(decision);
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
}
