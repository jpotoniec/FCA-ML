/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;
import put.semantic.fcanew.ml.Classifier;
import put.semantic.fcanew.ml.LinearRegression;
import put.semantic.fcanew.ml.WekaClassifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.Id3;

/**
 *
 * @author smaug
 */
public class AvailableClassifiersModel implements ComboBoxModel<Classifier> {

    private static final Classifier[] data = new Classifier[]{
        new WekaClassifier(new JRip()),
        new WekaClassifier(new LibSVM()),
        new WekaClassifier(new Id3()),
        new LinearRegression()
    };

    private Classifier selected = data[0];

    @Override
    public int getSize() {
        return data.length;
    }

    @Override
    public Classifier getElementAt(int index) {
        return data[index];
    }

    @Override
    public void addListDataListener(ListDataListener l) {
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
    }

    @Override
    public void setSelectedItem(Object anItem) {
        if (anItem instanceof Classifier) {
            selected = (Classifier) anItem;
        }
    }

    @Override
    public Object getSelectedItem() {
        return selected;
    }

}
