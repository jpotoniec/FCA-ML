/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fca.simul;

import java.io.File;
import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.KB;
import put.semantic.fcanew.SetOfAttributes;
import put.semantic.fcanew.SimpleSetOfAttributes;
import put.semantic.fcanew.SubsetOfAttributes;
import put.semantic.fcanew.simul.OWLExpert;
import put.semantic.fcanew.ui.ClassAttribute;

/**
 *
 * @author smaug
 */
public class OWLExpertTest {

    private OWLExpert compl, incompl;
    private Implication valid, invalid;

    @Before
    public void setup() throws OWLOntologyCreationException {
        KB complKB, incomplKB;
        complKB = new KB();
        complKB.setup(Arrays.asList(new File("countries/myCountriesCompleted.owl")), KB.Reasoner.PELLET);
        compl = new OWLExpert(complKB);
        incomplKB = new KB();
        incomplKB.setup(Arrays.asList(new File("countries/myCountriesNotCompleted.owl")), KB.Reasoner.PELLET);
        incompl = new OWLExpert(incomplKB);
        OWLDataFactory f = complKB.getManager().getOWLDataFactory();
        ClassAttribute ac, ec, eum;
        ac = new ClassAttribute(f.getOWLClass(IRI.create("file:/tmp/countries.owl#AsianCountry")), null);
        ec = new ClassAttribute(f.getOWLClass(IRI.create("file:/tmp/countries.owl#EuropeanCountry")), null);
        eum = new ClassAttribute(f.getOWLClass(IRI.create("file:/tmp/countries.owl#EUmember")), null);
        SetOfAttributes attrs = new SimpleSetOfAttributes(Arrays.asList(ac, ec, eum));
        valid = new Implication(new SubsetOfAttributes(attrs, new Integer[]{0, 1}), new SubsetOfAttributes(attrs, new Integer[]{2}));
        invalid = new Implication(new SubsetOfAttributes(attrs, new Integer[]{0}), new SubsetOfAttributes(attrs, new Integer[]{2}));
    }

    @Test
    public void complValid() {
        assertEquals(Expert.Decision.ACCEPT, compl.verify(valid));
    }

    @Test
    public void incomplValid() {
        assertEquals(Expert.Decision.REJECT, incompl.verify(valid));
    }

    @Test
    public void complInvalid() {
        assertEquals(Expert.Decision.REJECT, compl.verify(invalid));
    }

    @Test
    public void incomplInvalid() {
        assertEquals(Expert.Decision.REJECT, incompl.verify(invalid));
    }

    @Test
    public void complVerifyValid() {
        compl.setVerify(true);
        assertEquals(Expert.Decision.ACCEPT, compl.verify(valid));
    }

    @Test(expected = IllegalStateException.class)
    public void incomplVerifyValid() {
        incompl.setVerify(true);
        incompl.verify(valid);
    }

}
