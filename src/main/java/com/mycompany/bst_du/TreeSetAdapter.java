/* ========================= com/mycompany/bst_du/TreeSetAdapter.java ========================= */
package com.mycompany.bst_du;

import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

public final class TreeSetAdapter implements IntSetStructure {
    private final TreeSet<Integer> ts = new TreeSet<>();

    @Override public boolean insert(int key)   { return ts.add(key); }
    @Override public boolean delete(int key)   { return ts.remove(key); }
    @Override public boolean contains(int key) { return ts.contains(key); }
    @Override public int     size()            { return ts.size(); }
    @Override public void    clear()           { ts.clear(); }

    @Override
    public int minOrSentinel() {
        return ts.isEmpty() ? Integer.MIN_VALUE : ts.first();
    }

    @Override
    public int maxOrSentinel() {
        return ts.isEmpty() ? Integer.MAX_VALUE : ts.last();
    }

    @Override
    public List<Integer> rangeList(int lo, int hiExclusive) {
        if (lo >= hiExclusive) return List.of();
        NavigableSet<Integer> sub = ts.subSet(lo, true, hiExclusive, false);
        return new ArrayList<>(sub); // уже отсортированно
    }

    @Override
    public long rangeCount(int lo, int hiExclusive) {
        if (lo >= hiExclusive || ts.isEmpty()) return 0L;
        return ts.subSet(lo, true, hiExclusive, false).size();
    }
}
