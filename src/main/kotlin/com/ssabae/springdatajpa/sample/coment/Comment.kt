package com.ssabae.springdatajpa.sample.coment

import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

@Table(indexes = [
    Index(columnList = "issueId"),
    Index(columnList = "createdBy")
])
@Entity
class Comment (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get:PositiveOrZero
    @Version
    var version: Long? = null,

    @get:NotNull
    var issueId: UUID,

    @Valid
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, optional = false, orphanRemoval = true)
    var content: CommentContent,

    @get:NotNull
    var createdBy: UUID,

    @get:NotNull
    @get:PastOrPresent
    var createdAt: Instant = Instant.now()

)