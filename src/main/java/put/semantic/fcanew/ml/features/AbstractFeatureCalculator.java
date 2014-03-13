/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import put.semantic.fcanew.ml.features.values.BooleanFeatureValue;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;

/**
 *
 * @author smaug
 */
public abstract class AbstractFeatureCalculator implements FeatureCalculator {

    private List<String> names;

    public AbstractFeatureCalculator(List<String> name) {
        this.names = Collections.unmodifiableList(new ArrayList<>(name));
    }

    public AbstractFeatureCalculator(String... name) {
        this.names = Collections.unmodifiableList(Arrays.asList(name));
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    protected List<? extends NumericFeatureValue> transform(double... values) {
        List<NumericFeatureValue> result = new ArrayList<>();
        for (double v : values) {
            result.add(new NumericFeatureValue(v));
        }
        return result;
    }

    protected List<? extends BooleanFeatureValue> transform(boolean... values) {
        List<BooleanFeatureValue> result = new ArrayList<>();
        for (boolean v : values) {
            result.add(new BooleanFeatureValue(v));
        }
        return result;
    }

}
