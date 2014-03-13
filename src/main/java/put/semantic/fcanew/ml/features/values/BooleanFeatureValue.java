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
public class BooleanFeatureValue extends NumericFeatureValue {

    public BooleanFeatureValue(boolean value) {
        super(value ? 1 : 0);
    }

}
