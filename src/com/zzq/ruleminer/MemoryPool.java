package com.zzq.ruleminer;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.zzq.ruleminer.KB.Instantiator;
import com.zzq.ruleminer.RuleGraph.Edge;
import com.zzq.ruleminer.RuleGraph.Node;

public class MemoryPool {
    final static int MAX_NODE_NUM = 12;
    private static MemoryPool memoryPool = new MemoryPool();
    //private static ArrayList<Node> nodeList = new ArrayList<Node>();
    //private static ArrayList<Edge> edgeList = new ArrayList<Edge>();
    private Node[] nodePool;
    private Edge[] edgePool;
    private Collection<Rule> ruleCollection;
    private Map<Instantiator, Int> instyMap = new HashMap<Instantiator, Int>();
    private List<Instantiator> instyList = new LinkedList<Instantiator>();
    private InstantiatorNode instyHead;
    private int instySize = 0;
    private Object instySync = new Object();
    private int enlargeSize = 10;
    public int count = 0;
    
    class InstantiatorNode {
        Instantiator data;
        InstantiatorNode parent = null;
        InstantiatorNode lchild = null;
        InstantiatorNode rchild = null;
        public InstantiatorNode() {
            data = new Instantiator();
            data.frame = this;
        }
    }
    
    private MemoryPool() {
        init();
    }
    
    public static MemoryPool getInstance() {
        return memoryPool;
    }
    
    private void init() {
        nodePool = new Node[MAX_NODE_NUM];
        edgePool = new Edge[MAX_NODE_NUM * MAX_NODE_NUM];
        for (int i=0; i<nodePool.length; i++)
            nodePool[i] = new Node();
        for (int i=0; i<edgePool.length; i++)
            edgePool[i] = new Edge();
        ruleCollection = new LinkedHashSet<Rule>();
        instyHead = new InstantiatorNode();
        instySize = 1;
    }
    
    public Node[] getNodeList(int sz) {
        for (int i=0; i<sz; i++) {
          nodePool[i].init();
        }
        return nodePool;
    }
    
    public Edge[] getEdgeList(int sz) {
        for (int i=0; i<sz; i++) {
            edgePool[i].init();
        }
        return edgePool;
    }
    
    public Collection<Rule> getRuleCollection() {
        return ruleCollection;
    }
    
    public Instantiator getInstantiator() {
        count++;
        return new Instantiator();
//        
//        InstantiatorNode res = null;
//        synchronized (instySync) {
//            if (instyHead.data.used) {
//                InstantiatorNode tmp1 = new InstantiatorNode();
//                InstantiatorNode tmp2 = new InstantiatorNode();
//                tmp1.lchild = tmp2;
//                tmp1.rchild = instyHead;
//                tmp2.parent = tmp1;
//                instyHead.parent = tmp1;
//                instyHead = tmp1;
//                addTree(tmp2, instySize - 1);
//                instySize = instySize * 2 + 1;
//            }
//            res = instyHead;
//            while (true) {
//                if (res.rchild == null) {
//                    break;
//                }
//                if (res.rchild.data.used) {
//                    if (res.lchild.data.used) {
//                        break;
//                    } else {
//                        res = res.lchild;
//                    }
//                } else {
//                    res = res.rchild;
//                }
//            }
//        }
//        res.data.used = true;
//        return res.data;
    }
    
    public void addTree(InstantiatorNode obj, int num) {
        if (num == 0) {
            return;
        }
        obj.lchild = new InstantiatorNode();
        obj.rchild = new InstantiatorNode();
        obj.lchild.parent = obj;
        obj.rchild.parent = obj;
        addTree(obj.lchild, num / 2 - 1);
        addTree(obj.rchild, num / 2 - 1);
    }
    
    public void removeInstantiator(Instantiator insty) {
//        instyMap.get(insty).value = 0;
//        synchronized (instySync) {
//            insty.used = false;
//            InstantiatorNode now = (InstantiatorNode) insty.frame;
//            InstantiatorNode free = now;
//            while (free!=null && free.data.used) {
//                free = free.parent;
//            }
//            Instantiator tmp = now.data;
//            now.data = free.data;
//            free.data = tmp;
//        }
    }
    
    public static void main(String[] args) {
//        long t1 = System.currentTimeMillis();
//        for (int i=0; i<10000000; i++) {
//            Instantiator it = new Instantiator();
//        }
//        long t2 = System.currentTimeMillis();
//        for (int i=0; i<10000000; i++) {
//            MemoryPool.getInstance().getInstantiator();
//        }
//        long t3 = System.currentTimeMillis();
//        System.out.println(t2 - t1);
//        System.out.println(t3 - t2);
//        int a = 1;
//        a++;
    }
}
