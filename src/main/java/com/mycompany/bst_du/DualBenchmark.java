/* ========================= com/mycompany/bst_du/DualBenchmark.java ========================= */
package com.mycompany.bst_du;

import java.util.*;

/**
 * Универсальный бенчмарк: сравнивает две произвольные структуры (через IntSetStructure)
 * по полному набору операций из задания S1.
 * Теперь «intervalové hľadanie» измеряем по СПИСКАМ (rangeList), как в лекциях.
 */
public final class DualBenchmark {

    public static void run(BenchConfig cfg, String nameA, IntSetStructure A, String nameB, IntSetStructure B) {
        System.out.println("\n=== Universal S1 Benchmark: " + nameA + " vs " + nameB + " ===");
        System.out.printf(Locale.ROOT,
                "INSERT=%d, DELETE=%d, FIND=%d, RANGE=%d, MIN=%d, MAX=%d, DOMAIN=%d, WIDTH=%d%n",
                cfg.INSERT_COUNT, cfg.DELETE_COUNT, cfg.FIND_COUNT, cfg.RANGE_QUERY_COUNT,
                cfg.MIN_COUNT, cfg.MAX_COUNT, cfg.DOMAIN, cfg.INTERVAL_WIDTH);

        final int[] deleteKeys = new int[cfg.DELETE_COUNT];
        final int[] findKeys   = new int[cfg.FIND_COUNT];

        // ---------- Структура A ----------
        System.out.println("\n--- " + nameA + " ---");
        warmup(A, cfg);

        SplittableRandom genInsert = new SplittableRandom(cfg.SEED_INSERT);
        long t1 = System.nanoTime();
        long aInsertedUnique = reservoirInsertFill(A, genInsert, deleteKeys, findKeys, cfg);
        long t2 = System.nanoTime();
        printStat(nameA, "insert", cfg.INSERT_COUNT, t1, t2);

        long t3 = System.nanoTime();
        long aDeleted = deletes(A, deleteKeys);
        long t4 = System.nanoTime();
        printStat(nameA, "delete", cfg.DELETE_COUNT, t3, t4);
        checkSize(nameA, Math.max(0, aInsertedUnique - aDeleted), A.size());

        long t5 = System.nanoTime();
        int aFindHits = finds(A, findKeys);
        long t6 = System.nanoTime();
        printStat(nameA, "find_existing", aFindHits, t5, t6);

        SplittableRandom rqRndA = new SplittableRandom(cfg.SEED_RANGE_A);
        long t7 = System.nanoTime();
        long aRangeTotal = rangeStrictViaLists(A, rqRndA, cfg.DOMAIN, cfg.INTERVAL_WIDTH, cfg.RANGE_QUERY_COUNT, 500);
        long t8 = System.nanoTime();
        printStat(nameA, "range_list", cfg.RANGE_QUERY_COUNT, t7, t8, "total_hits", aRangeTotal);

        long t9 = System.nanoTime();
        for (int i = 0; i < cfg.MIN_COUNT; i++) A.minOrSentinel();
        long t10 = System.nanoTime();
        printStat(nameA, "min", cfg.MIN_COUNT, t9, t10);

        long t11 = System.nanoTime();
        for (int i = 0; i < cfg.MAX_COUNT; i++) A.maxOrSentinel();
        long t12 = System.nanoTime();
        printStat(nameA, "max", cfg.MAX_COUNT, t11, t12);

        // ---------- Структура B ----------
        System.out.println("\n--- " + nameB + " ---");
        warmup(B, cfg);

        SplittableRandom genInsertB = new SplittableRandom(cfg.SEED_INSERT);
        long s1 = System.nanoTime();
        long bInsertedUnique = reservoirInsertFill(B, genInsertB, deleteKeys, findKeys, cfg);
        long s2 = System.nanoTime();
        printStat(nameB, "insert", cfg.INSERT_COUNT, s1, s2);

        long s3 = System.nanoTime();
        long bDeleted = deletes(B, deleteKeys);
        long s4 = System.nanoTime();
        printStat(nameB, "delete", cfg.DELETE_COUNT, s3, s4);
        checkSize(nameB, Math.max(0, bInsertedUnique - bDeleted), B.size());

        long s5 = System.nanoTime();
        int bFindHits = finds(B, findKeys);
        long s6 = System.nanoTime();
        printStat(nameB, "find_existing", bFindHits, s5, s6);

        SplittableRandom rqRndB = new SplittableRandom(cfg.SEED_RANGE_B);
        long s7 = System.nanoTime();
        long bRangeTotal = rangeStrictViaLists(B, rqRndB, cfg.DOMAIN, cfg.INTERVAL_WIDTH, cfg.RANGE_QUERY_COUNT, 500);
        long s8 = System.nanoTime();
        printStat(nameB, "range_list", cfg.RANGE_QUERY_COUNT, s7, s8, "total_hits", bRangeTotal);

        long s9 = System.nanoTime();
        for (int i = 0; i < cfg.MIN_COUNT; i++) B.minOrSentinel();
        long s10 = System.nanoTime();
        printStat(nameB, "min", cfg.MIN_COUNT, s9, s10);

        long s11 = System.nanoTime();
        for (int i = 0; i < cfg.MAX_COUNT; i++) B.maxOrSentinel();
        long s12 = System.nanoTime();
        printStat(nameB, "max", cfg.MAX_COUNT, s11, s12);

        System.out.println("\n=== Done. Results printed above. ===");
    }

