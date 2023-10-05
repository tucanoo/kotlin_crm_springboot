package com.tucanoo.crm.dto

import com.tucanoo.crm.enums.Role
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

class UserDTO {
    var id: Long = 0

    @NotNull
    var role: Role? = null

    @NotBlank
    var username: String? = null

    @NotBlank
    var fullName: String? = null

    var password: String? = null

    var enabled: Boolean? = false
}


