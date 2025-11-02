/* ========================= com/mycompany/bst_du/Tester.java ========================= */
package com.mycompany.bst_du;

import java.util.*;

/**
 * Тестовый раннер для структуры AVL<N extends EntityNode<N>>.
 * В тестах разрешена рекурсия — для верификации инвариантов.
 */
public final class Tester {

    public static void runAll() {
        int passed = 0, failed = 0;
        List<Runnable> tests = List.of(
                Tester::testEmptyAVL,
                Tester::testBasicInsertFindMinMaxOrder,
                Tester::testRangeQueries,
                Tester::testDeletesVariants,
                Tester::testRandomFuzzing,
                Tester::testClear,
                Tester::testRangeListConsistency // <--- новый тест
        );

        long t0 = System.nanoTime();
        for (int i = 0; i < tests.size(); i++) {
            String name = "T" + (i + 1);
            try {
                tests.get(i).run();
                passed++;
                System.out.println("[PASS] " + name);
            } catch (AssertionError ae) {
                failed++;
                System.out.println("[FAIL] " + name + " :: " + ae.getMessage());
            } catch (Throwable th) {
                failed++;
                System.out.println("[FAIL] " + name + " :: " + th);
            }
        }
        long t1 = System.nanoTime();
        System.out.println("======================================");
        System.out.println("Tests finished: passed=" + passed + ", failed=" + failed
                + ", time=" + String.format(Locale.ROOT, "%.3f ms", (t1 - t0) / 1e6));
        System.out.println("======================================");

        if (failed > 0) {
            throw new AssertionError("Есть упавшие тесты: " + failed);
        }
    }

    private static void testEmptyAVL() {
        AVL<IntNode> avl = new AVL<>();
        assertTrue(avl.size() == 0, "size must be 0");
        assertTrue(avl.isEmpty(), "isEmpty must be true");
        assertTrue(avl.height() == -1, "height(empty) must be -1");
        assertTrue(avl.min() == null && avl.max() == null, "min/max must be null");
        assertTrue(avl.find(new IntNode(42)) == null, "find on empty must be null");
        assertTrue(avl.range(new IntNode(0), new IntNode(100)).isEmpty(), "range on empty must be empty");
        assertTrue(avl.inOrder().isEmpty(), "inOrder empty");
        assertTrue(avl.preOrder().isEmpty(), "preOrder empty");
        assertTrue(avl.postOrder().isEmpty(), "postOrder empty");
        checkAVLInvariants(avl);
    }

    private static void testBasicInsertFindMinMaxOrder() {
        AVL<IntNode> avl = new AVL<>();
        assertTrue(avl.insert(new IntNode(10)), "insert 10");
        assertTrue(avl.insert(new IntNode(5)), "insert 5");
        assertTrue(avl.insert(new IntNode(20)), "insert 20");
        assertTrue(avl.insert(new IntNode(7)), "insert 7");
        assertTrue(avl.insert(new IntNode(15)), "insert 15");
        assertTrue(avl.size() == 5, "size after inserts must be 5");

        boolean dup = avl.insert(new IntNode(15));
        assertTrue(!dup, "duplicate must not insert");
        assertTrue(avl.size() == 5, "size unchanged after duplicate");

        IntNode f = avl.find(new IntNode(7));
        assertTrue(f != null && f.getValue() == 7, "find(7) must return node 7");
        assertTrue(avl.contains(new IntNode(20)), "contains(20) true");

        assertTrue(avl.min().getValue() == 5, "min must be 5");
        assertTrue(avl.max().getValue() == 20, "max must be 20");

        List<IntNode> in = avl.inOrder();
        assertSorted(in);
        checkAVLInvariants(avl);
    }

    private static void testRangeQueries() {
        AVL<IntNode> avl = new AVL<>();
        for (int v = 0; v < 100; v++) avl.insert(new IntNode(v));
        assertTrue(avl.size() == 100, "size must be 100");

        List<IntNode> r1 = avl.range(new IntNode(10), new IntNode(20));
        assertTrue(r1.size() == 11, "range [10,20] size 11");
        assertSeq(r1, 10, 20, true, true);

        List<IntNode> r2 = avl.range(new IntNode(10), false, new IntNode(20), true);
        assertTrue(r2.size() == 10, "range (10,20] size 10");
        assertSeq(r2, 11, 20, true, true);

        List<IntNode> r3 = avl.range(new IntNode(0), new IntNode(0));
        assertTrue(r3.size() == 1 && r3.get(0).getValue() == 0, "range [0,0] == {0}");

        List<IntNode> r4 = avl.range(new IntNode(99), new IntNode(99));
        assertTrue(r4.size() == 1 && r4.get(0).getValue() == 99, "range [99,99] == {99}");

        List<IntNode> r5 = avl.range(new IntNode(200), new IntNode(300));
        assertTrue(r5.isEmpty(), "range outside must be empty");

        assertSorted(avl.inOrder());
        checkAVLInvariants(avl);
    }

