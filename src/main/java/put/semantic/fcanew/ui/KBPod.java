/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import com.hp.hpl.jena.ontology.Individual;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.POD;
import put.semantic.fcanew.SetOfAttributes;
import put.semantic.fcanew.SubsetOfAttributes;

/**
 *
 * @author smaug
 */
public class KBPod implements POD {

    private Individual ind;
    private SubsetOfAttributes p, n;

    public KBPod(Individual ind, SetOfAttributes attributes) {
        this.ind = ind;
        this.p = new SubsetOfAttributes(attributes);
        this.n = new SubsetOfAttributes(attributes);
        for (Attribute a : attributes) {
            ClassAttribute attr = (ClassAttribute) a;
            if (ind.hasOntClass(attr.getOntClass())) {
                p.add(attr);
            } else if (ind.hasOntClass(attr.getComplement())) {
                n.add(attr);
            }
        }
    }

    @Override
    public SubsetOfAttributes getPositive() {
        return p;
    }

    @Override
    public SubsetOfAttributes getNegative() {
        return n;
    }

    public Individual getIndividual() {
        return ind;
    }
    
    

}
