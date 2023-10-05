package com.tucanoo.crm.data.specifications

import com.tucanoo.crm.data.entities.User
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import jakarta.persistence.criteria.Predicate
import jakarta.persistence.criteria.Root
import org.springframework.data.jpa.domain.Specification


class UserDatatableFilter(var userQuery: String) :
    Specification<User?> {

    override fun toPredicate(root: Root<User?>, query: CriteriaQuery<*>, criteriaBuilder: CriteriaBuilder): Predicate? {
        val predicates = ArrayList<Predicate>()
        if (!userQuery.isNullOrEmpty()) {
            val lowerCaseQuery = userQuery.lowercase() // Convert query to lowercase
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root["username"]),
                    "%$lowerCaseQuery%"
                )
            )
            predicates.add(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root["fullName"]),
                    "%$lowerCaseQuery%"
                )
            )
        }
        return if (predicates.isNotEmpty()) criteriaBuilder.or(*predicates.toTypedArray()) else null
    }
}