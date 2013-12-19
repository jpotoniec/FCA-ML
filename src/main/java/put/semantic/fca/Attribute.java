/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;

/**
 *
 * @author smaug
 */
public interface Attribute {
        
    public String getHumanDescription();        

    public OntClass getOntClass();
    
    public List<String> getExamples();
    
    public List<String> getCounterexamples();
    
    boolean isSatisfiedBy(Individual a);
    
    public boolean isOppositeSatisfiedBy(Individual a);
}
