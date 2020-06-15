package com.ssabae.springdatajpa.sample.account

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface AccountRepository : JpaRepository<Account, UUID> {
    fun findByIdAndStateIn(id: UUID, states: Set<AccountState>): Optional<Account>
}