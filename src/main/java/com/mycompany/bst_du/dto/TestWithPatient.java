/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.dto;

import com.mycompany.bst_du.domain.Patient;
import com.mycompany.bst_du.domain.PcrTest;

public final class TestWithPatient {
    public final PcrTest test;
    public final Patient patient;
    public TestWithPatient(PcrTest t, Patient p){ this.test=t; this.patient=p; }
}