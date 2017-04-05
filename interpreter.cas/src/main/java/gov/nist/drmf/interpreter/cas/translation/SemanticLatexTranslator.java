package gov.nist.drmf.interpreter.cas.translation;

import gov.nist.drmf.interpreter.common.InformationLogger;
import gov.nist.drmf.interpreter.cas.logging.TranslatedExpression;
import gov.nist.drmf.interpreter.common.GlobalConstants;
import gov.nist.drmf.interpreter.common.GlobalPaths;
import gov.nist.drmf.interpreter.common.Keys;
import gov.nist.drmf.interpreter.common.TranslationException;
import gov.nist.drmf.interpreter.common.symbols.BasicFunctionsTranslator;
import gov.nist.drmf.interpreter.common.symbols.Constants;
import gov.nist.drmf.interpreter.common.symbols.GreekLetters;
import gov.nist.drmf.interpreter.common.symbols.SymbolTranslator;
import gov.nist.drmf.interpreter.mlp.extensions.MacrosLexicon;
import mlp.ParseException;
import mlp.PomParser;
import mlp.PomTaggedExpression;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.file.Path;

/**
 * This translation translate semantic LaTeX formula using
 * the math processor language by Abdou Youssef.
 * It based on BNF grammar programmed with JavaCC.
 *
 * It is the top level translation objects. That means
 * you can use {@link #translate(String)} to translate an
 * expression in general. To do so, you have to
 * invoke {@link #init(Path)} before you use this
 * translate method. On the other hand this translation can
 * handle also general PomTaggedExpression to translate.
 * @see PomTaggedExpression
 *
 * @author Andre Greiner-Petter
 */
public class SemanticLatexTranslator extends AbstractTranslator {
    public static String TAB = "";

    private static GreekLetters greekLetters;
    private static Constants constants;
    private static BasicFunctionsTranslator functions;
    private static SymbolTranslator symbols;

    private PomParser parser;

    public SemanticLatexTranslator(String from_language, String to_language ){
        greekLetters = new GreekLetters(from_language, to_language);
        constants = new Constants(Keys.KEY_DLMF, to_language);
        functions = new BasicFunctionsTranslator(to_language);
        symbols = new SymbolTranslator(from_language, to_language);

        INFO_LOG = new InformationLogger();
        LOG = LogManager.getLogger( SemanticLatexTranslator.class.toString() );
        //LOG.setLevel(Level.WARNING);

        global_exp = new TranslatedExpression();
        int length = GlobalConstants.CAS_KEY.length()+1 > "DLMF: ".length() ?
                (GlobalConstants.CAS_KEY.length()+2) : "DLMF: ".length();
        for ( int i = 0; i <= length; i++ )
            TAB += " ";
    }

    /**
     * Initializes the back end for the translation from semantic LaTeX to
     * a computer algebra system. It loads all translation information
     * from the files in the given path and instantiate the PomParser from
     * Prof. Abdou Youssef.
     * @param reference_dir_path the path to the ReferenceData directory.
     *                           You can find the path in
     *                           {@link GlobalPaths#PATH_REFERENCE_DATA}.
     * @throws IOException if it is not possible to read the information
     *                      from the files.
     */
    public void init( Path reference_dir_path ) throws IOException {
        greekLetters.init();
        constants.init();
        functions.init();
        symbols.init();

        MacrosLexicon.init();

        MULTIPLY = symbols.translateFromMLPKey( Keys.MLP_KEY_MULTIPLICATION );

        parser = new PomParser(reference_dir_path.toString());
        parser.addLexicons( MacrosLexicon.getDLMFMacroLexicon() );
    }

    /**
     *
     * @param expression
     * @return
     */
    public boolean translate( String expression ) throws TranslationException {
        if ( expression == null || expression.isEmpty() ) return false;
        try {
            PomTaggedExpression exp = parser.parse(expression);
            return translate(exp);
        } catch ( ParseException pe ){
            throw new TranslationException( Keys.KEY_LATEX, GlobalConstants.CAS_KEY, pe.getMessage(), pe );
        }
    }

    @Override
    public boolean translate( PomTaggedExpression expression ) throws TranslationException {
        reset();
        local_inner_exp.addTranslatedExpression(
                parseGeneralExpression(expression, null).getTranslatedExpression()
        );
        if ( isInnerError() ){
            throw new TranslationException(
                    Keys.KEY_LATEX,
                    GlobalConstants.CAS_KEY,
                    "Wasn't able to translate the given expression."
            );
        }
        return true;
    }

    public static GreekLetters getGreekLettersParser(){
        return greekLetters;
    }

    public static Constants getConstantsParser(){
        return constants;
    }

    public static BasicFunctionsTranslator getBasicFunctionParser(){
        return functions;
    }

    public static SymbolTranslator getSymbolsTranslator(){
        return symbols;
    }

    public InformationLogger getInfoLog(){
        return INFO_LOG;
    }
}
