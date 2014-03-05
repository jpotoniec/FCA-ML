/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.Set;

/**
 *
 * @author smaug
 */
public class FCA {

    protected Expert expert;
    protected PartialContext context;
    protected SubsetOfAttributes p;
    protected SetOfImplications implications;
    protected Implication current;

    public void setContext(PartialContext context) {
        this.context = context;
    }

    public void setExpert(Expert expert) {
        this.expert = expert;
    }

    protected SubsetOfAttributes getNext(SubsetOfAttributes p) {
        for (int j = context.getAttributes().size() - 1; j >= 0; --j) {
            SubsetOfAttributes result = p.getSubset(j - 1);
            result.add(j);
            implications.closure(result);
            if (SubsetOfAttributes.less(p, result, j)) {
                return result;
            }
        }
        return null;
    }

    public void reset() {
        p = new SubsetOfAttributes(context.getAttributes());
        implications = new SetOfImplications();
    }

    protected void processCurrentImplication() {
        Expert.Decision decision = expert.verify(current);
        switch (decision) {
            case ACCEPT:
                accept();
                break;
            case REJECT:
                reject();
                break;
            case SKIP:
                throw new UnsupportedOperationException("Skip is not supported");
        }
    }

    public void run() {
        while (!p.isFull()) {
            current = new Implication(p, context.K(p));
            System.out.printf("Current implication: %s\n", current);
            if (!current.isTrivial()) {
                processCurrentImplication();
            }
            p = getNext(p);
        }
    }

    protected void accept() {
        implications.add(current);
        //TODO: update context w.r.t. implications
    }

    protected void reject() {
//        context.addPOD(counterexample);
    }
}
