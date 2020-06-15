package com.ssabae.springdatajpa.sample.account

import org.springframework.stereotype.Service
import java.util.*

@Service
class AccountService(val accountRepository: AccountRepository) {

    fun save(account: Account): Account {
        return accountRepository.save(account)
    }

    fun findById(id: UUID): Optional<Account> {
        return accountRepository.findById(id)
    }

    fun findByIdAndStateIn(id: UUID?, states: Set<AccountState>): Account? {
        if (id == null) return null
        return accountRepository.findByIdAndStateIn(id, states).orElse(null)
    }

    fun findByIdExcludeDeleted(id: UUID): Account? {
        return this.findByIdAndStateIn(id, setOf(AccountState.ACTIVE, AccountState.LOCKED))
    }

    fun delete(account: Account) {
        accountRepository.delete(account)
    }
}