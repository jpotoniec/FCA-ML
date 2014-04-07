/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author smaug
 */
public class FCA {

    protected Expert expert;
    protected PartialContext context;
    protected SubsetOfAttributes p, forced;
    protected SetOfImplications implications;
    protected Implication current;
    protected Iterable<? extends Implication> bk;
    protected boolean goToNext;

    public void setBackgroundKnowledge(Iterable<? extends Implication> bk) {
        this.bk = bk;
    }

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

    public void reset(Iterable<Attribute> forced) {
        SetOfAttributes all = context.getAttributes();
        this.forced = new SubsetOfAttributes(all, forced);
        p = new SubsetOfAttributes(all);
        implications = new SetOfImplications();
        if (bk != null) {
            for (Implication i : bk) {
                implications.add(i);
            }
        }
    }

    protected void processCurrentImplication() {
        Expert.Decision decision = expert.verify(current);
        switch (decision) {
            case ACCEPT:
                accept();
                break;
            case REJECT:
                reject();
                goToNext = false;
                break;
            case SKIP:
                throw new UnsupportedOperationException("Skip is not supported");
        }
    }

    protected boolean isInteresting(Implication i) {
        if (i.isTrivial()) {
            return false;
        }
        return true;
    }

    public Set<SubsetOfAttributes> conclusions(SubsetOfAttributes premises) {
        SetOfAttributes all = context.getAttributes();
        Set<SubsetOfAttributes> result = new HashSet<>();
        SubsetOfAttributes kp = context.K(premises);
        if (premises.containsAny(forced)) {
            result.add(kp);
        } else {
            for (Attribute a : forced) {
                SubsetOfAttributes x = new SubsetOfAttributes(all, Arrays.asList(a));
                result.add(SubsetOfAttributes.intersectionOf(kp, context.K(x), forced));
            }
        }
        return result;
    }

    public void run() {
        while (!p.isFull()) {
            List<Implication> pending = new ArrayList<>();
            for (SubsetOfAttributes c : conclusions(p)) {
                for (SubsetOfAttributes e : c.split()) {
                    pending.add(new Implication(p, e));
                }
            }
            goToNext = true;
            for (Implication i : pending) {
                //check if i isn't already refuted or following from set of accepted implications
                this.current = i;
                System.out.printf("Current implication: %s\n", current);
                if (i.isRefutedBy(context)) {
                    System.out.println("Ignoring, already refuted");
                } else if (isInteresting(current)) {
                    processCurrentImplication();
                }
            }
            if (goToNext) {
                p = getNext(p);
            }
        }
    }

    protected void accept() {
        implications.add(current);
        context.update(current);
    }

    protected void reject() {
//        context.addPOD(counterexample);
    }
}
