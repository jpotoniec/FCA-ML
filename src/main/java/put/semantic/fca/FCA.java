package put.semantic.fca;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.apache.commons.lang.StringUtils;
import put.semantic.putapi.Descriptor;
import put.semantic.putapi.IntersectionClass;
import put.semantic.putapi.OntClass;
import put.semantic.putapi.OntObjectProperty;
import put.semantic.putapi.Reasoner;
import put.semantic.putapi.Vocabulary;
import put.semantic.putapi.impl.pellet.PelletClass;
import put.semantic.putapi.impl.pellet.PelletReasoner;

public class FCA {

    private void close(Set<Attribute> set, Set<Implication> implications) {
        int size;
        do {
            size = set.size();
            for (Implication i : implications) {
                if (i.isSatisfiedBy(set)) {
                    set.addAll(i.getConclusions());
                }
            }
        } while (set.size() != size);
    }

    private boolean isLess(Set<Attribute> a, Set<Attribute> b, List<Attribute> allAttributes, int j) {
        for (int i = 0; i < j - 1; ++i) {
            Attribute mi = allAttributes.get(i);
            if (a.contains(mi) != b.contains(mi)) {
                System.err.printf("Disagree on %d\n", i);
                return false;
            }
        }
        Attribute mj = allAttributes.get(j);
        return !a.contains(mj) && b.contains(mj);
    }

    private Set<Attribute> getNext(Set<Attribute> p, Set<Implication> l, List<Attribute> allAttributes) {
        for (int j = allAttributes.size() - 1; j >= 0; --j) {
            System.err.printf("j=%d\n", j);
            Attribute mj = allAttributes.get(j);
            if (p.contains(mj)) {
                continue;
            }
            Set<Attribute> next = new HashSet<>();
            next.addAll(p);
            next.retainAll(allAttributes.subList(0, j + 1));
            next.add(mj);
            close(next, l);
            if (isLess(p, next, allAttributes, j)) {
                return next;
            }
        }
        return p;
//        throw new RuntimeException("Uh-huh, wtf");
    }

    private String ask(Implication i) {
        JPanel panel = new JPanel();
        BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
        panel.setLayout(layout);
        String text = "<html><body width=\"500px\">";
        text += "<b>Premises:</b><p>";
        for (Attribute a : i.getPremises()) {
            text += a.toString() + " ";
        }
        text += "</p>";
        text += "<b>Conclusions:</b><p>";
        for (Attribute a : i.getConclusions()) {
            text += a.toString() + " ";
        }
        text += "</p>";
        text += "</body></html>";
        panel.add(new JLabel(text));
        JTextArea textarea = new JTextArea();
        textarea.setRows(10);
        panel.add(new JScrollPane(textarea));
        int result = JOptionPane.showConfirmDialog(null, panel,
                "Is following implication correct? If not, enter counterexample if possible.", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.NO_OPTION) {
            return textarea.getText();
        } else {
            return null;
        }
    }

    private void compute(Reasoner kb, List<Attribute> allAttributes) {
        Set<Attribute> p = new HashSet<>();
        Set<Implication> l = new HashSet<>();
        while (!p.containsAll(allAttributes)) {
            updateContext();
            System.out.println("P set: " + StringUtils.join(p, " "));
            System.out.println("L set: " + StringUtils.join(l, " "));
            PartialContext pc = new PartialContext(kb, p, allAttributes);
            System.out.println("Partial context obtainted");
            Set<Attribute> notRefuted = pc.getNotRefuted();
            if (!notRefuted.equals(p)) {
                System.out.printf("#non refuted=%d\n", notRefuted.size());
                for (Attribute conclusion : notRefuted) {
                    Implication i = new Implication(p, /*notRefuted*/ new HashSet<>(Arrays.asList(conclusion)));
                    System.out.println("Checking implication: " + i);
                    OntClass premises = i.getPremisesClass(kb);
                    OntClass conclusions = i.getConclusionsClass(kb);
                    if (conclusions.hasSubClass(premises)) { //XXX: tu potencjalnie jest problem, bo w algorytmie jest przecięcie wszystkich konkluzji, a nie tylko tych bez przesłanek
                        System.out.println("Confirmed by KB");
                        l.add(i);
                        close(p, l);
                        p = getNext(p, l, allAttributes);
                    } else {
                        System.out.println("Should ask expert");
                        String answer = ask(i);
                        boolean confirmed = answer == null;
                        if (confirmed) {
                            System.out.println("Confirmed");
                            l.add(i);
                            close(p, l);
                            p = getNext(p, l, allAttributes);
                            System.out.println("Should extend TBox");
                            updateKB(kb, premises, conclusions);

                        } else {
                            System.out.println("Declined");
                            System.out.println("Extending ABox");
                            String[] axioms = answer.split("\\s+");
                            for (String axiom : axioms) {
                                try {
                                    parseAxiom(kb, axiom);
                                } catch (Exception ex) {
                                    System.err.println(axiom);
                                    System.err.println(ex);
                                }
                            }
                            break;
                        }
                    }
                }
//                System.out.println(Descriptor.INSTANCE.describe(premises) + " -> " + Descriptor.INSTANCE.describe(conclusions));
                //check wheter the implication follows from TBox
            } else {
                p = getNext(p, l, allAttributes);
            }
        }
    }
    private static final String OWL = "http://www.w3.org/2002/07/owl#";
    private static final String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns# ";
    private static final String ENDPOINT = "http://localhost:8080/openrdf-sesame/repositories/lubm";
    private Context context;

