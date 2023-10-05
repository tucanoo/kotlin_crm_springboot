package com.tucanoo.crm.config

import com.tucanoo.crm.data.repositories.UserRepository
import com.tucanoo.crm.security.CustomUserDetailsService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Autowired
    lateinit var userRepository: UserRepository

    private val log = LoggerFactory.getLogger(this.javaClass)

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity, mvc: MvcRequestMatcher.Builder): SecurityFilterChain? {
        http
            .csrf { obj: CsrfConfigurer<HttpSecurity> -> obj.disable() }
            .authorizeHttpRequests(
                Customizer { requests ->
                    requests // only allow standard user and admin to edit customers
                        .requestMatchers(mvc.pattern("/customer/edit/**"))
                        .hasAnyAuthority("ADMIN", "USER") // only allow admin and standard users to edit customers
                        .requestMatchers(mvc.pattern("/customer/create/**"))
                        .hasAnyAuthority("ADMIN", "USER") // only allow admin and standard users to create customers
                        .requestMatchers(mvc.pattern("/user/**")).hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                }
            ) // allow access to login endpoint
            .formLogin { form: FormLoginConfigurer<HttpSecurity?> ->
                form
                    .loginPage("/login")
                    .permitAll()
            } // allow access to logout endpoint
            .logout { obj: LogoutConfigurer<HttpSecurity?> -> obj.permitAll() } // in case of 403 / Forbidden authorization failure, use a custom handler
            .exceptionHandling { exception: ExceptionHandlingConfigurer<HttpSecurity?> ->
                exception.accessDeniedHandler(
                    accessDeniedHandler()
                )
            }
        return http.build()
    }

    @Bean
    fun mvc(introspector: HandlerMappingIntrospector?): MvcRequestMatcher.Builder? {
        return MvcRequestMatcher.Builder(introspector)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder? {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun userDetailsService(): UserDetailsService? {
        return CustomUserDetailsService(userRepository)
    }

    @Bean
    fun authenticationProvider(): AuthenticationProvider? {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setUserDetailsService(userDetailsService())
        authenticationProvider.setPasswordEncoder(passwordEncoder())
        return authenticationProvider
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler? {
        return AccessDeniedHandler { request: HttpServletRequest, response: HttpServletResponse?,
                                     accessDeniedException: AccessDeniedException? ->

            log.warn(accessDeniedException?.message)

            request.setAttribute("error", "You do not have permission to access this page. ")
            val dispatcher = request.getRequestDispatcher("/")
            dispatcher.forward(request, response)
        }
    }
}