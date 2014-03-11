/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.rules.JRip;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author smaug
 */
public class WekaClassifier extends put.semantic.fcanew.ml.AbstractClassifier {

    private JRip jrip = new JRip();
    private Instances instances;
    private static final FastVector classes;

    static {
        classes = new FastVector(2);
        classes.addElement("reject");
        classes.addElement("accept");
    }

    @Override
    public void setup(List<String> names) {
        ArrayList<String> newNames = new ArrayList<>(names);
        newNames.add("__class");
        super.setup(newNames);
        FastVector attrs = new FastVector();
        for (String name : names) {
            Attribute attr = new Attribute(name);
            attrs.addElement(attr);
        }
        Attribute classAttr = new Attribute("__class", classes);
        attrs.addElement(classAttr);
        instances = new Instances("data", attrs, 0);
        instances.setClass(classAttr);
    }

    protected Instance makeInstance(Map<String, Double> features) {
        Instance result = new Instance(1, transform(features));
        result.setDataset(instances);
        return result;
    }

    @Override
    public void addExample(Map<String, Double> features, boolean accept) {
        Instance i = makeInstance(features);
        i.setClassValue(classes.elementAt(accept ? 1 : 0).toString());
        instances.add(i);
    }

    @Override
    public void updateModel() {
        try {
            jrip.buildClassifier(instances);
            justification = jrip.toString();
        } catch (Exception ex) {
            Logger.getLogger(WekaClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public double classify(Map<String, Double> featuresMap) {
        try {
            Instance i = makeInstance(featuresMap);            
            return jrip.distributionForInstance(i)[1];
        } catch (Exception ex) {
            Logger.getLogger(WekaClassifier.class.getName()).log(Level.SEVERE, null, ex);
            return Double.NaN;
        }
    }

}
