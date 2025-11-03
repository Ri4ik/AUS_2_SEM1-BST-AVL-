/* ========================= com/mycompany/bst_du/AVL.java ========================= */
package com.mycompany.bst_du;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Nerekurzívne AVL postavené na BST.
 * Iteratívne vkladanie/mazanie s vyvažovaním zdola nahor.
 */
public class AVL<N extends EntityNode<N>> extends BST<N> {

    private int balance(TreeNode<N> x) {
        return (x == null) ? 0 : (h(x.left) - h(x.right));
    }

    /* --------------------------- ROTATIONS ---------------------------
       SK: Rotácie vracajú nový koreň daného podstromu. Po rotácii MUSÍ nasledovať
           updateHeight: najprv na „nižšom“ uzle, potom na novom koreni podstromu.
    ------------------------------------------------------------------ */
    protected TreeNode<N> rotateRight(TreeNode<N> x) {
        if (x == null || x.left == null) return x;
        TreeNode<N> y = x.left;
        TreeNode<N> beta = y.right;

        // SK: y ide hore, x sa posúva doprava, beta sa stáva ľavým synom x
        y.right = x;
        x.left = beta;

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    protected TreeNode<N> rotateLeft(TreeNode<N> x) {
        if (x == null || x.right == null) return x;
        TreeNode<N> y = x.right;
        TreeNode<N> beta = y.left;

        // SK: y ide hore, x sa posúva doľava, beta sa stáva pravým synom x
        y.left = x;
        x.right = beta;

        updateHeight(x);
        updateHeight(y);
        return y;
    }

    protected TreeNode<N> rotateLeftRight(TreeNode<N> x) {
        if (x == null) return null;
        x.left = rotateLeft(x.left);
        return rotateRight(x);
    }

    protected TreeNode<N> rotateRightLeft(TreeNode<N> x) {
        if (x == null) return null;
        x.right = rotateRight(x.right);
        return rotateLeft(x);
    }

    /* --------------------------- INSERT (ITERATIVE + REBALANCE) --------------------------- */
    @Override
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
                return false; // SK: duplicitný kľúč — nevkladáme
            }
        }

        // SK: „zdvih“: začíname od rodiča novo vloženého uzla (je navrchu zásobníka)
        parents.pop(); // inserted node
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();

            // SK: prepočet + prípadná rotácia
            updateHeight(p);
            int bal = balance(p);
            TreeNode<N> newSubRoot = p;
            if (bal > 1) {
                if (balance(p.left) >= 0) newSubRoot = rotateRight(p);              // LL
                else { p.left = rotateLeft(p.left); newSubRoot = rotateRight(p); }  // LR
            } else if (bal < -1) {
                if (balance(p.right) <= 0) newSubRoot = rotateLeft(p);              // RR
                else { p.right = rotateRight(p.right); newSubRoot = rotateLeft(p); }// RL
            }
            // SK: pripojenie nového koreňa podstromu k „dedovi“ (ak existuje)
            if (!parents.isEmpty()) {
                TreeNode<N> gp = parents.peek();
                if (gp.left == p) gp.left = newSubRoot; else if (gp.right == p) gp.right = newSubRoot;
            } else {
                root = newSubRoot; // SK: bez deda → aktualizujeme globálny koreň
            }
        }
        return true;
    }

    /* --------------------------- DELETE (ITERATIVE + REBALANCE) --------------------------- */
    @Override
    public boolean delete(N key) {
        if (root == null || key == null) return false;

        Deque<TreeNode<N>> parents = new ArrayDeque<>(64);
        TreeNode<N> cur = root;

        // SK: hľadanie uzla
        while (cur != null && key.compareTo(cur.key) != 0) {
            parents.push(cur);
            cur = (key.compareTo(cur.key) < 0) ? cur.left : cur.right;
        }
        if (cur == null) return false;

        // SK: ak má 2 deti → výmena s inorder nástupcom (vymieňame len hodnoty)
        if (cur.left != null && cur.right != null) {
            parents.push(cur);
            TreeNode<N> succ = cur.right;
            while (succ.left != null) {
                parents.push(succ);
                succ = succ.left;
            }
            // SK: zámenný trik — štruktúru nemeníme
            N tmp = cur.key; cur.key = succ.key; succ.key = tmp;
            cur = succ; // SK: odstránime práve 'succ' (má nanajvýš 1 dieťa)
        }

        // SK: fyzické odstránenie uzla s ≤ 1 dieťaťom
        TreeNode<N> repl = (cur.left != null) ? cur.left : cur.right;

        if (parents.isEmpty()) {
            // SK: odstraňuje sa koreň
            root = repl;
            size--;
            if (root != null) updateHeight(root);
            return true;
        } else {
            TreeNode<N> p = parents.peek();
            if (p.left == cur) p.left = repl; else p.right = repl;
            size--;
        }

        // SK: vyvažovanie zdola nahor
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();

            updateHeight(p);
            int bal = (h(p.left) - h(p.right));

            TreeNode<N> newSubRoot = p;   // SK: default — bez rotácie
            if (bal > 1) {
                if ((h(p.left.left) - h(p.left.right)) >= 0) {
                    newSubRoot = rotateRight(p);                  // LL
                } else {
                    p.left = rotateLeft(p.left);                  // LR
                    newSubRoot = rotateRight(p);
                }
            } else if (bal < -1) {
                if ((h(p.right.left) - h(p.right.right)) <= 0) {
                    newSubRoot = rotateLeft(p);                   // RR
                } else {
                    p.right = rotateRight(p.right);               // RL
                    newSubRoot = rotateLeft(p);
                }
            }

            // SK: pripojenie k „dedovi“ alebo aktualizácia koreňa
            if (!parents.isEmpty()) {
                TreeNode<N> gp = parents.peek();
                if (gp.left == p) gp.left = newSubRoot;
                else if (gp.right == p) gp.right = newSubRoot;
            } else {
                root = newSubRoot;
            }
        }
        return true;
    }

    /* --------------------------- RANGE SEARCH (EXPLICIT WRAPPERS) ---------------------------
       SK: Funkčne to nie je nutné (dedené z BST), no ponechávame explicitné metódy:
           - budú priamo viditeľné v API AVL,
           - uľahčia autocompletion/vyhľadávanie,
           - držíme jednotný kontrakt s BST.
    ------------------------------------------------------------------ */

    /** SK: Intervalové vyhľadávanie: obe hranice vrátane. Vráti in-order zoznam prvkov. */
    public List<N> range(N lo, N hi) {
        return super.range(lo, hi);
    }

    /** SK: Intervalové vyhľadávanie s nastavením inkluzivity hraníc. */
    public List<N> range(N lo, boolean loInc, N hi, boolean hiInc) {
        return super.range(lo, loInc, hi, hiInc);
    }
}
