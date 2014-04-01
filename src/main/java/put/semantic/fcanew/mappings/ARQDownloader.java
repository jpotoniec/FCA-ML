package put.semantic.fcanew.mappings;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;

/**
 *
 * @author smaug
 */
public class ARQDownloader implements Downloader {

    protected Mappings mappings;

    public ARQDownloader(Mappings mappings) {
        this.mappings = mappings;
    }

    @Override
    public Mappings getMappings() {
        return mappings;
    }

    @Override
    public String getRepresentativeURI(Mappings.Entry m) {
        String prefixes = mappings.getPrefixes();
        String endpoint = mappings.getEndpoint();
        String pattern = String.format("%s\nselect distinct ?x where {%s} limit 1", prefixes, m.getPattern());
        System.err.println(pattern);
        try {
            QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, pattern);
            ResultSet result = qe.execSelect();
            if (result.hasNext()) {
                QuerySolution s = result.next();
                String var = s.varNames().next();
                String uri = s.getResource(var).getURI();
                if (uri == null) {
                    return "";
                }
                return uri;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean matches(String uri, Mappings.Entry m) {
        String prefixes = mappings.getPrefixes();
        String endpoint = mappings.getEndpoint();
        String pattern = String.format("%s\nask where {%s} bindings ?x {(<%s>)}", prefixes, m.getPattern(), uri);
        System.err.println(pattern);
        try {
            QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, pattern);
            return qe.execAsk();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
