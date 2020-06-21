package com.ssabae.springdatajpa.sample.issue

import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.*

@Entity
@Table(indexes = [
    Index(columnList = "repoId, issueNo", unique = true),
    Index(columnList = "repoId"),
    Index(columnList = "createBy"),
    Index(columnList = "title")
])
class Issue (

    @Id
    @Column(length = 36)
    var id: UUID? = null,

    @get:PositiveOrZero
    @Version
    var version: Long? = null,

    @get:NotBlank
    @get:Size(max = 200)
    @Column(length = 200, nullable = false, updatable = false)
    var repoId: String,

    @get:NotNull
    @get:PositiveOrZero
    @Column(nullable = false, updatable = false)
    var issueNo: Long,

    @get:NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: Status,

    @get:NotBlank
    @get:Size(max = 200)
    @Column(length = 200, nullable = false)
    var title: String,

    @Valid
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false, orphanRemoval = true)
    var content: IssueContent? = null,

    @Valid
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "issue_id")
    @OrderBy("attachedAt")
    @org.hibernate.annotations.BatchSize(size = 20)
    var attachedLabels: List<IssueAttachedLabel> = emptyList(),

    @get:NotNull
    @Column(length = 36, nullable = false, updatable = false)
    var createBy: UUID,

    @get:NotNull
    @get:PastOrPresent
    @Column(nullable = false, updatable = false)
    val createAt: Instant = Instant.now()

) {

    fun changeContent(content: IssueContent) {
        this.content = content
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Issue

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Issue(id=$id, version=$version, repoId='$repoId', issueNo=$issueNo, status=$status, content=$content, attachedLabels=$attachedLabels, createBy=$createBy, createAt=$createAt)"
    }
}