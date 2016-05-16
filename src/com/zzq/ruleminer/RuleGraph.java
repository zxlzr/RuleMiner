package com.zzq.ruleminer;

public class RuleGraph {
    final int DEFAULT_HASH_VALUE_IN = 775817;
    final int DEFAULT_HASH_VALUE_OUT = 581777;
    final int DEFAULT_HASH_VALUE_XOR = 536477;
    private int hashValue = 1;
    private Rule rule;
    private Node[] nodeList;
    private Edge[] edgeList;
    
    public static RuleGraph ruleGraph = new RuleGraph();
    
    public static class Node {
        public int in = 0;
        public String str;
        public int out = 0;
        public void init() {
            in = 0;
            out = 0;
        }
    }
    
    public static class Edge {
        public int hashValue = 1;
        public String str;
        public Node in = null;
        public Node out = null;
        public void init() {
            hashValue = 1;
            in = null;
            out = null;
        }
    }
    
    private RuleGraph() {
    }
    
    public RuleGraph init(Rule rule) {
        this.rule = rule;
        int sz = rule.getTriples().size();
        nodeList = MemoryPool.getInstance().getNodeList(sz * 2);
        edgeList = MemoryPool.getInstance().getEdgeList(sz);
        this.hashValue = 1;
        return this;
    }
    
    public static RuleGraph getInstance() {
        return ruleGraph;
    }
    
    public int calcHash() {
        int nodeNum = 0;
        int edgeNum = 0;
        for (int i=0; i<rule.getTriples().size(); i++) {
            String[] triple = rule.getTriples().get(i);
            Node n1 = null;
            Node n2 = null;
            for (int j=0; j<nodeNum; j++) {
                Node n = nodeList[j];
                if (n.str.equals(triple[0])) {
                    n1 = n;
                }
                if (n.str.equals(triple[2])) {
                    n2 = n;
                }
            }
            if (n1 == null) {
                n1 = nodeList[nodeNum++];
                n1.str = triple[0];
            }
            if (n2 == null) {
                n2 = nodeList[nodeNum++];
                n2.str = triple[2];
            }
            n1.out++;
            n2.in++;
            Edge edge = edgeList[edgeNum++];
            edge.str = triple[1];
            edge.in = n1;
            edge.out = n2;
            if (i == 0) {
                edge.hashValue *= 31;
            }
        }
        for (int i=0; i<edgeNum; i++) {
            Edge edge = edgeList[i];
            edge.hashValue += edge.in.in * DEFAULT_HASH_VALUE_IN;
            edge.hashValue += edge.in.out * DEFAULT_HASH_VALUE_OUT;
            edge.hashValue = edge.hashValue * 31 + edge.str.hashCode();
            edge.hashValue += edge.out.in * DEFAULT_HASH_VALUE_IN;
            edge.hashValue += edge.out.out * DEFAULT_HASH_VALUE_OUT;
//            edge.hashValue ^= DEFAULT_HASH_VALUE_XOR;
            this.hashValue += edge.hashValue;
        }
        return this.hashValue;
    }
    
    public static void main(String[] args) {
        Rule q = new Rule(new String[]{("?a") ,("<isCitizenOf>"), ("?b")}, 1);
        q.add(new String[]{("?d") ,("<isCitizenOf>"), ("?b")});
        q.add(new String[]{("?a") ,("<influences>"), ("?d")});
        Rule q2 = new Rule(new String[]{("?g") ,("<isCitizenOf>"), ("?b")}, 1);
        q2.add(new String[]{("?n") ,("<isCitizenOf>"), ("?b")});
        q2.add(new String[]{("?g") ,("<influences>"), ("?n")});
        @SuppressWarnings("unused")
        boolean a = q.equals(q2);
        @SuppressWarnings("unused")
        int c = 1;
        c++;
        
        int n = 1000000;
        long l1 = System.currentTimeMillis();
      for (int i=0; i<n; i++) {
          q.equals(q2);
    }
      long l2 = System.currentTimeMillis();
      System.out.println(l2 - l1);
//      l1 = System.currentTimeMillis();
//      
//        for (int i=0; i<n; i++) {
//          RuleGraphBak rg1 = new RuleGraphBak(q);
//          RuleGraphBak rg2 = new RuleGraphBak(q2);
//          boolean res = rg1.calcHash() == rg2.calcHash();
//        }
//  l2 = System.currentTimeMillis();
//  System.out.println(l2 - l1);
//      return hash1 == hash2;
    }
}
