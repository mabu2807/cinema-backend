package de.cinetastisch.backend.dto;

public record UserRequestDto(
         String firstName,
         String lastName,
         String email,
         String password,
         String birthday,
         String country,
         String city,
         String zip,
         String street,
         Integer houseNumber
) {
}
