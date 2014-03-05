/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author smaug
 */
public class SubsetOfAttributesTest {

    @Test
    public void less1() {
        SubsetOfAttributes a = new SubsetOfAttributes(null, new Integer[]{0, 2, 4});
        SubsetOfAttributes b = new SubsetOfAttributes(null, new Integer[]{0, 2, 4, 6});
        assertFalse(SubsetOfAttributes.less(a, b, 4));
        assertFalse(SubsetOfAttributes.less(a, b, 5));
        assertTrue(SubsetOfAttributes.less(a, b, 6));
    }

    @Test
    public void less2() {
        SubsetOfAttributes a = new SubsetOfAttributes(null, new Integer[]{0, 2, 4, 8, 10});
        SubsetOfAttributes b = new SubsetOfAttributes(null, new Integer[]{0, 2, 4, 6, 8, 10});
        assertFalse(SubsetOfAttributes.less(a, b, 4));
        assertFalse(SubsetOfAttributes.less(a, b, 5));
        assertTrue(SubsetOfAttributes.less(a, b, 6));
        assertFalse(SubsetOfAttributes.less(a, b, 8));
        assertFalse(SubsetOfAttributes.less(a, b, 10));
    }

}
