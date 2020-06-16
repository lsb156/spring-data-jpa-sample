package com.ssabae.springdatajpa.sample.comment

import com.ssabae.springdatajpa.sample.coment.Comment
import com.ssabae.springdatajpa.sample.coment.CommentContent
import com.ssabae.springdatajpa.sample.coment.CommentRepository
import com.ssabae.springdatajpa.sample.test.DataInitializeExecutionListener
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners
import java.util.*

@SpringBootTest
@TestExecutionListeners(
    listeners = [ DataInitializeExecutionListener::class ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
internal class CommentRepositoryTest {

    @Autowired
    lateinit var sut: CommentRepository

    private final val issue1Id: UUID = UUID.randomUUID()
    private final val creatorId: UUID = UUID.randomUUID()
    val comments = listOf(
        Comment(
            issueId = issue1Id,
            content = CommentContent(
                body = "comment 1",
                mimeType = "text/plain"
            ),
            createdBy = creatorId
        ),
        Comment(
            issueId = issue1Id,
            content = CommentContent(
                body = "comment 2",
                mimeType = "text/plain"
            ),
            createdBy = creatorId
        )
    )

    @Test
    fun insert() {
        // given
        val comment = comments[0]

        // when
        val actual = sut.save(comment)

        // then
        assertThat(actual.version).isEqualTo(0L)
        assertThat(comment.content).isSameAs(actual.content)
    }
}