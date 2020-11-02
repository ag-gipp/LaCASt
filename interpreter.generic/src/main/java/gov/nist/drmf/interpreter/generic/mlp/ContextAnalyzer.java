package gov.nist.drmf.interpreter.generic.mlp;

import com.formulasearchengine.mathosphere.mlp.cli.BaseConfig;
import com.formulasearchengine.mathosphere.mlp.contracts.CreateCandidatesMapper;
import com.formulasearchengine.mathosphere.mlp.contracts.WikiTextAnnotatorMapper;
import com.formulasearchengine.mathosphere.mlp.pojos.*;
import gov.nist.drmf.interpreter.generic.mlp.struct.ContextContentType;
import gov.nist.drmf.interpreter.generic.mlp.struct.MLPDependencyGraph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Andre Greiner-Petter
 */
public class ContextAnalyzer {
    private static final Logger LOG = LogManager.getLogger(ContextAnalyzer.class.getName());

    private final String context;
    private final ContextContentType contentType;

    public ContextAnalyzer(String context) {
        this(context, ContextContentType.guessContentType(context));
    }

    public ContextAnalyzer(String context, ContextContentType contentType) {
        if( ContextContentType.INDETERMINATE.equals(contentType) ) {
            throw new IllegalArgumentException("An indeterminate content type is not supported.");
        }

        this.context = context;
        this.contentType = contentType;
    }

    public MLPDependencyGraph extractDefiniens() {
        MLPDependencyGraph results = null;
        switch (contentType) {
            case WIKITEXT:
                results = extractDefiniensFromWikitext();
                break;
            case LATEX:
                results = extractDefiniensFromLaTeX();
                break;
            default:
                throw new IllegalCallerException("Unable extract relations for the given content type.");
        }
        return results;
    }

    private MLPDependencyGraph extractDefiniensFromWikitext() {
        BaseConfig config = new BaseConfig();
        RawWikiDocument document = new RawWikiDocument(context);

        MLPDependencyGraph graph = new MLPDependencyGraph();
        DocumentMetaLib metaLib = new DocumentMetaLib(graph);

        WikiTextAnnotatorMapper annotator = new WikiTextAnnotatorMapper(config);
        annotator.open(null);

        ParsedWikiDocument parsedWikiDocument = annotator.parse(document, metaLib);
        CreateCandidatesMapper mlp = new CreateCandidatesMapper(config);
        mlp.moiMapping(parsedWikiDocument);
        return graph;
    }

    private MLPDependencyGraph extractDefiniensFromLaTeX() {
        throw new IllegalCallerException("LaTeX format is not yet supported");
    }
}
