/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.util.Map;

public interface Classifier {

    public void addExample(Map<String, Double> features, double decision);

    public void updateModel();

    public double classify(Map<String, Double> featuresMap);

    public String getJustification();
}
