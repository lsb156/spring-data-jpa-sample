package com.ssabae.springdatajpa.sample.coment

import org.springframework.data.annotation.CreatedBy
import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Positive


@Table(indexes = [
    Index(columnList = "issueId"),
    Index(columnList = "createdBy")
])
@Entity
class Comment (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Positive
    @Version
    var version: Long? = null,

    @NotNull
    var issueId: UUID,

    @Valid
    @OneToOne(cascade = [CascadeType.ALL], fetch = FetchType.EAGER, optional = false, orphanRemoval = true)
    var content: CommentContent,

    @NotNull
    var createdBy: UUID,

    @NotNull
    @PastOrPresent
    var createdAt: Instant = Instant.now()

)