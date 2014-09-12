/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.simul;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.KB;

/**
 *
 * @author smaug
 */
public class OWLExpert implements Expert {

    private KB completedKB;
    private boolean verify = false;

    public OWLExpert(KB kb) {
        this.completedKB = kb;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

    @Override
    public Decision verify(Implication impl) {
        OWLDataFactory f = completedKB.getManager().getOWLDataFactory();
        OWLSubClassOfAxiom a = impl.toAxiom(f);
        if (completedKB.getReasoner().isEntailed(a)) {
            return Decision.ACCEPT;
        } else {
            //well, if ontology really is completed, then reject is safe answer here
            if (verify) {
                assert completedKB.getReasoner().isConsistent();
                OWLOntologyManager m = completedKB.getManager();
                m.addAxiom(completedKB.getMainOntology(), a);
                completedKB.getReasoner().flush();
                boolean cons = completedKB.getReasoner().isConsistent();
                m.removeAxiom(completedKB.getMainOntology(), a);
                completedKB.getReasoner().flush();
                assert completedKB.getReasoner().isConsistent();
                if (cons) {
                    throw new IllegalStateException("Apparently ontology is not completed");
                }
            }
            return Decision.REJECT;
        }
    }

}
