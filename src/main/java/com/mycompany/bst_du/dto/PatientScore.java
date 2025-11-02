/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.dto;

import com.mycompany.bst_du.domain.Patient;

public final class PatientScore {
    public final Patient patient;
    public final double score;
    public final int district; // -1 если не применимо
    public PatientScore(Patient p, double s){ this(p,s,-1); }
    public PatientScore(Patient p, double s, int d){ this.patient=p; this.score=s; this.district=d; }
}