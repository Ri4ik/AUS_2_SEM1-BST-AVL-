package com.mycompany.bst_du.dto;

import com.mycompany.bst_du.domain.Patient;

/**
 * SK: DTO reprezentujúce pacienta s priradeným skóre (napr. hodnota testu alebo
 * agregovaný ukazovateľ). Pole district je -1, ak sa nevzťahuje na konkrétny okres.
 */
public final class PatientScore {
    public final Patient patient;
    public final double score;
    public final int district; // -1, ak sa nevzťahuje

    public PatientScore(Patient p, double s){ this(p, s, -1); }
    public PatientScore(Patient p, double s, int d){
        this.patient = p;
        this.score = s;
        this.district = d;
    }
}
