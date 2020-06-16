package com.ssabae.springdatajpa.sample.repo

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*
import javax.persistence.LockModeType

interface RepoRepository : JpaRepository<Repo, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    override fun findById(id: String): Optional<Repo>
}