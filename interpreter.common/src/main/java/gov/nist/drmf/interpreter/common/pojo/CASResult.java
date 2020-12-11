package gov.nist.drmf.interpreter.common.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Andre Greiner-Petter
 */
public class CASResult {
    @JsonProperty("translation")
    private final String casRepresentation;

    @JsonProperty("numericResults")
    private NumericResult numericResults;

    @JsonProperty("symbolicResults")
    private final List<SymbolicCalculation> symbolicResults;

    public CASResult(String casRepresentation) {
        this.casRepresentation = casRepresentation;
        this.symbolicResults = new LinkedList<>();
    }

    public String getCasRepresentation() {
        return casRepresentation;
    }

    public NumericResult getNumericResults() {
        return numericResults;
    }

    public void setNumericResults(NumericResult numericResults) {
        this.numericResults = numericResults;
    }

    public List<SymbolicCalculation> getSymbolicResults() {
        return symbolicResults;
    }

    public void addSymbolicResult(SymbolicCalculation symbolicCalculation) {
        this.symbolicResults.add(symbolicCalculation);
    }

    public void addSymbolicResult(Collection<SymbolicCalculation> symbolicCalculation) {
        this.symbolicResults.addAll(symbolicCalculation);
    }
}
