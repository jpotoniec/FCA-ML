/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package put.semantic.fcanew.mappings;

/**
 *
 * @author smaug
 */
public interface Downloader {

    public Mappings getMappings();

    /**
     * Returns URI of object from remote KB specified in {@link getMappings()}
     *
     * @param m Mapping
     * @return Valid URI or empty string.
     */
    public String getRepresentativeURI(Mappings.Entry m);

    public boolean matches(String uri, Mappings.Entry m);
}
