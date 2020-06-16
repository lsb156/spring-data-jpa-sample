package com.ssabae.springdatajpa.sample.repo

import com.ssabae.springdatajpa.sample.test.DataInitializeExecutionListener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners
import org.springframework.transaction.support.TransactionTemplate
import java.util.*
import java.util.concurrent.CompletableFuture


@SpringBootTest
@TestExecutionListeners(
    listeners = [ DataInitializeExecutionListener::class ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
internal class RepoRepositoryTest {
    @Autowired
    private lateinit var sut: RepoRepository

    private val creatorId = UUID.randomUUID()
    private val repos = listOf(
        Repo(name = "test", description = "test desc", createdBy = creatorId),
        Repo(name = "spring-data-jdbc", description = "spring-data-jdbc desc", createdBy = creatorId)
    )

    @Test
    fun insert() {
        // given
        val repo = this.repos[0]

        // when
        val actual = this.sut.save(repo)

        // then
        assertThat(repo).isSameAs(actual)
        assertThat(actual.id).isNotNull()
        assertThat(actual.id).contains("-${actual.name}")
    }

    @Test
    fun updateWithLock(@Autowired transactionTemplate: TransactionTemplate) {
        // given
        val repo = this.repos[1]
        this.sut.save(repo)

        // when
        val future: CompletableFuture<Void>? = transactionTemplate.execute {
            val loadWithLock = this.sut.findById(repo.id!!).get()
            val futureChangeName: CompletableFuture<Void> =
                this.asyncChangeName(loadWithLock.id, "spring-data-jpa", transactionTemplate)

            this.sleep(1000)

            loadWithLock.changeName("spring-data-r2dbc")
            this.sut.save(loadWithLock)

            futureChangeName
        }

        future?.join()
        // when
        val actual = this.sut.findById(repo.id!!)

        // then
        assertThat(actual.get().name).isEqualTo("spring-data-jpa")
        assertThat(actual.get().description).isEqualTo("spring-data-jdbc desc")

    }

    @Test
    fun updateDifferentPropertyWithLock(@Autowired transactionTemplate: TransactionTemplate) {
        // given
        val repo = this.repos[1]
        this.sut.save(repo)

        // when
        val future: CompletableFuture<Void>? = transactionTemplate.execute {
            val loadWithLock = this.sut.findById(repo.id!!).get()

            // 비동기로 먼저 변경을 실행시키지만, LOCK 이 잡혀서 현재 트랜잭션이 종료될 때까지 대기한다.
            // LOCK 을 잡은 트랜잭션이 description 수정 커밋 완료 후에 SELECT 하므로, 수정된 Description 값이 유지될 수 있다.
            val futureChangeName: CompletableFuture<Void> =
                this.asyncChangeName(loadWithLock.id, "spring-data-jpa", transactionTemplate)

            this.sleep(1000)

            loadWithLock.changeDescription("spring-data-r2dbc desc")
            this.sut.save(loadWithLock)

            futureChangeName
        }

        // UPDATE 처리 작업을 기다린다.
        future?.join()

        // when
        val actual = this.sut.findById(repo.id!!)

        // then
        assertThat(actual.get().name).isEqualTo("spring-data-jpa")
        assertThat(actual.get().description).isEqualTo("spring-data-r2dbc desc")
    }


    private fun asyncChangeName(id: String?, name: String, transactionTemplate: TransactionTemplate): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            transactionTemplate.execute {
                val loadWithLock = this.sut.findById(id!!).get()
                loadWithLock.changeName(name)
                this.sut.save(loadWithLock)
                null
            }
        }
    }

    private fun sleep(sleepMs: Long) {
        try {
            Thread.sleep(sleepMs)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        }
    }
}