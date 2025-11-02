/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.bst_du;

import java.util.*;

/**
 * Demo / tests.
 */
/**
 * Main: запускает полный набор структурных тестов для AVL.
 */
public class BST_du {
    public static void main(String[] args) {
        // 1) Полные структурные тесты AVL
        Tester.runAll();

        // 2) Универсальное сравнение двух структур
        BenchConfig cfg = BenchConfig.s1Default();
        IntSetStructure avl = new AvlIntAdapter();
        IntSetStructure ref = new TreeSetAdapter(); // позже можно подставить HestAdapter

        DualBenchmark.run(cfg, "AVL<IntNode>", avl, "TreeSet<Integer>", ref);
    }
}

//public class BST_du {
//
//    public static void main(String[] args) {
////        // --- AVL<PersonNode> ---
////        AVL<PersonNode> avl = new AVL<>();
////        insertAll(avl,
////                new PersonNode("Ada", "Lovelace", 1815),
////                new PersonNode("Alan", "Turing", 1912),
////                new PersonNode("Edsger", "Dijkstra", 1930),
////                new PersonNode("Donald", "Knuth", 1938),
////                new PersonNode("Grace", "Hopper", 1906),
////                new PersonNode("Barbara", "Liskov", 1939));
////
////        // дубликат (не должен вставиться)
////        boolean dup = avl.insert(new PersonNode("Donald", "Knuth", 1938));
////
////        System.out.println("=== AVL<PersonNode> ===");
////        System.out.println("size=" + avl.size() + ", height=" + avl.height()
////                + ", inserted duplicate? " + dup);
////        System.out.println("min=" + (avl.min() == null ? "null" : avl.min().pretty()));
////        System.out.println("max=" + (avl.max() == null ? "null" : avl.max().pretty()));
////        System.out.println("contains(Knuth)= " + avl.contains(new PersonNode("Donald", "Knuth", 1938)));
////
////        List<PersonNode> inOrderPeople = castList(avl.inOrder());
////        System.out.println("inOrder:");
////        inOrderPeople.forEach(p -> System.out.println("  " + p.pretty()));
////
////        // SK: rýchla kontrola invariantov — inorder musí byť zoradený
////        if (!isSorted(inOrderPeople)) {
////            System.err.println("AVL ERROR: inorder nie je zoradený!");
////        }
////
////        // Удалим Turing и проверим повторно
////        avl.delete(new PersonNode("Alan", "Turing", 1912));
////        System.out.println("\nAfter delete(Turing): size=" + avl.size() + ", height=" + avl.height());
////        List<PersonNode> inOrderAfterDel = castList(avl.inOrder());
////        inOrderAfterDel.forEach(p -> System.out.println("  " + p.pretty()));
////        if (!isSorted(inOrderAfterDel)) {
////            System.err.println("AVL ERROR: inorder po delete nie je zoradený!");
////        }
//        // --- AVL<IntNode> ---
//    AVL<IntNode> avl = new AVL<>();
//
//    // вставки отдельных чисел
//    avl.insert(new IntNode(10));
//    avl.insert(new IntNode(5));
//    avl.insert(new IntNode(20));
//    avl.insert(new IntNode(7));
//    avl.insert(new IntNode(15));
//
//    // проверка дубликата (не должен вставиться)
//    boolean dup = avl.insert(new IntNode(15));
//
//    System.out.println("=== AVL<IntNode> ===");
//    System.out.println("size=" + avl.size() + ", height=" + avl.height()
//            + ", inserted duplicate? " + dup);
//    System.out.println("min=" + (avl.min()==null ? "null" : avl.min().pretty()));
//    System.out.println("max=" + (avl.max()==null ? "null" : avl.max().pretty()));
//    System.out.println("contains(7)= " + avl.contains(new IntNode(7)));
//
//    // inorder должен быть отсортирован по возрастанию
//    for (EntityNode e : avl.inOrder()) System.out.println("  " + e.pretty());
//
//    // удаление числа
//    avl.delete(new IntNode(10));
//    System.out.println("After delete(10): size=" + avl.size() + ", height=" + avl.height());
//    for (EntityNode e : avl.inOrder()) System.out.println("  " + e.pretty());
//
//        // --- BST<BookNode> ---
//        BST<BookNode> bst = new BST<>();
//        insertAll(bst,
//                new BookNode("Clean Code", "Robert C. Martin", 2008),
//                new BookNode("Effective Java", "Joshua Bloch", 2018),
//                new BookNode("Design Patterns", "GoF", 1994),
//                new BookNode("Algorithms", "Sedgewick & Wayne", 2011),
//                new BookNode("The Pragmatic Programmer", "Andrew Hunt, David Thomas", 1999));
//
//        System.out.println("\n=== BST<BookNode> ===");
//        System.out.println("size=" + bst.size() + ", height=" + bst.height());
//        System.out.println("min=" + (bst.min() == null ? "null" : bst.min().pretty()));
//        System.out.println("max=" + (bst.max() == null ? "null" : bst.max().pretty()));
//
//        List<BookNode> inOrderBooks = castList(bst.inOrder());
//        inOrderBooks.forEach(b -> System.out.println("  " + b.pretty()));
//        if (!isSorted(inOrderBooks)) {
//            System.err.println("BST ERROR: inorder nie je zoradený!");
//        }
//
//        // Удалим одну книгу и покажем результат
//        boolean removed = bst.delete(new BookNode("Design Patterns", "GoF", 1994));
//        System.out.println("\nDelete 'Design Patterns' -> " + removed);
//        List<BookNode> inOrderAfterBookDel = castList(bst.inOrder());
//        inOrderAfterBookDel.forEach(b -> System.out.println("  " + b.pretty()));
//        if (!isSorted(inOrderAfterBookDel)) {
//            System.err.println("BST ERROR: inorder po delete nie je zoradený!");
//        }
//    }
//
//    // ---------- Helpers ----------
//
//    @SafeVarargs
//    private static <T extends EntityNode<T>> void insertAll(BST<T> tree, T... items) {
//        for (T it : items) tree.insert(it);
//    }
//
//    // SK: „bezpečné“ pretypovanie pre výpis — JVM vie, že BST vracia List<N>, tu len uľahčujeme generic syntaktiku
//    @SuppressWarnings("unchecked")
//    private static <T extends EntityNode<T>> List<T> castList(List<? extends EntityNode> raw) {
//        List<T> out = new ArrayList<>(raw.size());
//        for (EntityNode e : raw) out.add((T) e);
//        return out;
//    }
//
//    // SK: kontrola zoradenia podľa compareTo; používa sa pre rýchly sanity-check
//    private static <T extends EntityNode<T>> boolean isSorted(List<T> arr) {
//        for (int i = 1; i < arr.size(); i++) {
//            if (arr.get(i - 1).compareTo(arr.get(i)) > 0) return false;
//        }
//        return true;
//    }
//}
