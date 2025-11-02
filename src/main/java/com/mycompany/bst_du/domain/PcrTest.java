/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.bst_du.domain;

import java.time.Instant;

public final class PcrTest {
    public final long   testCode;
    public final String patientId;
    public final long   workstationId;
    public final int    district;
    public final int    region;
    public final boolean positive;
    public final double value;
    public final Instant timestamp;
    public final String note;

    public PcrTest(long testCode, String patientId, Instant timestamp,
                   long workstationId, int district, int region,
                   boolean positive, double value, String note) {
        if (testCode <= 0) throw new IllegalArgumentException("testCode must be > 0");
        if (patientId == null || patientId.isEmpty()) throw new IllegalArgumentException("patientId required");
        if (timestamp == null) throw new IllegalArgumentException("timestamp required");
        this.testCode = testCode;
        this.patientId = patientId;
        this.timestamp = timestamp;
        this.workstationId = workstationId;
        this.district = district;
        this.region = region;
        this.positive = positive;
        this.value = value;
        this.note = note == null ? "" : note;
    }
}