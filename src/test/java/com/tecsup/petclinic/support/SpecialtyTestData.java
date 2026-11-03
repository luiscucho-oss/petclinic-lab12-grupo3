package com.tecsup.petclinic.support;

import com.tecsup.petclinic.dtos.SpecialtyDTO;

public final class SpecialtyTestData {
    private SpecialtyTestData() {
    }

    public static SpecialtyDTO newSpecialty() {
        return SpecialtyDTO.builder().name("cardiology").office("Centro")
                .openHour(8).closeHour(17).build();
    }

    public static SpecialtyDTO existingSpecialty() {
        return SpecialtyDTO.builder().id(1).name("radiology").office("Farewell")
                .openHour(8).closeHour(18).build();
    }
}
