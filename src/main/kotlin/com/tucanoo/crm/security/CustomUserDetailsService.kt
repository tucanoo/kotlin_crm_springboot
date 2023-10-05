package com.tucanoo.crm.security

import com.tucanoo.crm.data.repositories.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsService(private val userRepository: UserRepository) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            ?.orElseThrow { UsernameNotFoundException("User not found") }!!

        return CustomUserDetails(user)
    }
}