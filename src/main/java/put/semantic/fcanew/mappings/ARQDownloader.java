package put.semantic.fcanew.mappings;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import put.semantic.fcanew.Attribute;

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

    protected Mappings.Entry getEntry(Attribute a) {
        for (Mappings.Entry e : mappings.getEntries()) {
            if (a.equals(e.getAttribute())) {
                return e;
            }
        }
        return null;
    }

    protected boolean isValid(Mappings.Entry e) {
        return e != null && e.getPattern() != null && !e.getPattern().isEmpty();
    }

    protected String buildPattern(Attribute... attr) {
        StringBuilder result = new StringBuilder();
        for (Attribute a : attr) {
            Mappings.Entry e = getEntry(a);
            if (!isValid(e)) {
                return "";
            }
            if (result.length() > 0) {
                result.append(".\n");
            }
            result.append(e.getPattern());
        }
        return result.toString();
    }

    @Override
    public String getRepresentativeURI(Attribute... attr) {
        String prefixes = mappings.getPrefixes();
        String endpoint = mappings.getEndpoint();
        String where = buildPattern(attr);
        if (where.isEmpty()) {
            return "";
        }
        String query = String.format("%s\nselect distinct ?x where {%s} limit 1", prefixes, where);
        System.err.println(query);
        try {
            QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, query);
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
    public boolean[] matches(String uri, Attribute... attributes) {
        boolean[] result = new boolean[attributes.length];
        for (int i = 0; i < attributes.length; ++i) {
            Mappings.Entry e = getEntry(attributes[i]);
            result[i] = isValid(e) && matches(uri, e);
        }
        return result;
    }

    protected boolean matches(String uri, Mappings.Entry m) {
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
