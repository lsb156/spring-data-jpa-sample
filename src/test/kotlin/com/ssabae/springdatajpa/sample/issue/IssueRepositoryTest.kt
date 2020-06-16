package com.ssabae.springdatajpa.sample.issue

import com.ssabae.springdatajpa.sample.test.DataInitializeExecutionListener
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.orm.ObjectOptimisticLockingFailureException
import org.springframework.test.context.TestExecutionListeners
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CompletableFuture


@SpringBootTest
@TestExecutionListeners(
    listeners = [ DataInitializeExecutionListener::class ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
internal class IssueRepositoryTest {

    @Autowired
    private lateinit var sut: IssueRepository

    private final val repoId: String = "20200501120611-test"
    private final val creatorId: UUID = UUID.randomUUID()
    private final val labelIds = listOf(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()
    )
    private final val issues = listOf(
        Issue(
            id = UUID.randomUUID(),
            version = 0L,
            repoId = this.repoId,
            issueNo = 1L,
            status = Status.OPEN,
            title = "issue 1",
            content = IssueContent(body = "content 1", mimeType = "text/plain"),
            attachedLabels = listOf(
                IssueAttachedLabel(labelId = labelIds[0], attachedAt = Instant.now().minus(3L, ChronoUnit.DAYS)),
                IssueAttachedLabel(labelId = labelIds[1], attachedAt = Instant.now().minus(2L, ChronoUnit.DAYS))
            ),
            createBy = creatorId ),
        Issue(
            id = UUID.randomUUID(),
            version = 0L,
            repoId = this.repoId,
            issueNo = 2L,
            status = Status.OPEN,
            title = "issue 2",
            content = IssueContent(body = "content 2", mimeType = "text/plain"),
            attachedLabels = listOf(
                IssueAttachedLabel(labelId = labelIds[1], attachedAt = Instant.now().minus(3L, ChronoUnit.DAYS)),
                IssueAttachedLabel(labelId = labelIds[2], attachedAt = Instant.now().minus(2L, ChronoUnit.DAYS))
            ),
            createBy = creatorId ),
        Issue(
            id = UUID.randomUUID(),
            version = 0L,
            repoId = this.repoId,
            issueNo = 3L,
            status = Status.CLOSED,
            title = "issue 3",
            content = IssueContent(body = "content 3", mimeType = "text/plain"),
            attachedLabels = listOf(
                IssueAttachedLabel(labelId = labelIds[0], attachedAt = Instant.now().minus(3L, ChronoUnit.DAYS)),
                IssueAttachedLabel(labelId = labelIds[2], attachedAt = Instant.now().minus(2L, ChronoUnit.DAYS))
            ),
            createBy = creatorId )
        )

    @Test
    fun insert(@Autowired transactionTemplate: TransactionTemplate) {
        // given
        val issue = this.issues[0]

        // when
        val actual = this.sut.save(issue)

        // then
        assertThat(actual.version).isEqualTo(1L) // 0 -> update attachedLabels
        assertThat(issue.content).isNotSameAs(actual.content)

        assertThat(issue.attachedLabels[0].id).isNull()
        assertThat(issue.attachedLabels[1].id).isNull()
        assertThat(actual.attachedLabels[0].id).isEqualTo(1L)
        assertThat(actual.attachedLabels[1].id).isEqualTo(2L)

        transactionTemplate.execute {
            val load = sut.findById(issue.id!!)
            assertThat(load).isPresent
            assertThat(load.get().id).isEqualTo(issue.id)
            assertThat(load.get().version).isEqualTo(1L)
            assertThat(load.get().title).isEqualTo("issue 1")
            assertThat(load.get().status).isEqualTo(Status.OPEN)

            assertThat(load.get().content?.id).isNotNull()
            assertThat(load.get().content?.body).isEqualTo("content 1")
            assertThat(load.get().content?.mimeType).isEqualTo("text/plain")

            assertThat(load.get().attachedLabels[0].id).isEqualTo(1L)
            assertThat(load.get().attachedLabels[1].id).isEqualTo(2L)
        }
    }

    @Test
    fun changeStatus(@Autowired transactionTemplate: TransactionTemplate) {
        // given
        val issue = this.issues[0]
        this.sut.save(issue)

        // when
        val actual = this.sut.changeStatus(issue.id!!, Status.CLOSED)

        // then
        assertThat(actual).isEqualTo(1)

        transactionTemplate.execute {
            val load = this.sut.findById(issue.id!!)
            assertThat(load).isPresent
            assertThat(load.get().id).isEqualTo(issue.id)
            assertThat(load.get().version).isEqualTo(2L)
            assertThat(load.get().repoId).isEqualTo(this.repoId)
            assertThat(load.get().status).isEqualTo(Status.CLOSED)
            assertThat(load.get().title).isEqualTo("issue 1")
            assertThat(load.get().content?.body).isEqualTo("content 1")
            assertThat(load.get().content?.mimeType).isEqualTo("text/plain")
            assertThat(load.get().createBy).isEqualTo(this.creatorId)
            null
        }
    }

    @Test
    fun optimisticLockingFailure(@Autowired transactionTemplate: TransactionTemplate) {
        val issue = this.issues[0]
        this.sut.save(issue)

        assertThatThrownBy {
            transactionTemplate.execute {
                val load = this.sut.findById(issue.id!!).get()
                load.content?.body // lazy loading

                this.asyncChangeContent(
                    load.id!!,
                    IssueContent(
                        body = "spring-boot-jdbc",
                        mimeType = "text/markdown"
                    ),
                    transactionTemplate
                ).join()

                load.changeContent(
                    IssueContent(
                        body = "spring-data-jpa",
                        mimeType = "text/plain"
                    )
                )
                this.sut.save(load)
                null
            }
        }.isExactlyInstanceOf(ObjectOptimisticLockingFailureException::class.java)

        transactionTemplate.execute {
            val load = this.sut.findById(issue.id!!)
            assertThat(load.get().version).isEqualTo(2L)
            assertThat(load.get().content?.body).isEqualTo("spring-boot-jdbc")
            assertThat(load.get().content?.mimeType).isEqualTo("text/markdown")
            null
        }
    }


    @Test
    fun findByTitleLikeAndStatus() {
        // given
        this.sut.saveAll(this.issues)

        // when
        val actual = this.sut.findByTitleLikeAndStatus(
            "issue%",
            Status.OPEN,
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "issueNo"))
        )

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual.totalPages).isEqualTo(1)
        assertThat(actual.totalElements).isEqualTo(2)
        assertThat(actual.content[0].issueNo).isEqualTo(2)
        assertThat(actual.content[1].issueNo).isEqualTo(1)
    }

    @Test
    fun findByRepoIdAndAttachedLabelsLabelId() {
        // given
        this.sut.saveAll(this.issues)

        // when
        val actual = this.sut.findByRepoIdAndAttachedLabelsLabelId(
            this.repoId,
            this.labelIds[0],
            PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "issueNo"))
        )

        // then
        assertThat(actual).hasSize(2)
        assertThat(actual.totalPages).isEqualTo(1)
        assertThat(actual.totalElements).isEqualTo(2)
        assertThat(actual.content[0].issueNo).isEqualTo(3)
        assertThat(actual.content[1].issueNo).isEqualTo(1)
    }

    private fun asyncChangeContent(id: UUID, content: IssueContent, transactionTemplate: TransactionTemplate): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            transactionTemplate.execute {
                val issue = this.sut.findById(id).get()
                issue.changeContent(content)
                this.sut.save(issue)
                null
            }
        }
    }
}

