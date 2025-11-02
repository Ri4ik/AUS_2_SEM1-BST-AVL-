/* ========================= com/mycompany/bst_du/BST.java ========================= */
package com.mycompany.bst_du;

import java.util.*;
import java.util.function.Consumer;

/**
 * Generic, non-recursive BST with height bookkeeping (for AVL subclass).
 */
public class BST<N extends EntityNode<N>> {

    /** SK: sprístupnené pre AVL; obsahuje aj výšku kvôli neskoršiemu rebalance. */
    protected static class TreeNode<N extends EntityNode<N>> {
        N key;
        TreeNode<N> left, right;
        int height = 1; // SK: null deti majú implicitne h=0 → list má 1

        TreeNode(N key) { this.key = key; }
    }

    /** SK: protected, aby AVL mohol meniť koreň pri rotáciách. */
    protected TreeNode<N> root;
    protected  int size;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }
    public void clear() { root = null; size = 0; }

    /* --------------------------- ITERATIVE INSERT --------------------------- */
    public boolean insert(N key) {
        if (key == null) throw new IllegalArgumentException("Null keys not allowed");
        if (root == null) { root = new TreeNode<>(key); size = 1; return true; }

        Deque<TreeNode<N>> parents = new ArrayDeque<>(64);
        TreeNode<N> cur = root;
        while (true) {
            parents.push(cur);
            int cmp = key.compareTo(cur.key);
            if (cmp < 0) {
                if (cur.left == null) { cur.left = new TreeNode<>(key); size++; parents.push(cur.left); break; }
                cur = cur.left;
            } else if (cmp > 0) {
                if (cur.right == null) { cur.right = new TreeNode<>(key); size++; parents.push(cur.right); break; }
                cur = cur.right;
            } else {
                // already present -> no insert
                return false;
            }
        }

        // SK: po vložení iba aktualizujeme výšky smerom hore (bez rotácií v čistom BST)
        TreeNode<N> prev = parents.pop(); // inserted node
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();
            updateHeight(p);
            prev = p;
        }
        return true;
    }

    /* --------------------------- ITERATIVE FIND/CONTAINS --------------------------- */
    public N find(N key) {
        TreeNode<N> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.key;
            cur = (cmp < 0) ? cur.left : cur.right;
        }
        return null;
    }
    public boolean contains(N key) { return find(key) != null; }

    /* --------------------------- ITERATIVE DELETE --------------------------- */
    public boolean delete(N key) {
        if (root == null || key == null) return false;

        Deque<TreeNode<N>> parents = new ArrayDeque<>(64);
        TreeNode<N> cur = root;
        TreeNode<N> parent = null;

        // find node
        while (cur != null && key.compareTo(cur.key) != 0) {
            parents.push(cur);
            parent = cur;
            int cmp = key.compareTo(cur.key);
            cur = (cmp < 0) ? cur.left : cur.right;
        }
        if (cur == null) return false; // not found

        // case: two children -> swap with inorder successor
        if (cur.left != null && cur.right != null) {
            parents.push(cur);
            TreeNode<N> succParent = cur;
            TreeNode<N> succ = cur.right;
            while (succ.left != null) {
                parents.push(succ);
                succParent = succ;
                succ = succ.left;
            }
            // SK: výmenný trik — meníme len hodnotu, štruktúru zatiaľ nie
            cur.key = succ.key;
            parent = succParent;
            cur = succ; // fyzicky odstránime nástupcu (má nanajvýš 1 dieťa)
        }

        // now cur has at most one child
        TreeNode<N> repl = (cur.left != null) ? cur.left : cur.right;

        if (parents.isEmpty()) {
            // deleting root
            root = repl;
            size--;
            // SK: aktualizujeme výšky smerom nahor — nič nad koreňom nie je
            if (root != null) updateHeight(root);
            return true;
        } else {
            TreeNode<N> p = parents.peek();
            if (p.left == cur) p.left = repl;
            else p.right = repl;
            size--;
        }

        // SK: iteratívne aktualizácie výšok na ceste hore
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();
            updateHeight(p);
        }
        return true;
    }

    /* --------------------------- MIN / MAX (iterative) --------------------------- */
    public N min() { TreeNode<N> m = minNode(root); return (m == null) ? null : m.key; }
    public N max() { TreeNode<N> m = maxNode(root); return (m == null) ? null : m.key; }

    protected TreeNode<N> minNode(TreeNode<N> node) {
        if (node == null) return null;
        while (node.left != null) node = node.left;
        return node;
    }
    protected TreeNode<N> maxNode(TreeNode<N> node) {
        if (node == null) return null;
        while (node.right != null) node = node.right;
        return node;
    }

    /* --------------------------- HEIGHT (O(1) по корню) --------------------------- */
    public int height() { return (root == null) ? -1 : root.height - 1; } // SK: prázdne = -1; list = 0

    /* --------------------------- ITERATIVE TRAVERSALS --------------------------- */
    public java.util.List<N> inOrder() {
        java.util.List<N> out = new java.util.ArrayList<>();
        Deque<TreeNode<N>> st = new ArrayDeque<>();
        TreeNode<N> cur = root;
        while (cur != null || !st.isEmpty()) {
            while (cur != null) { st.push(cur); cur = cur.left; }
            TreeNode<N> n = st.pop();
            out.add(n.key);
            cur = n.right;
        }
        return out;
    }

    public java.util.List<N> preOrder() {
        java.util.List<N> out = new java.util.ArrayList<>();
        if (root == null) return out;
        Deque<TreeNode<N>> st = new ArrayDeque<>();
        st.push(root);
        while (!st.isEmpty()) {
            TreeNode<N> n = st.pop();
            out.add(n.key);
            if (n.right != null) st.push(n.right);
            if (n.left  != null) st.push(n.left);
        }
        return out;
    }

    public java.util.List<N> postOrder() {
        java.util.List<N> out = new java.util.ArrayList<>();
        if (root == null) return out;
        Deque<TreeNode<N>> s1 = new ArrayDeque<>(), s2 = new ArrayDeque<>();
        s1.push(root);
        while (!s1.isEmpty()) {
            TreeNode<N> n = s1.pop();
            s2.push(n);
            if (n.left != null) s1.push(n.left);
            if (n.right != null) s1.push(n.right);
        }
        while (!s2.isEmpty()) out.add(s2.pop().key);
        return out;
    }

    /* --------------------------- helpers --------------------------- */
    protected static <N extends EntityNode<N>> int h(TreeNode<N> x) { return (x == null) ? 0 : x.height; }
    protected static <N extends EntityNode<N>> void updateHeight(TreeNode<N> x) {
        if (x == null) return;
        x.height = 1 + Math.max(h(x.left), h(x.right));
    }
}