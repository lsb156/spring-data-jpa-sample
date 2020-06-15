package com.ssabae.springdatajpa.sample.account

import com.ssabae.springdatajpa.sample.test.DataInitializeExecutionListener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@SpringBootTest
@TestExecutionListeners(
    listeners = [ DataInitializeExecutionListener::class ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
internal class AccountRepositoryTest {
    @Autowired
    lateinit var sut: AccountService

    val accounts: List<Account> = listOf(
        Account(
            id = UUID.randomUUID(),
            loginId = "navercorp.com",
            name = "naver",
            state = AccountState.ACTIVE,
            email = "naver@navercorp.com"
        )
    )

    @Test
    fun encryptDecrypt() {
        // given
        val account = accounts[0]
        this.sut.save(account)

        // when
        val actual = this.sut.findById(account.id!!)

        // then
        assertThat(actual.get().email).isEqualTo(account.email)
    }


    @Test
    fun softDelete(@Autowired transactionTemplate: TransactionTemplate) {
        // given
        val account = accounts[0]
        this.sut.save(account)

        // when
        val actual = transactionTemplate.execute { status ->
            val load = this.sut.findById(account.id!!).get()
            this.sut.delete(load)
            load
        }

        // then
        actual ?: assert(true)
        actual?.let {
            assertThat(actual.state).isEqualTo(AccountState.DELETED)
            val loadDeleted = this.sut.findById(actual.id!!)
            assertThat(loadDeleted).isPresent
            assertThat(loadDeleted.get().state).isEqualTo(AccountState.DELETED)

            val deleted = this.sut.findByIdExcludeDeleted(actual.id!!)
            assertThat(deleted).isNull()
        }
    }

}