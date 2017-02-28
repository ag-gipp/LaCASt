package gov.nist.drmf.interpreter.maple;

import com.maplesoft.externalcall.MapleException;
import gov.nist.drmf.interpreter.common.Keys;
import gov.nist.drmf.interpreter.maple.parser.MapleInterface;
import gov.nist.drmf.interpreter.maple.setup.Initializer;

import java.io.IOException;

/**
 * Created by AndreG-P on 17.02.2017.
 */
public class MapleToSemanticInterpreter {

    public static void main (String[] args){
        if ( Initializer.loadMapleNatives() )
            System.out.println("Loading Maple Natives!");
        else {
            System.out.println("Cannot load maple native directory.");
            return;
        }


        try {
            MapleInterface imaple = new MapleInterface();
            imaple.init();
            String result = imaple.parse( "(infinity+Catalan/2)^gamma" );
            System.out.println("Translated to: " + result);
            System.out.println("ErrorLOG: " + imaple.getInternalErrorLog());
        } catch ( MapleException | IOException me ){
            System.out.println("Well, Maple-Exception... nice shit.");
            me.printStackTrace();
        }
    }

}
