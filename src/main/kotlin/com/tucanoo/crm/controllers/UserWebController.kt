package com.tucanoo.crm.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.tucanoo.crm.data.entities.User
import com.tucanoo.crm.data.repositories.UserRepository
import com.tucanoo.crm.dto.UserDTO
import com.tucanoo.crm.services.UserService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/user")
class UserWebController(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping
    fun index(): String {
        return "/user/index"
    }

    @GetMapping("/data_for_datatable")
    @ResponseBody
    fun getDataForDatatable(@RequestParam params: Map<String, Any>): String {
        val draw = params["draw"]?.toString()?.toIntOrNull() ?: 1
        val length = params["length"]?.toString()?.toIntOrNull() ?: 30
        val start = params["start"]?.toString()?.toIntOrNull() ?: 30
        val currentPage = start / length

        val sortName: String = params["columns[${params["order[0][column]"]?.toString()}][data]"]?.toString() ?: "id"
        val sortDir = params["order[0][dir]"]?.toString() ?: "asc"

        val sortOrder = if (sortDir == "desc") Sort.Order.desc(sortName) else Sort.Order.asc(sortName)
        val sort = Sort.by(sortOrder)

        val pageRequest = PageRequest.of(currentPage, length, sort)

        val queryString = params["search[value]"] as? String ?: ""

        val users: Page<User> = userService.getUsersForDatatable(queryString, pageRequest)

        val totalRecords = users.totalElements

        val cells = users.map { user ->
            mapOf(
                "id" to user.id,
                "username" to user.username,
                "fullName" to user.fullName,
                "enabled" to user.enabled,
                "dateCreated" to user.dateCreated
            ).toMutableMap()
        }.toList()

        val jsonMap = mutableMapOf(
            "draw" to draw,
            "recordsTotal" to totalRecords,
            "recordsFiltered" to totalRecords,
            "data" to cells
        )

        var json: String? = null
        try {
            json = objectMapper.writeValueAsString(jsonMap)
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }

        return json ?: ""
    }

    @GetMapping("/edit/{id}")
    fun edit(@PathVariable id: Long, model: Model): String {
        val userInstance = userRepository.findById(id).get()
        model.addAttribute("userInstance", userInstance)
        return "/user/edit.html"
    }

    @PostMapping("/update")
    fun update(
        @ModelAttribute("userInstance") userDTO: @Valid UserDTO?,
        bindingResult: BindingResult,
        atts: RedirectAttributes
    ): String? {
        return if (bindingResult.hasErrors()) {
            "/user/edit.html"
        } else {
            if (userService.updateUser(userDTO!!) != null) atts.addFlashAttribute(
                "message",
                "User updated successfully"
            ) else atts.addFlashAttribute("message", "User update failed.")
            "redirect:/user"
        }
    }

    @GetMapping("/create")
    fun create(model: Model): String {
        model.addAttribute("userInstance", User())
        return "/user/create.html"
    }

    @PostMapping("/save")
    fun save(
        @ModelAttribute("userInstance") userDTO: @Valid UserDTO?,
        bindingResult: BindingResult,
        atts: RedirectAttributes
    ): String? {
        return if (bindingResult.hasErrors()) {
            "/user/create.html"
        } else {
            if (userService.createNewUser(userDTO!!) != null) atts.addFlashAttribute(
                "message",
                "User created successfully"
            ) else atts.addFlashAttribute("message", "User creation failed.")
            "redirect:/user"
        }
    }

    @PostMapping("/delete")
    fun delete(@RequestParam id: Long, atts: RedirectAttributes): String {
        val userInstance = userRepository.findById(id)
            .orElseThrow {
                IllegalArgumentException(
                    "User Not Found:$id"
                )
            }
        userRepository.delete(userInstance)
        atts.addFlashAttribute("message", "User deleted.")
        return "redirect:/user"
    }
}