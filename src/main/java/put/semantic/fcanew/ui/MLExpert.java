/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import put.semantic.fcanew.Attribute;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.Implication;
import put.semantic.fcanew.PartialContext;
import put.semantic.fcanew.ml.Classifier;
import put.semantic.fcanew.ml.features.FeatureCalculator;
import put.semantic.fcanew.ml.features.values.FeatureValue;
import put.semantic.fcanew.ml.features.values.NumericFeatureValue;

public class MLExpert implements Expert {

    public static interface MLExpertEventListener extends EventListener {

        public void implicationAccepted(ImplicationDescription i, boolean autoDecision);

        public void implicationRejected(ImplicationDescription i, boolean autoDecision);

        public void ask(ImplicationDescription i, String justification);
    }

    private final List<? extends FeatureCalculator> calculators;
    private final Object lock = new Object();
    private Expert.Decision dec;
    private Map<String, Double> lastFeatures;
    private Implication currentImplication;
    private final Classifier classifier;
    private double clResult = Double.NaN;
    private final int credibilityTreshold;
    private final double ignoreTreshold;
    private final PartialContext context;
    private final List<MLExpertEventListener> listeners = new ArrayList<>();
    private final double autoAcceptTreshold;
    private boolean autoDecision;

    public void addEventListener(MLExpertEventListener l) {
        if (l != null) {
            listeners.add(l);
        }
    }

    public void removeEventListener(MLExpertEventListener l) {
        listeners.remove(l);
    }

