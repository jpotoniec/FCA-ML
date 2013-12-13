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
public interface Attribute {

    public boolean isSatisfiedBy(Individual a);

    public boolean isOppositeSatisfiedBy(Individual a);
    
    public String getHumanDescription();        

    public OntClass getOntClass(Reasoner kb);
}
