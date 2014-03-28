/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;

public class MultiList<T> {

    protected static class MagicModel<T> extends DefaultListModel<T> implements Iterable<T> {

        @Override
        public Iterator<T> iterator() {
            final Enumeration e = ((DefaultListModel) this).elements();
            return new Iterator<T>() {

                @Override
                public boolean hasNext() {
                    return e.hasMoreElements();
                }

                @Override
                public T next() {
                    return (T) e.nextElement();
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            };
        }

    }

    protected List<T> data;
    protected MagicModel<T>[] models;

    public MultiList(List<? extends T> data, int n) {
        this.data = new ArrayList<>(data);
        this.models = new MagicModel[n];
        for (int i = 0; i < n; ++i) {
            this.models[i] = new MagicModel<>();
        }
        for (T element : data) {
            this.models[0].addElement(element);
        }
    }

    public List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    public Iterable<T> getData(int subset) {
        return this.models[subset];
    }

    public ListModel getModel(int list) {
        return models[list];
    }

    public boolean move(T object, int from, int to) {
        if (models[from].contains(object)) {
            models[from].removeElement(object);
            models[to].addElement(object);
            return true;
        } else {
            return false;
        }
    }

}
