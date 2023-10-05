package com.tucanoo.crm.data.repositories

import com.tucanoo.crm.data.entities.Customer
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.stereotype.Repository

@Repository
interface CustomerRepository : CrudRepository<Customer, Long>,
    PagingAndSortingRepository<Customer, Long?>,
    JpaSpecificationExecutor<Customer>