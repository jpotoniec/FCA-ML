package put.semantic.fcanew.mappings;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
    public List<String> select(int limit, Attribute... attr) {
        String prefixes = mappings.getPrefixes();
        String endpoint = mappings.getEndpoint();
        String where = buildPattern(attr);
        if (where.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        String query = String.format("%s\nselect distinct ?x where {%s}", prefixes, where);
        if (limit > 0) {
            query += String.format("\nlimit %d", limit);
        }
        System.err.println(query);
        try {
            List<String> result = new ArrayList<>();
            QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, query);
            ResultSet queryResult = qe.execSelect();
            while (queryResult.hasNext()) {
                QuerySolution s = queryResult.next();
                String var = s.varNames().next();
                String uri = s.getResource(var).getURI();
                if (uri != null) {
                    result.add(uri);
                }
            }
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String getRepresentativeURI(Attribute... attr) {
        List<String> select = select(1, attr);
        if (select.isEmpty()) {
            return "";
        } else {
            return select.get(0);
        }
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
