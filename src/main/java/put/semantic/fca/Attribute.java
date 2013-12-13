/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca;

import java.util.List;
import put.semantic.putapi.Individual;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.Reasoner;

/**
 *
 * @author smaug
 */
public interface Attribute {
        
    public String getHumanDescription();        

    public OntClass getOntClass(Reasoner kb);
    
    public List<String> getExamples();
    
    public List<String> getCounterexamples();
}
