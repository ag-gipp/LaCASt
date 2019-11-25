package gov.nist.drmf.interpreter.evaluation;

import gov.nist.drmf.interpreter.common.exceptions.ComputerAlgebraSystemEngineException;
import gov.nist.drmf.interpreter.common.exceptions.TranslationException;
import gov.nist.drmf.interpreter.common.grammar.IComputerAlgebraSystemEngine;
import gov.nist.drmf.interpreter.common.grammar.ITranslator;
import gov.nist.drmf.interpreter.constraints.Constraints;
import gov.nist.drmf.interpreter.constraints.IConstraintTranslator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Andre Greiner-Petter
 */
public abstract class AbstractEvaluator<T> {
    private static final Logger LOG = LogManager.getLogger(AbstractEvaluator.class.getName());

    public final static String NL = System.lineSeparator();

    private IConstraintTranslator forwardTranslator;
    private IComputerAlgebraSystemEngine<T> engine;

    private HashMap<String, Integer> missingMacrosLib;

    public static final Pattern filterCases = Pattern.compile(
            "\\\\(BigO|littleo|[fdc]Diff|asymp|sim|[lc]?dots)[^a-zA-Z]|" +
                    "\\{(cases|array|[bBvp]matrix|Matrix|Lattice)}|" +
                    "([fg](?:\\\\left)?\\()"
    );

    public AbstractEvaluator(
            IConstraintTranslator forwardTranslator,
            IComputerAlgebraSystemEngine<T> engine
    ) {
        this.forwardTranslator = forwardTranslator;
        this.engine = engine;
        this.missingMacrosLib = new HashMap<>();
    }

    public String forwardTranslate(String in) throws TranslationException {
        return forwardTranslator.translate(in);
    }

    public T enterEngineCommand(String cmd) throws ComputerAlgebraSystemEngineException {
        return engine.enterCommand(cmd);
    }

    public IConstraintTranslator getThisConstraintTranslator() {
        return forwardTranslator;
    }

    public void addMissingMacro(String macro) {
        if ( missingMacrosLib.containsKey(macro) ) {
            missingMacrosLib.put(macro, missingMacrosLib.get(macro)+1);
        } else {
            missingMacrosLib.put(macro, 1);
        }
    }

    public List<String> getOrderedMissingMacros() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<Map.Entry<String, Integer>>(
                missingMacrosLib.entrySet()
        );

        Collections.sort(entries, Comparator.comparingInt(Map.Entry::getValue));
        Collections.reverse(entries);

        LinkedList<String> output = new LinkedList<>();
        for ( Map.Entry<String, Integer> e : entries ) {
            output.addLast(e.getKey() + ", " + e.getValue());
        }

