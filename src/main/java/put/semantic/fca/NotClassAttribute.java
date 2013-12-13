/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public class NotClassAttribute extends ClassAttribute {

    public NotClassAttribute(Reasoner kb, String uri) {
        super(kb, uri);
        OntClass tmp = this.clazz;
        this.clazz = this.notClazz;
        this.notClazz = tmp;
    }

    @Override
    public String getHumanDescription() {
        return String.format("Does object not belong to class %s?", notClazz.getLocalName());
    }

    @Override
    public String toString() {
        return "¬" + notClazz.getLocalName();
    }
}
