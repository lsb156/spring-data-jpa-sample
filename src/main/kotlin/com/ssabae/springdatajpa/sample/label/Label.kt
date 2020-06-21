package com.ssabae.springdatajpa.sample.label

import org.springframework.data.domain.Persistable
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Entity
@Table(indexes = [ Index(columnList = "repoId") ])
class Label (
    @Id
    @Column(name = "id")
    var natualId: UUID = UUID.randomUUID(),

    @get:NotBlank
    @get:Size(max = 200)
    @Column(length = 200, nullable = false, updatable = false)
    var repoId: String,

    @get:NotBlank
    @get:Size(max = 100)
    @Column(length = 100, nullable = false)
    var name: String,

    @get:NotBlank
    @get:Size(max = 20)
    @Column(length = 20, nullable = false)
    var color: String

) : Persistable<UUID> {

    @Transient
    var newFlag: Boolean = true

    @PostPersist
    @PostLoad
    fun markNotNew() {
        this.newFlag = false
    }

    override fun getId(): UUID? {
        return natualId
    }

    override fun isNew(): Boolean {
        return newFlag
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Label

        if (natualId != other.natualId) return false

        return true
    }

    override fun hashCode(): Int {
        return natualId.hashCode()
    }

    override fun toString(): String {
        return "Label(natualId=$natualId, repoId='$repoId', name='$name', color='$color', newFlag=$newFlag)"
    }

}