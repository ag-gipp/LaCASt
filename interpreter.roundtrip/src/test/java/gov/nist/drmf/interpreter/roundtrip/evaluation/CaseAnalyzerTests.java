package gov.nist.drmf.interpreter.roundtrip.evaluation;

import gov.nist.drmf.interpreter.DLMFTranslator;
import gov.nist.drmf.interpreter.common.constants.Keys;
import gov.nist.drmf.interpreter.common.tests.AssumeMLPAvailability;
import gov.nist.drmf.interpreter.evaluation.Case;
import gov.nist.drmf.interpreter.evaluation.CaseAnalyzer;
import gov.nist.drmf.interpreter.evaluation.Relations;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Andre Greiner-Petter
 */
@AssumeMLPAvailability
public class CaseAnalyzerTests {
    private static DLMFTranslator dlmfTrans;

    @BeforeAll
    public static void setup() throws IOException {
        dlmfTrans = new DLMFTranslator(Keys.KEY_MAPLE);
    }

    @Test
    public void simpleTest() {
        String line = "\\Ln@@{z} = \\int_1^z \\frac{\\diff{t}}{t} \\constraint{$z\\neq 0$}, \\url{http://dlmf.nist.gov/1.2.E1}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("\\Ln@@{z}", c.getLHS());
        assertEquals("\\int_1^z \\frac{\\diff{t}}{t}", c.getRHS());
        assertEquals(Relations.EQUAL, c.getRelation());

        assertEquals("[z <> 0]", c.getConstraints(dlmfTrans, null).toString());
        assertEquals("http://dlmf.nist.gov/1.2.E1", c.getMetaData().getLabel().getTex());
    }

    @Test
    public void simpleTest2() {
        String line = "\\sum_{k=0}^n k > 1 \\source{(8.04), p.~414}{Olver:1997:ASF} \\keyphrase{z} \\keyphrase{y} \\url{http://dlmf.nist.gov/1.2.E1} \\ccode{AI}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("\\sum_{k=0}^n k", c.getLHS());
        assertEquals("1", c.getRHS());
        assertEquals(Relations.GREATER_THAN, c.getRelation());

        assertNull(c.getConstraints(dlmfTrans, null));
        assertEquals("http://dlmf.nist.gov/1.2.E1", c.getMetaData().getLabel().getTex());
    }

    @Test
    public void multiTest() {
        String line = "\\sum_{k=0}^n k > 1 > 0 \\url{tmp} \\url{http://dlmf.nist.gov/1.2.E1}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("\\sum_{k=0}^n k", c.getLHS());
        assertEquals("1", c.getRHS());
        assertEquals(Relations.GREATER_THAN, c.getRelation());

        assertNull(c.getConstraints(dlmfTrans, null));
        assertEquals("http://dlmf.nist.gov/1.2.E1", c.getMetaData().getLabel().getTex());

        c = cc.get(1);
        assertEquals("1", c.getLHS());
        assertEquals("0", c.getRHS());
        assertEquals(Relations.GREATER_THAN, c.getRelation());
    }

    @Test
    public void pmTest() {
        String line = "\\pm 1 = - \\mp 1 \\url{http://dlmf.nist.gov/1.2.E1}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("+ 1", c.getLHS());
        assertEquals("- - 1", c.getRHS());
        assertEquals(Relations.EQUAL, c.getRelation());

        c = cc.get(1);
        assertEquals("- 1", c.getLHS());
        assertEquals("- + 1", c.getRHS());
        assertEquals(Relations.EQUAL, c.getRelation());
    }

    @Test
    public void errorTest() {
        String line = "\\sqrt{z^2} = \\begin{cases} z, & \\realpart@@{z} \\geq 0, -z, & \\realpart@@{z} \\leq 0. \\end{cases} \\label{eq:EF.PVEX} \\ccode{EF}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("\\sqrt{z^2}", c.getLHS());
        assertEquals(Relations.EQUAL, c.getRelation());
    }

    @Test
    public void equal0Test() {
        String line = "\\AiryAi@{z}+\\expe^{-2\\cpi\\iunit/3} \\AiryAi@{z\\expe^{-2\\cpi\\iunit/3}}+" +
                "\\expe^{2\\cpi\\iunit/3}\\AiryAi@{z\\expe^{2\\cpi\\iunit/3}}=0, " +
                "\\source{(8.03), p.~414}{Olver:1997:ASF} \\url{http://dlmf.nist.gov/1.2.E1} \\ccode{AI}";

        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(line, 1);
        Case c = cc.get(0);
        assertEquals("\\AiryAi@{z}+\\expe^{-2\\cpi\\iunit/3} \\AiryAi@{z\\expe^{-2\\cpi\\iunit/3}}+\\expe^{2\\cpi\\iunit/3}\\AiryAi@{z\\expe^{2\\cpi\\iunit/3}}", c.getLHS());
        assertEquals("0", c.getRHS());
        assertEquals(Relations.EQUAL, c.getRelation());

        assertNull(c.getConstraints(dlmfTrans, null));
        assertEquals("http://dlmf.nist.gov/1.2.E1", c.getMetaData().getLabel().getTex());
    }
}
