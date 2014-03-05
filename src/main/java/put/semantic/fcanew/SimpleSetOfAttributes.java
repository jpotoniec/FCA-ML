/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SimpleSetOfAttributes implements SetOfAttributes {

    private List<Attribute> attributes;

    public SimpleSetOfAttributes(Collection<Attribute> attributes) {
        this.attributes = Collections.unmodifiableList(new ArrayList<>(attributes));
    }

    @Override
    public Attribute get(int index) {
        return attributes.get(index);
    }

    @Override
    public int size() {
        return attributes.size();
    }

    @Override
    public Iterator<Attribute> iterator() {
        return attributes.iterator();
    }

    @Override
    public int indexOf(Attribute a) {
        return attributes.indexOf(a);
    }

}
