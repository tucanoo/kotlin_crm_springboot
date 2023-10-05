package com.tucanoo.crm.data.specifications

import com.tucanoo.crm.data.entities.Customer
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification

class CustomerDatatableFilter(var userQuery: String) : Specification<Customer> {
    override fun toPredicate(
        root: Root<Customer>,
        query: CriteriaQuery<*>,
        criteriaBuilder: CriteriaBuilder
    ): Predicate? {
        val predicates = ArrayList<Predicate>()

        if (!userQuery.isNullOrEmpty()) {
            val lowerCaseQuery = userQuery.lowercase() // Convert query to lowercase
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root["firstName"]),
                    "%$lowerCaseQuery%"
                )
            )
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root["lastName"]), "%$lowerCaseQuery%"))
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root["city"]), "%$lowerCaseQuery%"))
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root["emailAddress"]),
                    "%$lowerCaseQuery%"
                )
            )
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root["phoneNumber"]),
                    "%$lowerCaseQuery%"
                )
            )
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root["country"]), "%$lowerCaseQuery%"))
        }

        return if (predicates.isNotEmpty()) criteriaBuilder.or(*predicates.toTypedArray()) else null

    }
}