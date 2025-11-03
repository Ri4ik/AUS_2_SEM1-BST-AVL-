/* ========================= com/mycompany/bst_du/AvlIntAdapter.java ========================= */
package com.mycompany.bst_du;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** Adaptér Int→IntNode pre AVL: deleguje do vnútornej štruktúry, podporuje zoznam a rýchly Count. */
public final class AvlIntAdapter implements IntSetStructure {

    private final AVL<IntNode> avl = new AVL<>();

    @Override public boolean insert(int key)   { return avl.insert(new IntNode(key)); }
    @Override public boolean delete(int key)   { return avl.delete(new IntNode(key)); }
    @Override public boolean contains(int key) { return avl.contains(new IntNode(key)); }
    @Override public int     size()            { return avl.size(); }
    @Override public void    clear()           { avl.clear(); }

    @Override
    public int minOrSentinel() {
        IntNode m = avl.min();
        return (m == null) ? Integer.MIN_VALUE : m.getValue();
    }

    @Override
    public int maxOrSentinel() {
        IntNode m = avl.max();
        return (m == null) ? Integer.MAX_VALUE : m.getValue();
    }

    /** Zoznam ako polouzavretý interval [lo, hi). Deleguje na BST/AVL range s príznakmi. */
    @Override
    public List<Integer> rangeList(int lo, int hiExclusive) {
        if (lo >= hiExclusive) return List.of();
        var nodes = avl.range(new IntNode(lo), true, new IntNode(hiExclusive), false);
        List<Integer> out = new ArrayList<>(nodes.size());
        for (IntNode n : nodes) out.add(n.getValue());
        return out;
    }

    /** Rýchle počítanie bez alokovania zoznamu — ponechané pre pomocné merania. */
    @Override
    public long rangeCount(int lo, int hiExclusive) {
        long count = 0L;
        Deque<BST.TreeNode<IntNode>> st = new ArrayDeque<>();
        BST.TreeNode<IntNode> cur = avl.root;

        // SK: lower_bound(lo) — vybudujeme cestu k prvému uzlu s hodnotou >= lo
        while (cur != null) {
            int v = cur.key.getValue();
            if (v >= lo) { st.push(cur); cur = cur.left; }
            else         { cur = cur.right; }
        }
        // SK: in-order iterácia po hiExclusive (horná hranica je vylúčená)
        while (!st.isEmpty()) {
            BST.TreeNode<IntNode> n = st.pop();
            int v = n.key.getValue();
            if (v >= hiExclusive) break;      // SK: dosiahli sme hornú hranicu intervalu
            count++;
            // SK: krok na inorder-nástupcu: ľavý extrém pravého podstromu
            BST.TreeNode<IntNode> r = n.right;
            while (r != null) { st.push(r); r = r.left; }
        }
        return count;
    }
}
