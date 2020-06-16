package com.ssabae.springdatajpa.sample.label

import com.ssabae.springdatajpa.sample.test.DataInitializeExecutionListener
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestExecutionListeners
import javax.validation.ConstraintViolationException

@SpringBootTest
@TestExecutionListeners(
    listeners = [ DataInitializeExecutionListener::class ],
    mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
internal class LabelRepositoryTest {

    @Autowired
    private lateinit var sut: LabelRepository

    private final val repoId: String = "20200501120611-test"
    private final val labels = listOf(
        Label(repoId = this.repoId, name = "bug", color = "red")
    )

    @Test
    fun insert() {
        // given
        val label = this.labels[0]

        // when
        val actual = this.sut.save(label)

        // then
        assertThat(label).isSameAs(actual)
        assertThat(actual.id).isNotNull()
        assertThat(actual.isNew).isFalse()
    }

    @Test
    fun invalidNameBlank() {
        val label = Label(repoId = this.repoId, name = " ", color = "red")

        assertThatThrownBy { this.sut.save(label) }
            .extracting{
                println(it.cause?.cause)
                it.cause?.cause
            }
            .isExactlyInstanceOf(ConstraintViolationException::class.java)
    }


}