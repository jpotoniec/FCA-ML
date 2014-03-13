/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features.values;

/**
 *
 * @author smaug
 */
public class NumericFeatureValue implements FeatureValue {

    private double value;

    public NumericFeatureValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

}
