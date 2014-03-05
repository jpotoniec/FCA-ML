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
public interface SetOfAttributes extends Iterable<Attribute> {

    public Attribute get(int index);

    public int size();

    public int indexOf(Attribute a);
}
