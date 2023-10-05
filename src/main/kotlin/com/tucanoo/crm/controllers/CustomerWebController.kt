package com.tucanoo.crm.controllers

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.tucanoo.crm.data.entities.Customer
import com.tucanoo.crm.data.repositories.CustomerRepository
import com.tucanoo.crm.services.CustomerService
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
@RequestMapping("/customer")
class CustomerWebController(
    private val customerRepository: CustomerRepository,
    private val customerService: CustomerService,
    private val objectMapper: ObjectMapper
) {

    @GetMapping
    fun index(): String {
        return "/customer/index"
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

        val customers: Page<Customer> = customerService.getCustomersForDatatable(queryString, pageRequest)

        val totalRecords = customers.totalElements

        val cells = customers.map { customer ->
            mapOf(
                "id" to customer.id,
                "firstName" to customer.firstName,
                "lastName" to customer.lastName,
                "emailAddress" to customer.emailAddress,
                "city" to customer.city,
                "country" to customer.country,
                "phoneNumber" to customer.phoneNumber
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

    @GetMapping("/show/{id}")
    fun show(@PathVariable id: Long, model: Model): String {
        val customerInstance = customerRepository.findById(id).get()
        model.addAttribute("customerInstance", customerInstance)
        return "/customer/show"
    }

    @GetMapping("/edit/{id}")
    fun edit(@PathVariable id: Long, model: Model): String {
        val customerInstance = customerRepository.findById(id).get()
        model.addAttribute("customerInstance", customerInstance)
        return "/customer/edit"
    }

    @PostMapping("/update")
    fun update(
        @ModelAttribute("customerInstance") customerInstance: @Valid Customer?,
        bindingResult: BindingResult,
        model: Model?,
        atts: RedirectAttributes
    ): String? {
        return if (bindingResult.hasErrors()) {
            "/customer/edit"
        } else {
            if (customerRepository.save(customerInstance!!) != null)
                atts.addFlashAttribute("message", "Customer updated successfully")
            else
                atts.addFlashAttribute("message", "Customer update failed.")

            "redirect:/customer"
        }
    }

    @GetMapping("/create")
    fun create(model: Model): String {
        model.addAttribute("customerInstance", Customer())
        return "/customer/create"
    }

    @PostMapping("/save")
    fun save(
        @ModelAttribute("customerInstance") customerInstance: @Valid Customer,
        bindingResult: BindingResult,
        model: Model?,
        atts: RedirectAttributes
    ): String? {
        return if (bindingResult.hasErrors()) {
            "/customer/create"
        } else {
            if (customerRepository.save(customerInstance) != null)
                atts.addFlashAttribute("message", "Customer created successfully")
            else
                atts.addFlashAttribute("message", "Customer creation failed.")

            "redirect:/customer"
        }
    }

    @PostMapping("/delete")
    fun delete(@RequestParam id: Long, atts: RedirectAttributes): String {
        val customerInstance = customerRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Customer Not Found:$id") }

        customerRepository.delete(customerInstance)
        atts.addFlashAttribute("message", "Customer deleted.")

        return "redirect:/customer"
    }
}