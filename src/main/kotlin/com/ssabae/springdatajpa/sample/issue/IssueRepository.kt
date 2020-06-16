package com.ssabae.springdatajpa.sample.issue

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface IssueRepository : JpaRepository<Issue, UUID> {

    fun findByTitleLikeAndStatus(titleStartAt: String, status: Status, pageable: Pageable): Page<Issue>

    fun findByRepoIdAndAttachedLabelsLabelId(repoId: String, labelId: UUID, pageable: Pageable): Page<Issue>

    @Transactional
    @Modifying
    @Query("UPDATE Issue i SET i.version = i.version + 1, i.status = :status WHERE i.id = :id")
    fun changeStatus(@Param("id") id: UUID, @Param("status") status: Status): Int
}