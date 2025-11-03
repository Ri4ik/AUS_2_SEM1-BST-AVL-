package com.mycompany.bst_du;

/**
 * SK: Konfiguračná trieda pre benchmark — definuje počty operácií,
 * rozsahy dát a počiatočné seed hodnoty pre generátory.
 */
public final class BenchConfig {
    public final int INSERT_COUNT;       // SK: počet vkladaní
    public final int DELETE_COUNT;       // SK: počet mazaní
    public final int FIND_COUNT;         // SK: počet vyhľadávaní
    public final int RANGE_QUERY_COUNT;  // SK: počet intervalových dopytov
    public final int MIN_COUNT;          // SK: počet volaní min()
    public final int MAX_COUNT;          // SK: počet volaní max()

    public final int DOMAIN;             // SK: maximálny rozsah hodnôt (0..DOMAIN)
    public final int INTERVAL_WIDTH;     // SK: šírka intervalov pri range testoch

    public final long SEED_INSERT;       // SK: seed pre generátor vkladania
    public final long SEED_WARMUP;       // SK: seed pre warm-up fázu
    public final long SEED_FIND_PICK;    // SK: seed pre výber prvkov na vyhľadávanie
    public final long SEED_RANGE_A;      // SK: seed pre dolné hranice intervalov
    public final long SEED_RANGE_B;      // SK: seed pre horné hranice intervalov

    public final int  WARMUP_INSERTS;    // SK: počet prvkov pre zahriatie (warm-up)

    public BenchConfig(int INSERT, int DELETE, int FIND, int RANGE, int MIN, int MAX,
                       int DOMAIN, int WIDTH,
                       long SEED_INSERT, long SEED_WARMUP, long SEED_FIND_PICK, long SEED_RANGE_A, long SEED_RANGE_B,
                       int WARMUP_INSERTS) {
        this.INSERT_COUNT = INSERT;
        this.DELETE_COUNT = DELETE;
        this.FIND_COUNT = FIND;
        this.RANGE_QUERY_COUNT = RANGE;
        this.MIN_COUNT = MIN;
        this.MAX_COUNT = MAX;

        this.DOMAIN = DOMAIN;
        this.INTERVAL_WIDTH = WIDTH;

        this.SEED_INSERT = SEED_INSERT;
        this.SEED_WARMUP = SEED_WARMUP;
        this.SEED_FIND_PICK = SEED_FIND_PICK;
        this.SEED_RANGE_A = SEED_RANGE_A;
        this.SEED_RANGE_B = SEED_RANGE_B;

        this.WARMUP_INSERTS = WARMUP_INSERTS;
    }

    /**
     * SK: Predvolená konfigurácia pre hlavný benchmark scenár (s1).
     * Určená pre veľké testy výkonnosti s 10 miliónmi vkladov.
     */
    public static BenchConfig s1Default() {
        return new BenchConfig(
                10_000_000, 2_000_000, 5_000_000, 1_000_000, 2_000_000, 2_000_000,
                20_000_000, 2_000,
                0x9E37_79B9_7F4A_7C15L, 0x55AA_55AA_55AA_55AAL, 0xCAFEBABE_DEAD_BEEFL,
                0x1234_5678_9ABC_DEF0L, 0xDEAD_BEEF_FEED_FACEL,
                200_000
        );
    }
}
