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
                // already present -> no insert
                return false;
            }
        }

        // SK: po vložení iba aktualizujeme výšky smerom hore (bez rotácií v čistom BST)
        parents.pop(); // inserted node
        while (!parents.isEmpty()) {
            TreeNode<N> p = parents.pop();
            updateHeight(p);
        }
        return true;
    }

    /* --------------------------- ITERATIVE FIND/CONTAINS --------------------------- */
    /** Возвращает сам найденный элемент (или null), а не boolean. */
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

    /** Оставляем contains для удобства, но основной API — find/range. */
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

    /* --------------------------- RANGE SEARCH (ITERATIVE) --------------------------- */

    /** Закрытый диапазон [lo, hi], in-order, без рекурсии. */
    public List<N> range(N lo, N hi) {
        return range(lo, true, hi, true);
    }

    /** Полуинтервал [lo, hiExclusive), удобно для бенчей. */
    public List<N> rangeHalfOpen(N lo, N hiExclusive) {
        return range(lo, true, hiExclusive, false);
    }

    /**
     * Итеративный диапазонный поиск в стиле лекций:
     *  1) lower_bound(lo): идём от корня, запоминая путь к первому узлу с key >= lo;
     *  2) затем in-order итерация до hi (включительно/исключительно по флагам).
     *
     * Сложность: O(h + k), где h — высота, k — число найденных элементов.
     */
    public List<N> range(N lo, boolean loInc, N hi, boolean hiInc) {
        if (lo == null || hi == null) throw new IllegalArgumentException("Null bounds not allowed");
        if (lo.compareTo(hi) > 0) return Collections.emptyList();

        ArrayList<N> out = new ArrayList<>();
        Deque<TreeNode<N>> st = new ArrayDeque<>();
        TreeNode<N> cur = root;

        // === 1) lower_bound(lo): построить стек пути к первому узлу с key >= lo ===
        while (cur != null) {
            int cmp = cur.key.compareTo(lo);
            if (cmp >= 0) { st.push(cur); cur = cur.left; }
            else          { cur = cur.right; }
        }

        // === 2) in-order от lower_bound до hi ===
        while (!st.isEmpty()) {
            TreeNode<N> n = st.pop();

            // верхняя граница
            int cHi = n.key.compareTo(hi);
            if (cHi > 0 || (cHi == 0 && !hiInc)) break;

            // нижняя граница
            int cLo = n.key.compareTo(lo);
            boolean okLo = (cLo > 0) || (cLo == 0 && loInc);
            if (okLo) out.add(n.key);

            // шаг к inorder-наследнику: левый экстремум правого поддерева
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

    /** Экранирует поле (разделитель — ';'). */
    public static String csvEsc(String s){
        if (s == null) return "";
        boolean needQ = s.indexOf(';')>=0 || s.indexOf('"')>=0 || s.indexOf('\n')>=0 || s.indexOf('\r')>=0;
        String r = s.replace("\"", "\"\"");
        return needQ ? "\"" + r + "\"" : r;
    }

    /** Снимает экранирование поля. */
    public static String csvUnesc(String s){
        if (s == null || s.isEmpty()) return "";
        s = s.trim();
        if (s.length()>=2 && s.charAt(0)=='"' && s.charAt(s.length()-1)=='"') {
            String core = s.substring(1, s.length()-1);
            return core.replace("\"\"", "\"");
        }
        return s;
    }

    /** Разбивает CSV-строку по ';', поддерживая кавычки и удвоенные кавычки. */
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
     * Запись дерева в CSV-файл в порядке in-order.
     * @param file     путь к файлу
     * @param encoder  функция (узел) -> строка без перевода строки
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
     * Чтение CSV в дерево.
     * @param file         путь к файлу
     * @param lineDecoder  функция (строка CSV) -> готовый узел N (или null для пропуска строки)
     * @param sorted       true: строки уже отсортированы по ключу — используем O(n) сборку;
     *                     false: вставляем по одной (O(n log n)).
     * @param skipHeader   true: пропустить первую строку
     * @return количество успешно импортированных записей
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
                        // пропускаем битую строку
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
                        // пропуск
                    }
                }
            }
            if (entries.isEmpty()) return 0;
            buildBalancedFromSorted(entries);
            return entries.size();
        }
    }

    /** Итеративная сборка идеально сбалансированного BST из отсортированного списка ключей (по compareTo). */
    protected void buildBalancedFromSorted(List<N> sorted) {
        if (sorted == null || sorted.isEmpty()) { clear(); return; }

        // дерево собираем из готовых TreeNode без insert — O(n)
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

        // пересчёт высот post-order (итеративно)
        recomputeHeightsIterative();
    }

    /** Итеративный post-order для пересчёта height у всех узлов. */
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