    private void updateContext() {
        context.update();
    }

    public void xmain(String[] args) {
        final Reasoner kb = new PelletReasoner();
        kb.loadFile("/home/smaug/praca/Asparagus/Main/data/univ-bench.owl");

//        kb.loadFile("/home/smaug/praca/Asparagus/Main/data/University0_0.owl");
        final List<Attribute> allAttributes = new ArrayList<>();
        String property = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#takesCourse";
//        String thing = Vocabulary.Thing;
//        String person = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person";
//        String graduateStudent = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#GraduateStudent";
//        String undergraduateStudent = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#UndergraduateStudent";
//        String student = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Student";
//        String ta = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#TeachingAssistant";
//        String chair = "http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Chair";

        List<OntClass> classes = kb.getClasses();
        for (OntClass c : classes) {
            String uri = c.getURI();
            if (uri != null && !uri.startsWith(OWL) && !uri.startsWith(RDF)) {
//                allAttributes.add(new DomainAttribute(kb, ENDPOINT, property, uri));
                allAttributes.add(new ClassAttribute(kb, uri, ENDPOINT));
//                allAttributes.add(new NotClassAttribute(kb, uri));
            }
        }
        List<OntObjectProperty> properties = kb.getObjectProperties();
        for (OntObjectProperty p : properties) {
            String uri = p.getURI();
            if (uri != null && !uri.startsWith(OWL) && !uri.startsWith(RDF)) {
                allAttributes.add(new PropertyAttribute(kb, ENDPOINT, uri));
            }
        }

        this.context = new Context(allAttributes, kb);

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ContextWindow cw = new ContextWindow(context);
                cw.setVisible(true);
            }
        });

//        allAttributes.add(new ClassAttribute(kb, person));
//        allAttributes.add(new ClassAttribute(kb, graduateStudent));
//        allAttributes.add(new ClassAttribute(kb, student));
//        allAttributes.add(new ClassAttribute(kb, ta));
//        allAttributes.add(new ClassAttribute(kb, undergraduateStudent));
//        allAttributes.add(new ClassAttribute(kb, chair));
//        allAttributes.add(new NotClassAttribute(kb, person));
//        allAttributes.add(new NotClassAttribute(kb, graduateStudent));
//        allAttributes.add(new NotClassAttribute(kb, student));
//        allAttributes.add(new NotClassAttribute(kb, ta));
//        allAttributes.add(new NotClassAttribute(kb, undergraduateStudent));
//        allAttributes.add(new NotClassAttribute(kb, chair));
//        allAttributes.add(new DisjointnessAttribute(kb, graduateStudent, ta));
//        allAttributes.add(new DisjointnessAttribute(kb, graduateStudent, student));
//        allAttributes.add(new DisjointnessAttribute(kb, student, person));
//        allAttributes.add(new DisjointnessAttribute(kb, graduateStudent, undergraduateStudent));
//        allAttributes.add(new DisjointnessAttribute(kb, chair, undergraduateStudent));
//        allAttributes.add(new DisjointnessAttribute(kb, graduateStudent, chair));
        compute(kb, allAttributes);
