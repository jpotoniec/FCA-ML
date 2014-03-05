/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

/**
 *
 * @author smaug
 */
public class Implication {

    protected SubsetOfAttributes premises, conclusions;

    public Implication(SubsetOfAttributes premises, SubsetOfAttributes conclusions) {
        this.premises = premises;
        this.conclusions = conclusions;
        conclusions.removeAll(premises);
    }

    public SubsetOfAttributes getPremises() {
        return premises;
    }

    public SubsetOfAttributes getConclusions() {
        return conclusions;
    }

    public boolean isTrivial() {
        return conclusions.isEmpty();
    }

    @Override
    public String toString() {
        return premises.toString() + "->" + conclusions.toString();
    }
}
