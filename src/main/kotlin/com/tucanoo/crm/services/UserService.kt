package com.tucanoo.crm.services

import com.tucanoo.crm.data.entities.User
import com.tucanoo.crm.data.repositories.UserRepository
import com.tucanoo.crm.data.specifications.UserDatatableFilter
import com.tucanoo.crm.dto.UserDTO
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {


    fun getUsersForDatatable(queryString: String?, pageable: Pageable?): Page<User> {
        val userDatatableFilter = UserDatatableFilter(queryString!!)
        return userRepository.findAll(userDatatableFilter, pageable!!)
    }

    fun createNewUser(userDTO: UserDTO): User? {

        val user: User = User(
            fullName = userDTO.username,
            role = userDTO.role,
            username = userDTO.username,
            password = passwordEncoder.encode(userDTO.password),
            enabled = true
        )

        return userRepository.save(user)
    }

    fun updateUser(userDTO: UserDTO): User? {
        val user = userRepository.findById(userDTO.id).orElseThrow()!!

        user.fullName = userDTO.fullName
        user.username = userDTO.username
        user.enabled = userDTO.enabled!!
        user.role = userDTO.role

        return userRepository.save(user)
    }
}