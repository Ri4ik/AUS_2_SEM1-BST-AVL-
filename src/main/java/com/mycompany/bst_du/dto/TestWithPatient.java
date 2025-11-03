package com.mycompany.bst_du.dto;

import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;

/**
 * SK: DTO reprezentujúce prepojenie medzi testom a pacientom.
 * Používa sa napr. pri dotazoch „nájdi test pacienta“ alebo pri spoločných výpisoch.
 */
public final class TestWithPatient {
    public final PcrTest test;
    public final Patient patient;

    public TestWithPatient(PcrTest t, Patient p){
        this.test = t;
        this.patient = p;
    }
}