        return output;
    }

    public void forceGC() throws ComputerAlgebraSystemEngineException {
        this.engine.forceGC();
    }

    public abstract void performSingleTest(Case testCase);

    public void performAllTests(LinkedList<Case> testCases) {
        for ( Case test : testCases ) {
            performSingleTest(test);
        }
    }

    public abstract LinkedList<Case> loadTestCases();

    public LinkedList<Case> loadTestCases(
            int[] subset,
            Set<Integer> skipLines,
            Path dataset,
            HashMap<Integer, String> labelLib,
            HashMap<Integer, String> skippedLinesInfo
    ) {
        LinkedList<Case> testCases = new LinkedList<>();
        int[] currLine = new int[] {0};

        try (BufferedReader br = Files.newBufferedReader(dataset)) {
            Stream<String> lines = br.lines();

            int start = subset[0];
            int limit = subset[1];

            lines.sequential()
                    .peek(l -> currLine[0]++) // line counter
                    .filter(l -> start <= currLine[0] && currLine[0] < limit) // filter by limits
                    .filter(l -> {
                        if ( !skipLines.contains(currLine[0]) ) {
                            return true;
                        } else {
                            skippedLinesInfo.put(currLine[0], "Skipped - (user defined).");
                            Status.SKIPPED.add();
                            return false;
                        }
                    }) // skip entries if wanted
                    .flatMap(l -> {
                        if ( l.contains("comments{Warning") ) {
                            skippedLinesInfo.put(currLine[0], "Ignore - No semantic math.");
                            Status.IGNORE.add();
                            return null;
                        }

                        LinkedList<Case> cc = CaseAnalyzer.analyzeLine(l, currLine[0]);
                        if ( cc == null || cc.isEmpty() ) {
                            System.out.println(currLine[0] + ": unable to extract test case.");
                            skippedLinesInfo.put(currLine[0], "Skipped - Unable to analyze test case.");
                            Status.SKIPPED.add();
                            return null;
                        }

                        LinkedList<Case> testCC = new LinkedList<>();
                        String reason = "";
                        for ( Case ic : cc ) {
                            String test = ic.getLHS() + " " + ic.getRHS();
                            Matcher m = filterCases.matcher(test);
                            if ( !m.find() ) {
                                String conStr = ic.getRawConstraint();
                                if ( conStr != null && !conStr.isEmpty() ) {
                                    Matcher cm = filterCases.matcher(conStr);
                                    if ( cm.find() ) ic.removeConstraint();
                                }
                                testCC.add(ic);
                            } else {
                                LOG.warn("Ignore " + currLine[0] + " because: " + test);
                                if ( !reason.isEmpty() ) reason += ", ";

                                if ( m.group(1) != null ) reason += m.group(1);
                                else if ( m.group(2) != null ) reason += m.group(2);
                                else reason += "Generic function " + m.group(3);
                            }
                        }

                        if (!testCC.isEmpty()) {
                            Case c = testCC.get(0);
                            labelLib.put(c.getLine(), c.getDlmf());
                            return testCC.stream();
                        } else {
                            skippedLinesInfo.put(currLine[0], "Ignore - Invalid test case: " + reason);
                            Status.IGNORE.add();
                            return null;
                        }
                    })
                    .filter( Objects::nonNull )
                    .forEach(testCases::add);
            return testCases;
        } catch (IOException ioe) {
            LOG.fatal("Cannot load dataset!", ioe);
            return null;
        }
    }

    public abstract EvaluationConfig getConfig();
    public abstract HashMap<Integer, String> getLabelLibrary();
    public abstract LinkedList<String>[] getLineResults();

    private void writeMissingMacros() throws IOException {
        Path out = getConfig().getMissingMacrosOutputPath();
        Files.write(out, getOrderedMissingMacros());
    }

    public void writeResults() throws IOException {
        if ( getConfig().getMissingMacrosOutputPath() != null ) {
            writeMissingMacros();
        }

        String results = getResults(
                this.getConfig(),
                this.getLabelLibrary(),
                this.getLineResults()
        );
        Files.write(
                this.getConfig().getOutputPath(),
                results.getBytes()
        );
    }

    protected String getResults(
            EvaluationConfig config,
            HashMap<Integer, String> labelLib,
            LinkedList<String>[] lineResults
    ){
        StringBuffer sb = new StringBuffer();

        sb.append("Overall: ");
        sb.append(Status.buildString());
        sb.append(" for test expression: ");
        sb.append(config.getTestExpression());
        sb.append(NL);

        return buildResults(
                sb,
                labelLib,
                config.showDLMFLinks(),
                config.getSubSetInterval(),
                lineResults
        );
    }

    protected String buildResults(
            StringBuffer sb,
            HashMap<Integer, String> labelLib,
            boolean showDLMF,
            int[] limits,
            LinkedList<String>[] lineResults){
        int start = limits[0];
        int limit = limits[1];

        for ( int i = start; i < lineResults.length && i < limit; i++ ){
            if ( lineResults[i] == null ){
                sb.append(i).append(": Skipped (is null)").append(NL);
                return sb.toString();
            }

            LinkedList<String> tmp = lineResults[i];
            LinkedList<String> lineResult = new LinkedList();
            for ( String s : tmp ) {
                if ( !s.contains("Ignore") ) lineResult.add(s);
            }

            if ( lineResult.isEmpty() ) continue;

            sb.append(i);
            boolean first = true;
            Character c = 'a';

            for ( String s : lineResult ) {
                if ( !first ) {
                    sb.append(i+"-"+c);
                    c++;
                } else first = false;

                String dlmf = labelLib.get(i);
                if ( dlmf != null && showDLMF ){
                    sb.append(" [").append(dlmf).append("]: ");
                } else sb.append(": ");

                sb.append(s);
                sb.append(NL);
            }


        }
        return sb.toString();
    }
}