//        allAttributes.add(new DomainAttribute(kb, property, thing));
//        allAttributes.add(new DomainAttribute(kb, property, person));
//        allAttributes.add(new DomainAttribute(kb, property, graduateStudent));
//        allAttributes.add(new DomainAttribute(kb, property, student));
//        allAttributes.add(new DomainAttribute(kb, property, ta));
//        PartialContext pc = new PartialContext(kb, allAttributes.subList(0, 1), allAttributes);
//        System.out.println(pc);
//        for (POD pod : pc.getDescriptions()) {
//            System.err.println(pod);
//        }
//        for (Attribute a : pc.getNotRefuted()) {
//            System.err.println(a.getHumanDescription());
//        }
    }

    public static void main(String[] args) {
        new FCA().xmain(args);
    }

    private void parseAxiom(Reasoner kb, String axiom) {
        if (axiom == null || axiom.isEmpty()) {
            return;
        }
        System.out.println(axiom);
        boolean complement = false;
        if (axiom.charAt(0) == '!') {
            complement = true;
            axiom = axiom.substring(1);
        }
        Pattern p = Pattern.compile("^([^(]+)\\((.+)\\)$");
        Matcher m = p.matcher(axiom);
        if (m.matches()) {
            String clazzURI = m.group(1);
            String indURI = m.group(2);
            System.out.printf("class='%s' individual='%s'\n", clazzURI, indURI);
            OntClass clazz = kb.getClass(clazzURI);
            if (complement) {
                clazz = kb.createComplementClass(null, clazz);
            }
            fetchIndividual(kb, indURI);
            kb.createIndividual(indURI, new OntClass[]{clazz});
        }
        p = Pattern.compile("^(.*)\\^(.*)$");
        m = p.matcher(axiom);
        if (m.matches()) {
            String c1 = m.group(1);
            String c2 = m.group(2);
            OntClass i = kb.createIntersectionClass(null, new OntClass[]{kb.getClass(c1), kb.getClass(c2)});
            ((PelletClass) kb.getClass(Vocabulary.Nothing)).addSubclass(i);
        }
    }

    private void updateKB(Reasoner kb, OntClass premises, OntClass conclusions) {
        String p = Descriptor.INSTANCE.describe(premises);
        String c = Descriptor.INSTANCE.describe(conclusions);
        System.out.printf("%s -> %s\n", p, c);
        ((PelletClass) conclusions).addSubclass(premises);
        //TODO: chyba należy dodawać do KB dowolny przykład popierający tę implikację
    }

    private void fetchIndividual(Reasoner kb, String uri) {
        //construct {<http://www.Department0.University0.edu/FullProfessor7> a ?type} where {<http://www.Department0.University0.edu/FullProfessor7> a ?type. filter(isuri(?type))}
        kb.construct(ENDPOINT, String.format("construct {<%1$s> a ?type} where {<%1$s> a ?type. filter(isuri(?type))}", uri));
//        kb.construct(ENDPOINT, String.format("construct {<%1$s> ?p []} where {<%1$s> ?p []}", uri));        
//        kb.construct(ENDPOINT, String.format("construct {[] ?p <%1$s>} where {[] ?p <%1$s>}", uri));
    }
}
//!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person(http://www.Department0.University0.edu/AssistantProfessor7/Publication0)
//http://www.Department0.University0.edu/FullProfessor7
//!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Student(http://www.Department0.University0.edu/FullProfessor7)
//http://www.Department0.University0.edu/UndergraduateStudent331
//!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Chair(http://www.Department0.University0.edu/UndergraduateStudent331)
//!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#UndergraduateStudent(http://www.Department0.University0.edu/GraduateStudent1)
/*
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#UndergraduateStudent(http://www.Department0.University0.edu/GraduateStudent1)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Chair(http://www.Department0.University0.edu/GraduateStudent1)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Chair(http://www.Department0.University0.edu/UndergraduateStudent331)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#TeachingAssistant(http://www.Department0.University0.edu/UndergraduateStudent331)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Student(http://www.Department0.University0.edu/FullProfessor7)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#TeachingAssistant(http://www.Department0.University0.edu/FullProfessor7)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Person(http://www.Department0.University0.edu/AssistantProfessor7/Publication0)
 !http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#GraduateStudent(http://www.Department0.University0.edu/UndergraduateStudent331)
 * 
 * 
!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Student(http://www.Department0.University0.edu/Lecturer5)
!http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#Chair(http://www.Department0.University0.edu/Lecturer5)
 
 */