    // ---------- helpers ----------

    private static void warmup(IntSetStructure S, BenchConfig cfg) {
        SplittableRandom wr = new SplittableRandom(cfg.SEED_WARMUP);
        for (int i = 0; i < cfg.WARMUP_INSERTS; i++) S.insert(wr.nextInt(1_000_000));
        for (int i = 0; i < cfg.WARMUP_INSERTS / 4; i++) S.contains(wr.nextInt(1_000_000));
        for (int i = 0; i < cfg.WARMUP_INSERTS / 8; i++) S.delete(wr.nextInt(1_000_000));
        S.clear();
    }

    private static long reservoirInsertFill(IntSetStructure S,
                                            SplittableRandom gen,
                                            int[] deleteKeys,
                                            int[] findKeys,
                                            BenchConfig cfg) {
        long insertedUnique = 0L;
        for (int i = 0; i < cfg.INSERT_COUNT; i++) {
            int v = gen.nextInt(cfg.DOMAIN);
            if (i < cfg.DELETE_COUNT) deleteKeys[i] = v;

            if (i < cfg.FIND_COUNT) findKeys[i] = v;
            else {
                int r = gen.nextInt(i + 1);
                if (r < cfg.FIND_COUNT) findKeys[r] = v;
            }

            if (S.insert(v)) insertedUnique++;
        }
        return insertedUnique;
    }

    private static long deletes(IntSetStructure S, int[] keys) {
        long del = 0;
        for (int v : keys) if (S.delete(v)) del++;
        return del;
    }

    private static int finds(IntSetStructure S, int[] keys) {
        int hits = 0;
        for (int v : keys) if (S.contains(v)) hits++;
        return hits;
    }

    /** Теперь берём ИМЕННО список, проверяем ≥500, суммируем total_hits как суммарную длину списков. */
    private static long rangeStrictViaLists(IntSetStructure S, SplittableRandom rnd,
                                            int domain, int width, int acceptedNeeded, int minHits) {
        int accepted = 0;
        long total = 0L;
        while (accepted < acceptedNeeded) {
            int lo = rnd.nextInt(domain - width);
            int hi = lo + width;
            var list = S.rangeList(lo, hi); // [lo, hi)
            if (list.size() >= minHits) {
                total += list.size();
                accepted++;
            }
        }
        return total;
    }

    private static void checkSize(String who, long expected, long actual) {
        if (expected != actual) {
            System.out.printf(Locale.ROOT,
                    "WARNING [%s]: expected size=%,d, actual size=%,d%n",
                    who, expected, actual);
        } else {
            System.out.printf(Locale.ROOT,
                    "OK       [%s]: size = %,d%n", who, actual);
        }
    }

    private static void printStat(String who, String op, long ops, long tStart, long tEnd) {
        printStat(who, op, ops, tStart, tEnd, null, null);
    }

    private static void printStat(String who, String op, long ops, long tStart, long tEnd,
                                  String extraKey, Long extraVal) {
        long ns = tEnd - tStart;
        double ms = ns / 1_000_000.0;
        double thr = (ops > 0 && ns > 0) ? (ops * 1_000_000_000.0 / ns) : 0.0;

        String line = String.format(Locale.ROOT,
                "%-10s %-16s : time = %,.3f ms, ops = %,d, throughput = %,.2f ops/s",
                who, op, ms, ops, thr);
        System.out.println(line);

        if (extraKey != null && extraVal != null) {
            System.out.printf(Locale.ROOT, "             %-16s : %s = %,d%n", op, extraKey, extraVal);
        }
    }
}
