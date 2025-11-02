/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.domain;


import java.time.LocalDate;

public final class Patient {
    public final String patientId;
    public final String firstName;
    public final String lastName;
    public final LocalDate birthDate;

    public Patient(String patientId, String firstName, String lastName, LocalDate birthDate) {
        if (patientId == null || patientId.isEmpty()) throw new IllegalArgumentException("patientId required");
        this.patientId = patientId;
        this.firstName = firstName == null ? "" : firstName;
        this.lastName  = lastName  == null ? "" : lastName;
        this.birthDate = birthDate == null ? LocalDate.of(1970,1,1) : birthDate;
    }
}