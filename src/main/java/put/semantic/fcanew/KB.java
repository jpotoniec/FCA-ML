/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import uk.ac.manchester.cs.jfact.JFactFactory;

/**
 *
 * @author smaug
 */
public class KB {

    public enum Reasoner {

        HERMIT, PELLET, JFACT
    };

    private OWLReasoner reasoner;
//    private OWLOntology abox;
    private OWLOntology mainOntology;
//    private OWLOntology tbox;
    private OWLOntologyManager manager;

    public OWLReasoner getReasoner() {
        return reasoner;
    }

    public OWLOntology getAbox() {
        return mainOntology;
    }

    public OWLOntology getTbox() {
        return mainOntology;
    }

    public OWLOntology getMainOntology() {
        return mainOntology;
    }

    public OWLOntologyManager getManager() {
        return manager;
    }        

    private void createMainOntology(List<File> files) throws OWLOntologyCreationException {
        Set<OWLOntology> ontologies = new HashSet<>();
        for (File f : files) {
            ontologies.addAll(manager.getImportsClosure(manager.loadOntology(IRI.create(f))));
        }
        mainOntology = manager.createOntology(IRI.generateDocumentIRI(), ontologies);
    }

    private void createReasoner(Reasoner r) throws OWLOntologyCreationException {        
        OWLOntology o = mainOntology;
        switch (r) {
            case HERMIT:
                reasoner = new org.semanticweb.HermiT.Reasoner.ReasonerFactory().createReasoner(o);
                break;
            case PELLET:
                reasoner = new PelletReasoner(o, BufferingMode.BUFFERING);
                break;
            case JFACT:
                reasoner = new JFactFactory().createReasoner(o);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void setup(List<File> files, Reasoner r) throws OWLOntologyCreationException {
        manager = OWLManager.createOWLOntologyManager();
//        tbox = manager.createOntology(IRI.generateDocumentIRI());
//        abox = manager.createOntology(IRI.generateDocumentIRI());
        createMainOntology(files);        
        createReasoner(r);
    }
}
