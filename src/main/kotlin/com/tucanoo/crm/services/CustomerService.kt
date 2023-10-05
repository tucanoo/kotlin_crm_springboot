package com.tucanoo.crm.services

import com.tucanoo.crm.data.entities.Customer
import com.tucanoo.crm.data.repositories.CustomerRepository
import com.tucanoo.crm.data.specifications.CustomerDatatableFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class CustomerService(private val customerRepository: CustomerRepository) {

    fun getCustomersForDatatable(queryString: String, pageable: Pageable): Page<Customer> {
        val customerDatatableFilter = CustomerDatatableFilter(queryString)
        return customerRepository.findAll(customerDatatableFilter, pageable)
    }

}