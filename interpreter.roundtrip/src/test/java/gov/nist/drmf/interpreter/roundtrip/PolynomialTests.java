package gov.nist.drmf.interpreter.roundtrip;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.LinkedList;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 * Created by AndreG-P on 06.03.2017.
 */
public class PolynomialTests extends AbstractRoundTrip {
    static final String[] test_polynomials = new String[]{
            "x-3-y-(z^3-4)",
            "(infinity+Catalan/2)^gamma",
            "gamma+alpha^5-I^(x/5)+Catalan",
            "alpha+2*beta+3*I-kappa/Theta+x*y^2.3",
            "x + x^2 - ((1-gamma)*x/2)^I",
            "1/((a+2)/(b^(Catalan/I)/(alpha*q^I*x/3)*alpha))",
            "((x^a)^b)^c",
            "(-(1/2*(1-gamma))*x)*(-I)",
            "gamma-(3*beta)/4-3*I+(kappa/(Theta^y+x*y^2.3))^(-I)"
    };

    static final String[] test_names = new String[]{
            "Simple Minus Sum Test",
            "Simple Constants Test",
            "Greek letters and Constants Test 1/2",
            "Greek letters and Constants Test 2/2",
            "Polynomial Test",
            "Complex Nested Fractions Test",
            "Nested Power Tests",
            "Tricky Negative Product",
            "Tricky Negative Fractions and Powers"
    };

    @TestFactory
    Iterable<DynamicTest> polynomialRoundTripTests(){
        return createFromMapleTestList( test_polynomials, test_names );
    }

    @Test
    void fixPointTest() {
        int threshold = 10;
        for ( String test : PolynomialTests.test_polynomials ){
            int c = 0;
            String tmp, last = "";
            LinkedList<String> latex_results = new LinkedList<>();
            LinkedList<String> maple_results = new LinkedList<>();
            boolean latex_equ = false, maple_equ = false;

            maple_results.add(test);

            try {
                while ( c < threshold && !(latex_equ && maple_equ) ){
                    tmp = translator.translateFromMapleToLaTeXClean( maple_results.getLast() );
                    latex_results.addLast( tmp );
                    if ( tmp.equals( last ) ) latex_equ = true;

                    last = maple_results.getLast();
                    tmp = translator.translateFromLaTeXToMapleClean( latex_results.getLast() );
                    maple_results.addLast( tmp );
                    if ( tmp.equals( last ) ) maple_equ = true;

                    last = latex_results.getLast();
                    c++;
                }

                assertTrue(
                        "Input expression and fix point are not equivalent! " + maple_results.getLast(),
                        translator.simplificationTester( test, maple_results.getLast() )
                );
            } catch ( Exception e ){
                fail( "Exception occurred!" );
            }
        }
    }
}