    protected void fireImplicationAccepted() {
        assert !EventQueue.isDispatchThread();
        final ImplicationDescription i = new ImplicationDescription(currentImplication, clResult, shouldAccept(), lastFeatures);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (MLExpertEventListener l : listeners) {
                    l.implicationAccepted(i, autoDecision);
                }
            }
        });
    }

    protected void fireImplicationRejected() {
        assert !EventQueue.isDispatchThread();
        final ImplicationDescription i = new ImplicationDescription(currentImplication, clResult, shouldAccept(), lastFeatures);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (MLExpertEventListener l : listeners) {
                    l.implicationRejected(i, autoDecision);
                }
            }
        });
    }

    protected void fireAsk(final String justification) {
        assert !EventQueue.isDispatchThread();
        final ImplicationDescription i = new ImplicationDescription(currentImplication, clResult, shouldAccept(), lastFeatures);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                for (MLExpertEventListener l : listeners) {
                    l.ask(i, justification);
                }
            }
        });
    }

    public MLExpert(Classifier cl, int credibilityTreshold, List<? extends FeatureCalculator> calculators, double ignoreTreshold, PartialContext context, double autoAcceptTreshold) {
        this.classifier = cl;
        this.credibilityTreshold = credibilityTreshold;
        this.calculators = calculators;
        this.ignoreTreshold = ignoreTreshold;
        this.context = context;
        this.autoAcceptTreshold = autoAcceptTreshold;
        List<String> features = new ArrayList<>();
        for (FeatureCalculator calc : calculators) {
            features.addAll(calc.getNames());
        }
        classifier.setup(features.toArray(new String[0]));
    }

    @Override
    public Expert.Decision verify(Implication impl) {
        this.currentImplication = impl;
        synchronized (lock) {
            dec = null;
        }
        ask(impl);
        while (true) {
            try {
                synchronized (lock) {
                    if (dec == null) {
                        lock.wait();
                    }
                    System.out.println("Decision: " + dec);
                    return dec;
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void rememberExample(boolean accept) {
        double p = accept ? 1 : 0;
        if (Math.abs(clResult - p) < ignoreTreshold || autoDecision) {
            return;
        }
        //clResult is nan or is far enough from proper value
        synchronized (classifier) {
            classifier.addExample(lastFeatures, accept);
            classifier.updateModel();
        }
    }

    public void accept() {
        if (currentImplication.isRefutedBy(context)) {
            return;
        }
        fireImplicationAccepted();
        synchronized (lock) {
            rememberExample(true);
            dec = Expert.Decision.ACCEPT;
            lock.notify();
        }
    }

    private int counter = 0;

    protected String generateURI() {
        return String.format("http://www.nonexisting.org/%d", ++counter);
    }

    protected boolean addCounterexample() {
        if (currentImplication.getConclusions().size() != 1) {
            return false;
        }
        OWLReasoner model = context.getModel();
        OWLOntologyManager m = model.getRootOntology().getOWLOntologyManager();
        OWLDataFactory f = m.getOWLDataFactory();
        OWLNamedIndividual ind = f.getOWLNamedIndividual(IRI.create(generateURI()));
        if (!currentImplication.getPremises().isEmpty()) {
            for (Attribute a : currentImplication.getPremises()) {
                m.addAxiom(model.getRootOntology(), f.getOWLClassAssertionAxiom(((ClassAttribute) a).getOntClass(), ind));
            }
        }
        m.addAxiom(model.getRootOntology(), f.getOWLClassAssertionAxiom(((ClassAttribute) currentImplication.getConclusions().iterator().next()).getComplement(), ind));
        model.flush();
        context.updateContext();
        return true;
    }

    public void reject() {
        new SwingWorker<Boolean, Object>() {

            @Override
            protected Boolean doInBackground() throws Exception {
                if (!currentImplication.isRefutedBy(context)) {
                    return addCounterexample();
                } else {
                    return true;
                }
            }

            @Override
            protected void done() {
                try {
                    if (!get()) {
                        return;
                    }
                    fireImplicationRejected();
                    synchronized (lock) {
                        rememberExample(false);
                        dec = Expert.Decision.REJECT;
                        lock.notify();
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    //should never happen
                    Logger.getLogger(MLExpert.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }.execute();
    }

    private Map<String, Double> getFeatures(Implication impl) {
        Map<String, Double> result = new TreeMap<>();
        for (FeatureCalculator calc : calculators) {
            System.err.println(calc.getClass());
            List<String> names = calc.getNames();
            List<? extends FeatureValue> values = calc.compute(impl, context.getModel(), context);
            for (int i = 0; i < names.size(); ++i) {
                result.put(names.get(i), ((NumericFeatureValue) values.get(i)).getValue());
            }
        }
        return result;
    }

    private Boolean shouldAccept() {
        if (!isClassifierReady()) {
            return null;
        }
        if (clResult > 0.6) {
            return true;
        } else if (clResult < 0.4) {
            return false;
        } else {
            return null;
        }
    }

    private boolean isClassifierReady() {
        int[] dist;
        synchronized (classifier) {
            dist = classifier.getClassDistribution();
        }
        for (int i = 0; i < dist.length; ++i) {
            if (dist[i] < credibilityTreshold) {
                return false;
            }
        }
        return true;
    }

    private String classifierState() {
        int[] dist;
        synchronized (classifier) {
            dist = classifier.getClassDistribution();
        }
        String result = "";
        for (int i = 0; i < dist.length; ++i) {
            if (i > 0) {
                result += ", ";
            }
            result += String.format("%d", dist[i]);
        }
        return "(" + result + ")";
    }

    private void ask(final Implication impl) {
        assert !EventQueue.isDispatchThread();
        autoDecision = false;
        Map<String, Double> features = getFeatures(impl);
        String justification;
        synchronized (classifier) {
            try {
                clResult = classifier.classify(features);
            } catch (Exception ex) {
                clResult = Double.NaN;
                ex.printStackTrace();
            }
            if (isClassifierReady()) {
                justification = String.format("<html>Probability of acceptance: %.3f<br>Justification: <br><pre>%s</pre></html>", clResult, classifier.getJustification());
            } else {
                justification = String.format("<html>Classifier is learning (%.3f)<br>%s</html>", clResult, classifierState());
            }
        }
        synchronized (lock) {
            lastFeatures = features;
            currentImplication = impl;
        }
        fireAsk(justification);
        if (features.get("follows from KB") == 1) {
            accept();
        }
        if (isClassifierReady()) {
            if (clResult < autoAcceptTreshold) {
                autoDecision = true;
                reject();
            }
            if (clResult > 1 - autoAcceptTreshold) {
                autoDecision = true;
                accept();
            }
        }
    }

    public Implication getCurrentImplication() {
        return currentImplication;
    }

    public void loadExamplesFromFile(File f) throws IOException {
        synchronized (classifier) {
            classifier.loadExamples(f);
        }
    }

    public void saveExamplesToFile(File f) throws IOException {
        synchronized (classifier) {
            classifier.saveExamples(f);
        }
    }
}
