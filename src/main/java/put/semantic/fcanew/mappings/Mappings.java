/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.mappings;

import java.util.List;
import put.semantic.fcanew.Attribute;

/**
 *
 * @author smaug
 */
public interface Mappings {

    public interface Entry {

        Attribute getAttribute();

        String getPattern();

    }

    String getEndpoint();

    String getPrefixes();

    List<? extends Entry> getEntries();

    String getPattern(Attribute a);

}
