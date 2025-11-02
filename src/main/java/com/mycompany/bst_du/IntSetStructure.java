/* ========================= com/mycompany/bst_du/IntSetStructure.java ========================= */
package com.mycompany.bst_du;

import java.util.List;

/**
 * Единый контракт для сравнения упорядоченных целочисленных структур.
 * Диапазон трактуем как полуинтервал [lo, hi) с возрастанием.
 */
public interface IntSetStructure {
    boolean insert(int key);
    boolean delete(int key);
    boolean contains(int key);
    int     size();
    void    clear();

    /** @return минимальный ключ или Integer.MIN_VALUE, если структура пуста. */
    int minOrSentinel();

    /** @return максимальный ключ или Integer.MAX_VALUE, если структура пуста. */
    int maxOrSentinel();

    /**
     * Возвращает ОТСОРТИРОВАННЫЙ список элементов в полуинтервале [lo, hi).
     * Используется в функциональных проверках и в бенчмарке «по спискам».
     */
    List<Integer> rangeList(int lo, int hiExclusive);

    /**
     * Быстрый подсчёт для микро-бенчей (по умолчанию через список).
     * Основной S1-бенч теперь использует именно rangeList(...).
     */
    default long rangeCount(int lo, int hiExclusive) {
        return rangeList(lo, hiExclusive).size();
    }
}