    private static void testDeletesVariants() {
        AVL<IntNode> avl = new AVL<>();
        int[] seq = {10, 5, 20, 3, 7, 15, 30, 6, 8, 13, 17, 25, 40};
        for (int x : seq) avl.insert(new IntNode(x));
        assertTrue(avl.size() == seq.length, "size after build");

        assertTrue(avl.delete(new IntNode(6)), "delete leaf 6");
        assertTrue(!avl.contains(new IntNode(6)), "6 removed");
        checkAVLInvariants(avl);

        assertTrue(avl.delete(new IntNode(25)), "delete node with one child 25");
        assertTrue(!avl.contains(new IntNode(25)), "25 removed");
        checkAVLInvariants(avl);

        assertTrue(avl.delete(new IntNode(20)), "delete node with two children 20");
        assertTrue(!avl.contains(new IntNode(20)), "20 removed");
        checkAVLInvariants(avl);

        IntNode rootVal = avl.find(avl.min());
        assertTrue(avl.delete(rootVal), "delete current root (min as proxy)");
        checkAVLInvariants(avl);

        assertSorted(avl.inOrder());
    }

    private static void testRandomFuzzing() {
        AVL<IntNode> avl = new AVL<>();
        Random rnd = new Random(1234567L);
        final int N = 1000;
        final int DEL = 300;

        TreeSet<Integer> expected = new TreeSet<>();
        while (expected.size() < N) {
            int v = rnd.nextInt(5000);
            if (expected.add(v)) {
                boolean ok = avl.insert(new IntNode(v));
                assertTrue(ok, "unique insert must be true");
            }
        }
        assertTrue(avl.size() == expected.size(), "size after random inserts must match set");
        checkAVLInvariants(avl);
        assertInOrderMatchesSet(avl, expected);

        List<Integer> all = new ArrayList<>(expected);
        Collections.shuffle(all, rnd);
        for (int i = 0; i < DEL; i++) {
            int v = all.get(i);
            boolean ok = avl.delete(new IntNode(v));
            assertTrue(ok, "delete existing must be true");
            expected.remove(v);
        }
        assertTrue(avl.size() == expected.size(), "size after deletions must match set");
        checkAVLInvariants(avl);
        assertInOrderMatchesSet(avl, expected);

        for (int i = 0; i < 20; i++) {
            int a = rnd.nextInt(5000), b = rnd.nextInt(5000);
            int lo = Math.min(a, b), hi = Math.max(a, b);
            List<IntNode> got = avl.range(new IntNode(lo), true, new IntNode(hi), true);
            List<Integer> exp = new ArrayList<>(expected.subSet(lo, true, hi, true));
            assertTrue(got.size() == exp.size(), "range size must match expected");
            for (int k = 0; k < exp.size(); k++) {
                assertTrue(got.get(k).getValue() == exp.get(k), "range content mismatch at " + k);
            }
        }
    }

    private static void testClear() {
        AVL<IntNode> avl = new AVL<>();
        for (int i = 0; i < 50; i++) avl.insert(new IntNode(i));
        assertTrue(avl.size() == 50, "size 50 before clear");
        avl.clear();
        assertTrue(avl.size() == 0 && avl.isEmpty(), "clear -> empty");
        assertTrue(avl.height() == -1, "height after clear -1");
        assertTrue(avl.min() == null && avl.max() == null, "min/max after clear null");
        assertTrue(avl.inOrder().isEmpty(), "inOrder after clear empty");
    }

