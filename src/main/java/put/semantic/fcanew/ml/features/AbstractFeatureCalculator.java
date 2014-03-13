/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml.features;

/**
 *
 * @author smaug
 */
public abstract class AbstractFeatureCalculator implements FeatureCalculator {

    private String name;

    public AbstractFeatureCalculator(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
