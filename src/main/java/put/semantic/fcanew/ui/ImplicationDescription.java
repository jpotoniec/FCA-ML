/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.ui;

import java.util.Map;
import put.semantic.fcanew.Expert;
import put.semantic.fcanew.Implication;

public class ImplicationDescription {

    private final Implication implication;
    private final double classificationOutcome;
    private final Expert.Suggestion suggestion;
    private final Map<String, Double> features;

    public ImplicationDescription(Implication implication, double classificationOutcome, Boolean accept, Map<String, Double> features) {
        this.implication = implication;
        this.classificationOutcome = classificationOutcome;
        if (accept == null) {
            this.suggestion = Expert.Suggestion.UNKNOWN;
        } else if (accept) {
            this.suggestion = Expert.Suggestion.ACCEPT;
        } else {
            this.suggestion = Expert.Suggestion.REJECT;
        }
        this.features = features;
    }

    public Implication getImplication() {
        return implication;
    }

    public double getClassificationOutcome() {
        return classificationOutcome;
    }

    public Expert.Suggestion getSuggestion() {
        return suggestion;
    }

    public Map<String, Double> getFeatures() {
        return features;
    }

}
