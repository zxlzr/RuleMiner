package com.zzq.ruleminer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.zzq.ruleminer.MiningAssistant.ConfidenceMetric;

public class RuleMiner {
    
    private KB kb;
    private MiningAssistant miningAssistant;
    private int numThread = 1;
    private RuleConsumer ruleConsumer;
    
    public RuleMiner() {
        
    }
    
    public boolean init(String[] args) {        
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        @SuppressWarnings("static-access")
        Option headCoverageOpt = OptionBuilder.withArgName("min-head-coverage")
                .hasArg()
                .withDescription("Minimum head coverage. Default: 0.01")
                .create("minhc");

        @SuppressWarnings("static-access")
        Option maxDepthOpt = OptionBuilder.withArgName("max-depth")
                .hasArg()
                .withDescription("Maximum number of atoms in the antecedent and succedent of rules. "
                        + "Default: 3")
                .create("maxad");

        @SuppressWarnings("static-access")
        Option stdConfThresholdOpt = OptionBuilder.withArgName("min-std-confidence")
                .hasArg()
                .withDescription("Minimum standard confidence threshold. "
                        + "This value is not used for pruning, only for filtering of the results. Default: 0.0")
                .create("minc");

        @SuppressWarnings("static-access")
        Option pcaConfThresholdOpt = OptionBuilder.withArgName("min-pca-confidence")
                .hasArg()
                .withDescription("Minimum PCA confidence threshold. "
                        + "This value is not used for pruning, only for filtering of the results. "
                        + "Default: 0.0")
                .create("minpca");

        @SuppressWarnings("static-access")
        Option numThreadOpt = OptionBuilder.withArgName("numThread")
                .hasArg()
                .withDescription("numThread")
                .create("numThread");

        @SuppressWarnings("static-access")
        Option allowConstOpt = OptionBuilder.withArgName("allowConst")
                .hasArg()
                .withDescription("allowConst")
                .create("const");

        @SuppressWarnings("static-access")
        Option allowOpenOpt = OptionBuilder.withArgName("allowOpenedAtoms")
                .hasArg()
                .withDescription("allowOpenedAtoms")
                .create("open");

        options.addOption(stdConfThresholdOpt);
        options.addOption(pcaConfThresholdOpt);
        options.addOption(headCoverageOpt);
        options.addOption(maxDepthOpt);
        options.addOption(numThreadOpt);
        options.addOption(allowConstOpt);
        options.addOption(allowOpenOpt);
        
        CommandLine cli = null;

        try {
            cli = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println("Unexpected exception: " + e.getMessage());
            return false;
        }
        kb = new KB();
        String[] leftOverArgs = cli.getArgs();
        List<File> files = new ArrayList<File>();
        for (String str : leftOverArgs) {
            File file = new File(str);
            files.add(file);
        }
        if (files.isEmpty()) {
            System.out.println("Please add file(s)!");
            return false;
        }
        kb.loadFile(files);
        kb.init();
        
        miningAssistant = new MiningAssistant(kb);
        miningAssistant.setConfidenceMetric(ConfidenceMetric.StdConfidence);
        
        if (cli.hasOption("minhc")) {
            String minHeadCoverage = cli.getOptionValue("minhc");
            miningAssistant.setMinHeadCoverage(Double.parseDouble(minHeadCoverage));
        }

        if (cli.hasOption("minc")) {
            String minConfidenceStr = cli.getOptionValue("minc");
            miningAssistant.setMinStdConfidence(Double.parseDouble(minConfidenceStr));
        }

        if (cli.hasOption("minpca")) {
            String minicStr = cli.getOptionValue("minpca");
            miningAssistant.setMinPcaConfidence(Double.parseDouble(minicStr));
        }

        if (cli.hasOption("maxad")) {
            String maxDepthStr = cli.getOptionValue("maxad");
            miningAssistant.setMaxLen(Integer.parseInt(maxDepthStr));
        }

        if (cli.hasOption("numThread")) {
            numThread = Integer.parseInt(cli.getOptionValue("numThread"));
        }

        if (cli.hasOption("const")) {
            String thresholdConstMultiStr = cli.getOptionValue("const");
            miningAssistant.setAllowConstants(true);
            miningAssistant.setThresholdConstMulti(Integer.parseInt(thresholdConstMultiStr));
        }

        if (cli.hasOption("open")) {
            String thresholdOpenedMultiStr = cli.getOptionValue("open");
            miningAssistant.setAllowOpenedAtoms(true);
            miningAssistant.setThresholdOpenedMulti(Integer.parseInt(thresholdOpenedMultiStr));
        }
        
        try {
            ruleConsumer = new RuleConsumer(new FileOutputStream("output.csv"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public Collection<Rule> mining() {
        Collection<Rule> out = new LinkedHashSet<Rule>();
        Collection<Rule> q = null;
        if (miningAssistant.getAllowConstants()) {
            q = miningAssistant.getInitialAtomsWithInstantiator(10);
        } else {
            q = miningAssistant.getInitialAtoms(10);
        }
        
        ruleConsumer.outputInitInfo();
        
        MiningThread[] threads = new MiningThread[numThread];
        for (int i=0; i<numThread; i++) {
            threads[i] = new MiningThread(q, out);
        }
        for (int i=0; i<numThread; i++) {
            threads[i].start();
        }
        for (int i=0; i<numThread; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return out;
    }
    
    class MiningThread extends Thread {
        Collection<Rule> q;
        Collection<Rule> out;
        
        public MiningThread(Collection<Rule> seeds, Collection<Rule> output) {
            q = seeds;
            out = output;
        }
        
        @Override
        public void run() {
            while(true) {
                Rule r = null;
                synchronized (q) {
                    Iterator<Rule> iterator = q.iterator();
                    if (iterator.hasNext()) {
                        r = iterator.next();
                        iterator.remove();
                    } else {
                        break;
                    }
                }

                if(miningAssistant.acceptForOutput(r)) {
                    synchronized (out) {
                        out.add(r);
                        ruleConsumer.addRule(r);
                    }
                }
                
                if(!r.isPerfect() && r.length() < miningAssistant.getMaxLen()) {
                    Collection<Rule> R = miningAssistant.refine(r);
                    synchronized (q) {
                        q.addAll(R);
                    }
                }
            }
        }
    }
    
    class RuleConsumer {
        public Collection <Rule> closedRules = new LinkedHashSet<Rule>();
        public Collection <Rule> instantiatedRules = new LinkedHashSet<Rule>();
        public Collection <Rule> openedRules = new LinkedHashSet<Rule>();
        public OutputStream outputStream;
        public boolean outputForRealTime = true;
        
        public RuleConsumer(OutputStream outputStream) {
            this.outputStream = outputStream;
        }
        
        public void addRule(Rule rule) {
            if (rule.getOpened()) {
                openedRules.add(rule);
            } else if (rule.flag.equals("InstantiatedAtoms")) {
                instantiatedRules.add(rule);
            } else {
                closedRules.add(rule);
            }
            if (outputForRealTime) {
                System.out.println(rule.toString() + "," + rule.getStdConfidence() + "," + rule.getPcaConfidence() + "," + rule.getSupport() + "," + rule.flag);
            }
        }
        
        public void outputToFile() {
            try {
                for (Rule r : closedRules) {
                    outputStream.write((r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "\n").getBytes());
                }
                for (Rule r : instantiatedRules) {
                    outputStream.write((r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "\n").getBytes());
                }
                for (Rule r : openedRules) {
                    outputStream.write((r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "\n").getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void outputToStdout() {
            try {
                for (Rule r : closedRules) {
                    System.out.println(r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "," + r.flag);
                }
                for (Rule r : instantiatedRules) {
                    System.out.println(r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "," + r.flag);
                }
                for (Rule r : openedRules) {
                    System.out.println(r.toString() + "," + r.getStdConfidence() + "," + r.getPcaConfidence() + "," + r.getSupport() + "," + r.flag);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void outputInitInfo() {
            System.out.println("Using " + miningAssistant.getConfidenceMetric());
            System.out.println("Minimum StdConfidence Threshold: " + miningAssistant.getMinStdConfidence());
            System.out.println("Minimum PcaConfidence Threshold: " + miningAssistant.getMinPcaConfidence());
            System.out.println("Minimum HeadCoverage Threshold: " + miningAssistant.getMinHeadCoverage());
            System.out.println("Max Depth: " + miningAssistant.getMaxLen());
            System.out.println("num thread: " + numThread);
            System.out.println("allow const: " + miningAssistant.getAllowConstants());
            System.out.println("allow open: " + miningAssistant.getAllowOpenedAtoms());
            System.out.println("Rule\tStdConfidence\tPcaConfidence\tSupport");
        }
        
        public void outputResultInfo() {
            System.out.println("closedRules: " + closedRules.size());
            System.out.println("instantiatedRules: " + instantiatedRules.size());
            System.out.println("openedRules: " + openedRules.size());
        }
    }
    
    public static void main(String args[]) {
        RuleMiner ruleMiner = new RuleMiner();
        if (ruleMiner.init(args)) {
            System.out.println("Mining......Start");
            long t1 = System.currentTimeMillis();
//            ruleMiner.ruleConsumer.outputForRealTime = false;
            Collection<Rule> rules = ruleMiner.mining();
//            ruleMiner.ruleConsumer.outputToStdout();
            ruleMiner.ruleConsumer.outputResultInfo();
            ruleMiner.ruleConsumer.outputToFile();
            long t2 = System.currentTimeMillis();
            System.out.println("Mining......OK");
            System.out.println("Mined " + rules.size() + " rules");
            System.out.println("Total Time: " + (t2 - t1) / 1000.0 + " s");
        }
    }
}
