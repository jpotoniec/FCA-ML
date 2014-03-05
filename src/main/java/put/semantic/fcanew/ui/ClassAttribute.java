/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import put.semantic.fcanew.Attribute;

/**
 *
 * @author smaug
 */
public class ClassAttribute implements Attribute {

    private OntClass clazz, complement;

    public ClassAttribute(OntClass clazz, OntModel model) {
        this.clazz = clazz;
        this.complement = model.createComplementClass(null, clazz);
    }

    public OntClass getOntClass() {
        return clazz;
    }

    public OntClass getComplement() {
        return complement;
    }

    @Override
    public String toString() {
        return clazz.getLocalName();
    }

}
