package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(@NotNull Long userId, @NotBlank @Size(max = 30) String employeeCode,
		@Size(max = 80) String department, @Size(max = 80) String designation) {
}
