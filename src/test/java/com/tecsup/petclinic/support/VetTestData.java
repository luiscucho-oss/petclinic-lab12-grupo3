package com.tecsup.petclinic.support;

import com.tecsup.petclinic.dtos.VetDTO;

public final class VetTestData {
    private VetTestData() {
    }

    public static VetDTO newVet() {
        return VetDTO.builder().firstName("Lucia").lastName("Torres")
                .email("lucia.torres@petclinic.com").phone("999111222").build();
    }

    public static VetDTO existingVet() {
        return VetDTO.builder().id(1).firstName("James").lastName("Carter")
                .email("james.carter@petclinic.com").phone("6085551234").build();
    }
}
