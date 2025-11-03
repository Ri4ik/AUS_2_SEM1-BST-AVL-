/* ========================= com/mycompany/bst_du/BST.java ========================= */
package com.mycompany.bst_du;

import java.util.*; 
import java.util.function.Function;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 * Všeobecný, nerekurzívny BST s evidenciou výšky (využíva podtrieda AVL).
 */
public class BST<N extends EntityNode<N>> {

    /** SK: sprístupnené pre AVL; uzol nesie aj výšku kvôli neskoršiemu rebalance. */
    protected static class TreeNode<N extends EntityNode<N>> {
        N key;
        TreeNode<N> left, right;
        int height = 1; // SK: null deti majú implicitne h=0 → list má 1

        TreeNode(N key) { this.key = key; }
    }

    /** SK: protected, aby AVL mohol meniť koreň pri rotáciách. */
    protected TreeNode<N> root;
    protected int size;

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
                // SK: kľúč už existuje → nevkladáme duplicitný prvok
                return false;
            }
        }

        // SK: po vložení len aktualizujeme výšky smerom hore (v čistom BST bez rotácií)
        parents.pop(); // vložený uzol
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();
            updateHeight(p);
        }
        return true;
    }

    /* --------------------------- ITERATIVE FIND/CONTAINS --------------------------- */
    /** SK: Vráti nájdený prvok (alebo null), nie iba boolean. */
    public N find(N key) {
        if (key == null) throw new IllegalArgumentException("Null keys not allowed");
        TreeNode<N> cur = root;
        while (cur != null) {
            int cmp = key.compareTo(cur.key);
            if (cmp == 0) return cur.key;
            cur = (cmp < 0) ? cur.left : cur.right;
        }
        return null;
    }

    /** SK: `contains` ponechávame pre pohodlie; primárne API je `find`/`range`. */
    public boolean contains(N key) { return find(key) != null; }

    /* --------------------------- ITERATIVE DELETE --------------------------- */
    public boolean delete(N key) {
        if (root == null || key == null) return false;

        Deque<TreeNode<N>> parents = new ArrayDeque<>(64);
        TreeNode<N> cur = root;
        TreeNode<N> parent = null;

        // SK: hľadanie uzla na odstránenie
        while (cur != null && key.compareTo(cur.key) != 0) {
            parents.push(cur);
            parent = cur;
            int cmp = key.compareTo(cur.key);
            cur = (cmp < 0) ? cur.left : cur.right;
        }
        if (cur == null) return false; // SK: nenašlo sa

        // SK: prípad dvoch detí → zámenný trik s inorder nástupcom
        if (cur.left != null && cur.right != null) {
            parents.push(cur);
            TreeNode<N> succParent = cur;
            TreeNode<N> succ = cur.right;
            while (succ.left != null) {
                parents.push(succ);
                succParent = succ;
                succ = succ.left;
            }
            // SK: meníme len hodnotu uzla, štruktúru zatiaľ nie
            cur.key = succ.key;
            parent = succParent;
            cur = succ; // SK: fyzicky odstránime nástupcu (má nanajvýš 1 dieťa)
        }

        // SK: v tomto bode má `cur` nanajvýš jedno dieťa
        TreeNode<N> repl = (cur.left != null) ? cur.left : cur.right;

        if (parents.isEmpty()) {
            // SK: mazanie koreňa
            root = repl;
            size--;
            if (root != null) updateHeight(root);
            return true;
        } else {
            TreeNode<N> p = parents.peek();
            if (p.left == cur) p.left = repl;
            else p.right = repl;
            size--;
        }

        // SK: iteratívne doaktualizovanie výšok po ceste nahor
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

    /* --------------------------- HEIGHT (O(1) na koreni) --------------------------- */
    public int height() { return (root == null) ? -1 : root.height - 1; } // SK: prázdny = -1; list = 0

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

    /* --------------------------- RANGE SEARCH (ITERATIVE) --------------------------- */

    /** SK: Uzavretý interval [lo, hi], in-order, bez rekurzie. */
    public List<N> range(N lo, N hi) {
        return range(lo, true, hi, true);
    }

    /** SK: Polouzavretý interval [lo, hiExclusive); vhodné pre benchmarky. */
    public List<N> rangeHalfOpen(N lo, N hiExclusive) {
        return range(lo, true, hiExclusive, false);
    }

    /**
     * SK: Iteratívne vyhľadávanie v intervale v štýle prednášok:
     *  1) lower_bound(lo): od koreňa si vybudujeme cestu k prvému uzlu s key >= lo;
     *  2) potom in-order iterujeme po hi (vrátane/vynechané podľa príznakov).
     *
     * Zložitosť: O(h + k), kde h je výška a k je počet vrátených prvkov.
     */
    public List<N> range(N lo, boolean loInc, N hi, boolean hiInc) {
        if (lo == null || hi == null) throw new IllegalArgumentException("Null bounds not allowed");
        if (lo.compareTo(hi) > 0) return Collections.emptyList();

        ArrayList<N> out = new ArrayList<>();
        Deque<TreeNode<N>> st = new ArrayDeque<>();
        TreeNode<N> cur = root;

        // === 1) lower_bound(lo): zásobník cesty k prvému uzlu s key >= lo ===
        while (cur != null) {
            int cmp = cur.key.compareTo(lo);
            if (cmp >= 0) { st.push(cur); cur = cur.left; }
            else          { cur = cur.right; }
        }

        // === 2) in-order od lower_bound po hi ===
        while (!st.isEmpty()) {
            TreeNode<N> n = st.pop();

            // SK: horná hranica
            int cHi = n.key.compareTo(hi);
            if (cHi > 0 || (cHi == 0 && !hiInc)) break;

            // SK: dolná hranica
            int cLo = n.key.compareTo(lo);
            boolean okLo = (cLo > 0) || (cLo == 0 && loInc);
            if (okLo) out.add(n.key);

            // SK: krok k inorder-nástupcovi: ľavý extrém pravého podstromu
            TreeNode<N> r = n.right;
            while (r != null) { st.push(r); r = r.left; }
        }
        return out;
    }

    /* --------------------------- helpers --------------------------- */
    protected static <N extends EntityNode<N>> int h(TreeNode<N> x) { return (x == null) ? 0 : x.height; }
    protected static <N extends EntityNode<N>> void updateHeight(TreeNode<N> x) {
        if (x == null) return;
        x.height = 1 + Math.max(h(x.left), h(x.right));
    }
    /* ========================================================================
       ==================== CSV SUPPORT (NO LIBRARIES) ========================
       ======================================================================== */

    /** SK: Escapuje pole (oddeľovač je ';'). */
    public static String csvEsc(String s){
        if (s == null) return "";
        boolean needQ = s.indexOf(';')>=0 || s.indexOf('"')>=0 || s.indexOf('\n')>=0 || s.indexOf('\r')>=0;
        String r = s.replace("\"", "\"\"");
        return needQ ? "\"" + r + "\"" : r;
    }

    /** SK: Odstraňuje escapovanie poľa. */
    public static String csvUnesc(String s){
        if (s == null || s.isEmpty()) return "";
        s = s.trim();
        if (s.length()>=2 && s.charAt(0)=='"' && s.charAt(s.length()-1)=='"') {
            String core = s.substring(1, s.length()-1);
            return core.replace("\"\"", "\"");
        }
        return s;
    }

    /** SK: Rozdelenie CSV riadku podľa ';' s podporou úvodzoviek a zdvojených úvodzoviek. */
    public static String[] csvSplit(String line){
        ArrayList<String> out = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i=0;i<line.length();i++){
            char c = line.charAt(i);
            if (inQ) {
                if (c=='"') {
                    if (i+1<line.length() && line.charAt(i+1)=='"') { sb.append('"'); i++; }
                    else inQ=false;
                } else sb.append(c);
            } else {
                if (c=='"') inQ=true;
                else if (c==';') { out.add(sb.toString()); sb.setLength(0); }
                else sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }

    /**
     * SK: Zápis stromu do CSV v poradí in-order.
     * @param file     cesta k súboru
     * @param encoder  funkcia (uzol) -> riadok bez konca riadku
     */
    public void exportCsv(Path file, Function<N, String> encoder) throws IOException {
        if (file == null) throw new IllegalArgumentException("file required");
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        try (BufferedWriter w = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {

            List<N> nodes = this.inOrder();
            for (N n : nodes) {
                if (n == null) continue;
                String line = encoder.apply(n);
                if (line != null && !line.isEmpty()) {
                    w.write(line);
                    w.write('\n');
                }
            }
        }
    }

    /**
     * SK: Čítanie CSV do stromu.
     * @param file         cesta k súboru
     * @param lineDecoder  funkcia (CSV riadok) -> hotový uzol N (alebo null ak sa riadok preskočí)
     * @param sorted       true: riadky sú vopred zotriedené podľa kľúča — použije sa O(n) zostavenie;
     *                     false: vkladanie po jednom (O(n log n)).
     * @param skipHeader   true: preskočiť prvý riadok (hlavičku)
     * @return počet úspešne importovaných záznamov
     */
    public int importCsv(Path file, Function<String, N> lineDecoder, boolean sorted, boolean skipHeader) throws IOException {
        if (file == null || !Files.exists(file)) return 0;

        if (!sorted) {
            int ok = 0;
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                if (skipHeader) r.readLine();
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    try {
                        N node = lineDecoder.apply(line);
                        if (node != null && this.insert(node)) ok++;
                    } catch (Exception ex) {
                        // SK: poškodený riadok preskočíme
                    }
                }
            }
            return ok;
        } else {
            ArrayList<N> entries = new ArrayList<>();
            try (BufferedReader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                if (skipHeader) r.readLine();
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;
                    try {
                        N node = lineDecoder.apply(line);
                        if (node != null) entries.add(node);
                    } catch (Exception ex) {
                        // SK: preskočiť chybný vstup
                    }
                }
            }
            if (entries.isEmpty()) return 0;
            buildBalancedFromSorted(entries);
            return entries.size();
        }
    }

    /** SK: Iteratívna stavba dokonale vyváženého BST zo zotriedeného zoznamu kľúčov (podľa compareTo). */
    protected void buildBalancedFromSorted(List<N> sorted) {
        if (sorted == null || sorted.isEmpty()) { clear(); return; }

        // SK: skladáme strom priamo z hotových TreeNode bez `insert` — O(n)
        this.root = null;
        this.size = sorted.size();

        class Task {
            int lo, hi;
            TreeNode<N> parent;
            boolean attachLeft;
            Task(int lo, int hi, TreeNode<N> parent, boolean attachLeft){
                this.lo=lo; this.hi=hi; this.parent=parent; this.attachLeft=attachLeft;
            }
        }

        int mid = sorted.size()/2;
        TreeNode<N> r = new TreeNode<>(sorted.get(mid));
        this.root = r;

        Deque<Task> st = new ArrayDeque<>();
        if (mid-1 >= 0) st.push(new Task(0, mid-1, r, true));
        if (mid+1 <= sorted.size()-1) st.push(new Task(mid+1, sorted.size()-1, r, false));

        while (!st.isEmpty()){
            Task t = st.pop();
            if (t.lo > t.hi) continue;
            int m = t.lo + (t.hi - t.lo)/2;
            TreeNode<N> node = new TreeNode<>(sorted.get(m));
            if (t.attachLeft) t.parent.left = node; else t.parent.right = node;

            if (m-1 >= t.lo) st.push(new Task(t.lo, m-1, node, true));
            if (m+1 <= t.hi) st.push(new Task(m+1, t.hi, node, false));
        }

        // SK: následný prepočet výšok v post-order (iteratívne)
        recomputeHeightsIterative();
    }

    /** SK: Iteratívny post-order na prepočet `height` pre všetky uzly. */
    protected void recomputeHeightsIterative() {
        if (root == null) return;
        Deque<TreeNode<N>> s1 = new ArrayDeque<>();
        Deque<TreeNode<N>> s2 = new ArrayDeque<>();
        s1.push(root);
        while (!s1.isEmpty()){
            TreeNode<N> n = s1.pop();
            s2.push(n);
            if (n.left  != null) s1.push(n.left);
            if (n.right != null) s1.push(n.right);
        }
        while (!s2.isEmpty()){
            TreeNode<N> n = s2.pop();
            updateHeight(n);
        }
    }
}
