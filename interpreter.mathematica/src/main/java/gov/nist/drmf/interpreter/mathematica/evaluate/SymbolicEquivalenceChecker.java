package gov.nist.drmf.interpreter.mathematica.evaluate;

import com.wolfram.jlink.Expr;
import com.wolfram.jlink.ExprFormatException;
import com.wolfram.jlink.KernelLink;
import com.wolfram.jlink.MathLinkException;
import gov.nist.drmf.interpreter.mathematica.common.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Andre Greiner-Petter
 */
public class SymbolicEquivalenceChecker {
    private static final Logger LOG = LogManager.getLogger(SymbolicEquivalenceChecker.class.getName());

    private final KernelLink engine;

    public SymbolicEquivalenceChecker( KernelLink engine ){
        this.engine = engine;
    }

    public boolean fullSimplifyDifference( String LHS, String RHS, String assumption ) throws MathLinkException {
        String eq = LHS + " - (" + RHS + ")";
        Expr ex = fullSimplify(eq, assumption);
        return testZero(ex);
    }

    public boolean fullSimplifyDifference( String LHS, String RHS ) throws MathLinkException {
        String eq = LHS + " - (" + RHS + ")";
        Expr ex = fullSimplify(eq);
        return testZero(ex);
    }

    public Expr fullSimplify(String test) throws MathLinkException {
        return fullSimplify(test, null);
    }

    public Expr fullSimplify(String test, String assumption) throws MathLinkException {
//        String simplify = Commands.FULL_SIMPLIFY.build(test);
        String expr = assumption == null ?
                Commands.FULL_SIMPLIFY.build(test) :
                Commands.FULL_SIMPLIFY_ASSUMPTION.build(test, assumption);

        engine.evaluate(expr);
        engine.waitForAnswer();

        return engine.getExpr();
    }

    public boolean testZero(Expr expr) throws MathLinkException {
//        Expr expr = engine.getExpr();
        if ( expr.numberQ() ) {
            try {
                double d = expr.asDouble();
                return d == 0;
            } catch (ExprFormatException e) {
                LOG.info("Not equal! " + expr.toString());
            }
        }
        return false;
    }

    public void abort() {
        LOG.warn("Register an abortion request. Call abort evaluation now!");
        engine.abortEvaluation();
    }
}
