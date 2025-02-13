/*
import com.clarkparsia.owlapiv3.OWL;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    public OWLClassExpression parseQueryString(String ns, String queryStr) {
        // Parse the query and return an appropriate OWLClassExpression
        queryStr = queryStr.trim();
        if (queryStr.startsWith("(") && queryStr.endsWith(")")) {
            queryStr = queryStr.substring(1, queryStr.length() - 1).trim();
        }

        // Handle OR, AND, NOT, quantification, etc. for different query types
        List<String> orParts = splitAtTopLevel(queryStr, " or ");
        if (orParts.size() > 1) {
            List<OWLClassExpression> orExpressions = new ArrayList<>();
            for (String orPart : orParts) {
                orExpressions.add(parseQueryString(ns, orPart));
            }
            return OWL.or(orExpressions.toArray(new OWLClassExpression[0]));
        }

        List<String> andParts = splitAtTopLevel(queryStr, " and ");
        if (andParts.size() > 1) {
            List<OWLClassExpression> andExpressions = new ArrayList<>();
            for (String andPart : andParts) {
                andExpressions.add(parseQueryString(ns, andPart));
            }
            return OWL.and(andExpressions.toArray(new OWLClassExpression[0]));
        }

        if (queryStr.startsWith("not ")) {
            String subQuery = queryStr.substring(4).trim();
            return OWL.not(parseQueryString(ns, subQuery));
        }

        return OWL.Class(ns + queryStr.trim());
    }

    private List<String> splitAtTopLevel(String input, String delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int lastIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && input.startsWith(delimiter, i)) {
                result.add(input.substring(lastIndex, i).trim());
                lastIndex = i + delimiter.length();
                i += delimiter.length() - 1; // skip the delimiter
            }
        }
        result.add(input.substring(lastIndex).trim());
        return result;
    }
}
*/

package ontology;
import com.clarkparsia.owlapiv3.OWL;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;

import java.util.ArrayList;
import java.util.List;

public class QueryParser {

    private final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    public OWLClassExpression parseQueryString(String ns, String queryStr) {
        queryStr = queryStr.trim();
        if (queryStr.startsWith("(") && queryStr.endsWith(")")) {
            queryStr = queryStr.substring(1, queryStr.length() - 1).trim();
        }

        if (containsOperator(queryStr, " or ")) {
            return parseOrQuery(ns, queryStr);
        }

        if (containsOperator(queryStr, " and ")) {
            return parseAndQuery(ns, queryStr);
        }

        if (queryStr.startsWith("not ")) {
            return parseNotQuery(ns, queryStr);
        }

        if (queryStr.contains(" some ")) {
            return parseSomeQuery(ns, queryStr);
        }

        return OWL.Class(ns + queryStr.trim());
    }

    private boolean containsOperator(String queryStr, String operator) {
        return splitAtTopLevel(queryStr, operator).size() > 1;
    }

    private OWLClassExpression parseOrQuery(String ns, String queryStr) {
        List<String> orParts = splitAtTopLevel(queryStr, " or ");
        List<OWLClassExpression> orExpressions = new ArrayList<>();
        for (String orPart : orParts) {
            orExpressions.add(parseQueryString(ns, orPart));
        }
        return OWL.or(orExpressions.toArray(new OWLClassExpression[0]));
    }

    private OWLClassExpression parseAndQuery(String ns, String queryStr) {
        List<String> andParts = splitAtTopLevel(queryStr, " and ");
        List<OWLClassExpression> andExpressions = new ArrayList<>();
        for (String andPart : andParts) {
            andExpressions.add(parseQueryString(ns, andPart));
        }
        return OWL.and(andExpressions.toArray(new OWLClassExpression[0]));
    }

    private OWLClassExpression parseNotQuery(String ns, String queryStr) {
        String subQuery = queryStr.substring(4).trim();
        return OWL.not(parseQueryString(ns, subQuery));
    }

    private OWLClassExpression parseSomeQuery(String ns, String queryStr) {
        int someIndex = queryStr.indexOf(" some ");
        String property = queryStr.substring(0, someIndex).trim();
        String value = queryStr.substring(someIndex + 6).trim();

        if (value.startsWith("(") && value.endsWith(")")) {
            value = value.substring(1, value.length() - 1).trim();
        }

        //OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(ns + property);
        OWLObjectProperty objectProperty = dataFactory.getOWLObjectProperty(IRI.create(ns + property));

        OWLClassExpression filler = parseQueryString(ns, value);

        return dataFactory.getOWLObjectSomeValuesFrom(objectProperty, filler);
    }

    private List<String> splitAtTopLevel(String input, String delimiter) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int lastIndex = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && input.startsWith(delimiter, i)) {
                result.add(input.substring(lastIndex, i).trim());
                lastIndex = i + delimiter.length();
                i += delimiter.length() - 1;
            }
        }
        result.add(input.substring(lastIndex).trim());
        return result;
    }
}





