/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.mappings;

import put.semantic.fcanew.Attribute;

/**
 *
 * @author smaug
 */
public interface Downloader {

    public Mappings getMappings();

    /**
     * Returns URI of object from remote KB specified in {@link getMappings()}
     *
     * @param attr
     * @return Valid URI or empty string.
     */
    public String getRepresentativeURI(Attribute... attr);

    /**
     *
     * @param uri
     * @param attributes
     * @return length==attributes.length
     */
    public boolean[] matches(String uri, Attribute... attributes);
}
