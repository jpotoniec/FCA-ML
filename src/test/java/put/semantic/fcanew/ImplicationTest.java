/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ImplicationTest {

    @Mock
    private Attribute a, b, c, d;

    @Test
    public void reduceTest() {
        SetOfAttributes attrs = new SimpleSetOfAttributes(Arrays.asList(a, b, c, d));
        SetOfImplications base = new SetOfImplications();
        base.add(new Implication(new SubsetOfAttributes(attrs, new Integer[]{0}), new SubsetOfAttributes(attrs, new Integer[]{1})));
        Implication x = new Implication(new SubsetOfAttributes(attrs, new Integer[]{0, 1}), new SubsetOfAttributes(attrs, new Integer[]{2}));
        SetOfImplications reducted = x.reduce(base);
        assertEquals(1, reducted.size());
        Implication expected = new Implication(new SubsetOfAttributes(attrs, new Integer[]{0}), new SubsetOfAttributes(attrs, new Integer[]{2}));
        assertArrayEquals(new Object[]{expected}, reducted.toArray());
    }
}