    /** Новый тест: сверяем rangeHalfOpen против «ручного» эталона по inOrder. */
    private static void testRangeListConsistency() {
        AVL<IntNode> avl = new AVL<>();
        for (int v = 0; v < 200; v++) avl.insert(new IntNode(v));
        Random rnd = new Random(42);
        for (int t = 0; t < 50; t++) {
            int a = rnd.nextInt(220), b = rnd.nextInt(220);
            int lo = Math.min(a, b), hi = Math.max(a, b);
            List<IntNode> got = avl.rangeHalfOpen(new IntNode(lo), new IntNode(hi));
            // Эталон: отфильтровать inOrder по полуинтервалу
            List<IntNode> in = avl.inOrder();
            List<IntNode> exp = new ArrayList<>();
            for (IntNode x : in) {
                int v = x.getValue();
                if (v >= lo && v < hi) exp.add(x);
            }
            assertTrue(got.size() == exp.size(), "rangeHalfOpen size mismatch");
            for (int i = 0; i < exp.size(); i++) {
                if (got.get(i).getValue() != exp.get(i).getValue()) {
                    throw new AssertionError("rangeHalfOpen content mismatch at " + i);
                }
            }
        }
    }

    // ======== helpers/инварианты ========

    private static void assertTrue(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
    }

    private static void assertSorted(List<IntNode> in) {
        for (int i = 1; i < in.size(); i++) {
            if (in.get(i - 1).compareTo(in.get(i)) > 0) {
                throw new AssertionError("inOrder must be non-decreasing at index " + i);
            }
        }
    }

    private static void assertSeq(List<IntNode> list, int lo, int hi, boolean loInc, boolean hiInc) {
        if (list.isEmpty()) {
            if (loInc && hiInc && lo == hi) return;
        }
        int expected = loInc ? lo : lo + 1;
        int end = hiInc ? hi : hi - 1;
        for (IntNode n : list) {
            if (n.getValue() != expected) {
                throw new AssertionError("range sequence mismatch: expected " + expected + " got " + n.getValue());
            }
            expected++;
        }
        if (expected - 1 != end) {
            throw new AssertionError("range end mismatch: expected last " + end + " got " + (expected - 1));
        }
    }

    private static void assertInOrderMatchesSet(AVL<IntNode> avl, NavigableSet<Integer> expect) {
        List<IntNode> in = avl.inOrder();
        assertTrue(in.size() == expect.size(), "inOrder size must equal set size");
        int i = 0;
        for (int v : expect) {
            if (in.get(i).getValue() != v) {
                throw new AssertionError("inOrder mismatch at " + i + ": expected " + v + " got " + in.get(i).getValue());
            }
            i++;
        }
    }

    private static <N extends EntityNode<N>> void checkAVLInvariants(AVL<N> tree) {
        Pair p = checkNode(tree.root);
        int publicH = tree.height();
        int expectedPublicH = (p.h == 0) ? -1 : (p.h - 1);
        assertTrue(publicH == expectedPublicH, "public height() mismatch: " + publicH + " vs " + expectedPublicH);
        assertTrue(p.count == tree.size(), "node count vs size() mismatch: " + p.count + " vs " + tree.size());
    }

    private static <N extends EntityNode<N>> Pair checkNode(BST.TreeNode<N> node) {
        if (node == null) return new Pair(0, 0);
        Pair L = checkNode(node.left);
        Pair R = checkNode(node.right);

        int expectedH = 1 + Math.max(L.h, R.h);
        if (node.height != expectedH) {
            throw new AssertionError("height field mismatch at node=" + safePretty(node.key)
                    + " expected=" + expectedH + " actual=" + node.height);
        }

        int bal = L.h - R.h;
        if (Math.abs(bal) > 1) {
            throw new AssertionError("AVL balance violated at node=" + safePretty(node.key)
                    + " balance=" + bal);
        }

        if (node.left != null && node.left.key.compareTo(node.key) > 0) {
            throw new AssertionError("BST order violated (left > node) at node=" + safePretty(node.key));
        }
        if (node.right != null && node.right.key.compareTo(node.key) < 0) {
            throw new AssertionError("BST order violated (right < node) at node=" + safePretty(node.key));
        }

        return new Pair(expectedH, 1 + L.count + R.count);
    }

    private static String safePretty(Object o) {
        if (o == null) return "null";
        if (o instanceof EntityNode<?> en) {
            try { return en.pretty(); } catch (Throwable ignored) {}
        }
        return String.valueOf(o);
    }

    private static final class Pair {
        final int h;
        final int count;
        Pair(int h, int count) { this.h = h; this.count = count; }
    }
}
