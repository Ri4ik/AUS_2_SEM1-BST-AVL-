/* ========================= com/mycompany/bst_du/IntSetStructure.java ========================= */
package com.mycompany.bst_du;

import java.util.List;

/**
 * SK: Jednotný kontrakt pre porovnávanie usporiadaných celočíselných štruktúr.
 * Interval chápeme ako polouzavretý [lo, hi) so vzostupným zoradením.
 */
public interface IntSetStructure {
    boolean insert(int key);
    boolean delete(int key);
    boolean contains(int key);
    int     size();
    void    clear();

    /** @return minimálny kľúč alebo Integer.MIN_VALUE, ak je štruktúra prázdna. */
    int minOrSentinel();

    /** @return maximálny kľúč alebo Integer.MAX_VALUE, ak je štruktúra prázdna. */
    int maxOrSentinel();

    /**
     * SK: Vráti ZORADENÝ zoznam prvkov v polouzavretom intervale [lo, hi).
     * Používa sa vo funkčných testoch a v „listovom“ benchmarku.
     */
    List<Integer> rangeList(int lo, int hiExclusive);

    /**
     * SK: Rýchle spočítanie pre mikro-bench (predvolene cez zoznam).
     * Hlavný S1-benchmark teraz používa priamo rangeList(...).
     */
    default long rangeCount(int lo, int hiExclusive) {
        return rangeList(lo, hiExclusive).size();
    }
}
