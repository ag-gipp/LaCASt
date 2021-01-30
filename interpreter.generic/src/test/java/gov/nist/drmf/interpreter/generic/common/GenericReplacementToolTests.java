package gov.nist.drmf.interpreter.generic.common;

import gov.nist.drmf.interpreter.pom.SemanticMLPWrapper;
import gov.nist.drmf.interpreter.pom.common.meta.AssumeMLPAvailability;
import gov.nist.drmf.interpreter.pom.extensions.PrintablePomTaggedExpression;
import mlp.ParseException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Andre Greiner-Petter
 */
@AssumeMLPAvailability
public class GenericReplacementToolTests {
    private static SemanticMLPWrapper mlp = SemanticMLPWrapper.getStandardInstance();

    @Test
    void constantReplaceTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("i + e^{\\pi}");
        GenericReplacementTool replacementTool = new GenericReplacementTool(ppte);
        ppte = replacementTool.getSemanticallyEnhancedExpression();
        assertEquals("\\iunit + \\expe^{\\cpi}", ppte.getTexString());
    }

    @Test
    void iAsIndexTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("x_i");
        GenericReplacementTool replacementTool = new GenericReplacementTool(ppte);
        ppte = replacementTool.getSemanticallyEnhancedExpression();
        assertEquals("x_i", ppte.getTexString());
    }

    @Test
    void imReplacementTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("x + \\operatorname{Im} (z)");
        GenericConstantReplacer replacementTool = new GenericConstantReplacer(ppte);
        ppte = replacementTool.fixConstants();
        assertEquals("x + \\imagpart(z)", ppte.getTexString());
    }

    @Test
    void simpleDiffTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("\\int_0^1 x dx");
        GenericReplacementTool replacementTool = new GenericReplacementTool(ppte);
        ppte = replacementTool.getSemanticallyEnhancedExpression();
        assertEquals("\\int_0^1 x \\diff{x}", ppte.getTexString());
    }

    @Test
    void complexIntTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("\\int_{-1}^1 (1 - x)^{\\alpha} (1 + x)^{\\beta} \\JacobipolyP{\\alpha}{\\beta}{m}@{x} \\JacobipolyP{\\alpha}{\\beta}{n}@{x} dx");
        GenericReplacementTool replacementTool = new GenericReplacementTool(ppte);
        ppte = replacementTool.getSemanticallyEnhancedExpression();
        assertEquals("\\int_{-1}^1(1 - x)^{\\alpha}(1 + x)^{\\beta} \\JacobipolyP{\\alpha}{\\beta}{m}@{x} \\JacobipolyP{\\alpha}{\\beta}{n}@{x} \\diff{x}", ppte.getTexString());
    }

    @Test
    void derivTest() throws ParseException {
        PrintablePomTaggedExpression ppte = mlp.parse("P_{n}(z) = \\frac{1 }{2^n  n! } \\frac{d^n }{ d z^n }  ( z^2 - 1 )^n");
        GenericReplacementTool replacementTool = new GenericReplacementTool(ppte);
        ppte = replacementTool.getSemanticallyEnhancedExpression();
        assertEquals("P_{n}(z) = \\frac{1 }{2^n  n! } \\deriv [n]{ }{z}(z^2 - 1)^n", ppte.getTexString());
    }
}
