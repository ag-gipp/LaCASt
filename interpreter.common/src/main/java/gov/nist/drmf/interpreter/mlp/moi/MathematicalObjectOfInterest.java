package gov.nist.drmf.interpreter.mlp.moi;

import gov.nist.drmf.interpreter.common.grammar.MathTermTags;
import gov.nist.drmf.interpreter.common.interfaces.IMatcher;
import gov.nist.drmf.interpreter.mlp.SemanticMLPWrapper;
import gov.nist.drmf.interpreter.mlp.extensions.*;
import mlp.MathTerm;
import mlp.ParseException;
import org.intellij.lang.annotations.Language;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Andre Greiner-Petter
 */
public class MathematicalObjectOfInterest {
    @Language("Regexp")
    public static final String WILDCARD_PATTERN = "var\\d+";

    private static final SemanticMLPWrapper mlp = SemanticMLPWrapper.getStandardInstance();

    private final Set<String> identifiers;
    private final PrintablePomTaggedExpression moi;
    private final MatchablePomTaggedExpression matchableMOI;

    private String originalLaTeX;
    private String pattern;

    private HashMap<String, String> wildcardIdentifierMapping;
    private HashMap<String, String> potentialPrimaryIdentifierWildcardMapping;

    public MathematicalObjectOfInterest(String latex) throws ParseException {
        this(mlp.parse(latex));
    }

    public MathematicalObjectOfInterest(PrintablePomTaggedExpression moi) {
        this.originalLaTeX = moi.getTexString();
        this.moi = moi;

        PrintablePomTaggedExpression moiCopy = new PrintablePomTaggedExpression(moi);
        Collection<PrintablePomTaggedExpression> identifierNodes = PrintablePomTaggedExpressionUtility.getIdentifierNodes(moiCopy);

        this.identifiers = identifierNodes.stream()
                .map( PrintablePomTaggedExpression::getRoot )
                .map( MathTerm::getTermText )
                .collect(Collectors.toSet());

        if ( this.identifiers.size() > 1 ) {
            this.wildcardIdentifierMapping = replaceIdentifiersByWildcards(identifierNodes);
        } else this.wildcardIdentifierMapping = new HashMap<>();

        this.pattern = moiCopy.getTexString();
        this.matchableMOI = PomMatcherBuilder.compile(moiCopy, WILDCARD_PATTERN);

        this.potentialPrimaryIdentifierWildcardMapping = new HashMap<>();
        for (Map.Entry<String, String> wildcardIdentifier : this.wildcardIdentifierMapping.entrySet() ) {
            if ( !this.originalLaTeX.matches(".*[(\\[|]\\s*"+wildcardIdentifier.getValue()+"\\s*(?:right)?[|\\])].*")  ) {
                // identifier does not appear isolated in parentheses, so its a candidate for primary identifier
                this.potentialPrimaryIdentifierWildcardMapping.put(wildcardIdentifier.getKey(), wildcardIdentifier.getValue());
            }
        }
    }

    private HashMap<String, String> replaceIdentifiersByWildcards(
            Collection<PrintablePomTaggedExpression> identifierNodes
    ) {
        HashMap<String, String> wildcardToIdentifierMap = new HashMap<>();
        HashMap<String, String> wildcardMemoryMap = new HashMap<>();
        for ( PrintablePomTaggedExpression identifierNode : identifierNodes ) {
            String wildcard = wildcardMemoryMap.computeIfAbsent(
                    identifierNode.getRoot().getTermText(),
                    key -> "var"+wildcardMemoryMap.size()
            );
            wildcardToIdentifierMap.put(wildcard, identifierNode.getRoot().getTermText());
            identifierNode.setRoot(new MathTerm(wildcard, MathTermTags.alphanumeric.tag()));
        }
        return wildcardToIdentifierMap;
    }

    /**
     * Returns either null (if no match was found) or a dependency pattern that represents the match.
     * @param expression the MOI that should be matched
     * @return the dependency pattern that matched or null of no match was found
     */
    public DependencyPattern match(MathematicalObjectOfInterest expression) {
        if ( expression == null ) return null;

        PomMatcher matcher = this.matchableMOI.matcher(
                expression.moi,
                MatcherConfig.getInPlaceMatchConfig().ignoreNumberOfAts(false)
        );

        while ( matcher.find() ) {
            // we found a match. let's see if at least one primary identifier is shared
            Map<String, String> groups = matcher.groups();

            if ( groups.isEmpty() ) {
                // if there are no wildcards, it must have been a perfect match, e.g., z is in \Gamma(z).
                return new DependencyPattern(this.pattern, matcher);
            }

            for ( String wildcard : groups.keySet() ) {
                // if at least one identifier is also a potential primary identifier, we found a match and return true
                if ( groups.get(wildcard).equals( this.potentialPrimaryIdentifierWildcardMapping.get(wildcard) ) )
                    return new DependencyPattern(this.pattern, matcher);
            }
        }

        // we did not found appropriate match
        return null;
    }

    public String getOriginalLaTeX() {
        return originalLaTeX;
    }

    public String getPattern() {
        return pattern;
    }

    public Set<String> getIdentifiers() {
        return identifiers;
    }

    public PrintablePomTaggedExpression getMoi() {
        return moi;
    }

    public MatchablePomTaggedExpression getMatcher() {
        return matchableMOI;
    }

    public HashMap<String, String> getWildcardIdentifierMapping() {
        return wildcardIdentifierMapping;
    }

    public HashMap<String, String> getPotentialPrimaryIdentifierWildcardMapping() {
        return potentialPrimaryIdentifierWildcardMapping;
    }
}
