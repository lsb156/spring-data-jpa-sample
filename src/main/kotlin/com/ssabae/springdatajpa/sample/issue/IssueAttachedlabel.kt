package com.ssabae.springdatajpa.sample.issue

import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent

@Entity
@Table(indexes = [ Index(columnList = "labelId") ])
class IssueAttachedLabel (

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @get:NotNull
    @Column(nullable = false, updatable = false)
    var labelId: UUID,

    @get:NotNull
    @PastOrPresent
    @Column(nullable = false, updatable = false)
    var attachedAt: Instant

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IssueAttachedLabel

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "IssueAttachedLabel(id=$id, labelId=$labelId, attachedAt=$attachedAt)"
    }
}
