package com.tecsup.petclinic.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VetDTO {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    @Builder.Default
    private Boolean active = true;
}
