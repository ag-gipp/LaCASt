package gov.nist.drmf.interpreter.maple.translation.components;

import com.maplesoft.externalcall.MapleException;
import com.maplesoft.openmaple.Algebraic;
import com.maplesoft.openmaple.List;
import com.maplesoft.openmaple.MString;
import gov.nist.drmf.interpreter.common.GlobalConstants;
import gov.nist.drmf.interpreter.common.TranslationException;
import gov.nist.drmf.interpreter.common.grammar.Brackets;
import gov.nist.drmf.interpreter.common.symbols.Constants;
import gov.nist.drmf.interpreter.common.symbols.GreekLetters;
import gov.nist.drmf.interpreter.maple.grammar.MapleInternal;
import gov.nist.drmf.interpreter.maple.grammar.TranslatedExpression;
import gov.nist.drmf.interpreter.maple.grammar.TranslatedList;
import gov.nist.drmf.interpreter.maple.translation.MapleInterface;

/**
 * Created by AndreG-P on 28.02.2017.
 */
public class FunctionAndVariableTranslator extends ListTranslator {
    FunctionAndVariableTranslator( MapleInternal internal, int length ){
        super( internal, length );
    }

    @Override
    public boolean translate( List list ) throws TranslationException, MapleException {
        boolean b;
        switch ( root ){
            case string:
            case name:
                b = parseString( list );
                LOG.trace( "Translated " + root + ". " + translatedList.getLastExpression() );
                return b;
            case ass_name:
                Algebraic a = list.select(2);
                String msg = "AssignedNames are not allowed in this program. " +
                        "To find this here, means you previously defined the " +
                        "name of the object. " + a.toString() + ". But this is not allowed!";
                LOG.warn(msg);
                failures.addFailure( msg, this.getClass(), list.toString() );
                return false;
            case function:
                failures.addFailure( "Cannot translate functions yet.", this.getClass(), list.toString() );
                LOG.warn("Cannot translate functions yet.");
                return false;
            case power:
                parsePower( list );
                return true;
            default:
                failures.addFailure( "Wrong Parser for given element.", this.getClass(), list.toString() );
                LOG.debug("Cannot translate " + root + " in FunctionAndVariableTranslator.");
                return false;
        }
    }

    private boolean parseString( List list ) throws MapleException {
        Algebraic a = list.select(2);
        if ( !(a instanceof MString) ){
            failures.addFailure( "Expecting an MString!", this.getClass(), a.toString() );
            return false;
        }

        // get string value
        MString ms = (MString)a;
        String str = ms.stringValue();

        // this string could be a greek letter or a constant.
        TranslatedExpression t;
        MapleInterface mi = MapleInterface.getUniqueMapleInterface();
        GreekLetters greek = mi.getGreekTranslator();
        Constants constants = mi.getConstantsTranslator();

        // TODO additional information here!
        // first looking for constants
        String constant = constants.translate( str );
        if ( constant != null )
            t = new TranslatedExpression(constant);
        else { // second looking for greek letters
            String greekResult = greek.translate( str );
            if ( greekResult != null )
                t = new TranslatedExpression(greekResult);
            else t = new TranslatedExpression(str);
        }

        // put the translation into the list
        translatedList.addTranslatedExpression( t );
        return true;
    }

    private void parsePower( List list ) throws TranslationException, MapleException {
        List base = (List)list.select(2);
        List exponent = (List)list.select(3);

        TranslatedList trans_base = translateGeneralExpression( base );
        if ( trans_base.getLength() > 1 )
            trans_base.embrace();

        TranslatedList trans_exponent = translateGeneralExpression( exponent );
        trans_exponent.embrace( Brackets.left_braces );

        translatedList.addTranslatedExpression( trans_base );
        translatedList.addTranslatedExpression( GlobalConstants.CARET_CHAR );
        translatedList.addTranslatedExpression( trans_exponent );
        LOG.trace("Translated POWER. " + trans_base + GlobalConstants.CARET_CHAR + trans_exponent);
    }
}
