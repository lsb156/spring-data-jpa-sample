package com.ssabae.springdatajpa.sample.repo

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Size

@Entity
@Table(indexes = [
    Index(columnList = "name", unique = true),
    Index(columnList = "createdBy")
])
class Repo (

    @Id
    var id: String? = null,

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    var name: String,

    @Size(max = 255)
    var description: String,

    @NotNull
    @Column(nullable = false, updatable = false)
    var createdBy: UUID,

    @NotNull
    @PastOrPresent
    @Column(nullable = false, updatable = false)
    var createAt: Instant = Instant.now()

) {
    companion object {
        private val ID_PREDIX_FORMAT = DateTimeFormatter
            .ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.of("Asia/Seoul"))

        fun generateId(repo: Repo): String {
            if (repo.id != null) {
                throw IllegalArgumentException("Repo is already set id. id: ${repo.id}")
            }
            return StringJoiner("-")
                .add(ID_PREDIX_FORMAT.format(repo.createAt))
                .add(repo.name)
                .toString()
        }
    }

    fun changeName(name: String) {
        this.name = name
    }

    fun changeDescription(description: String) {
        this.description = description
    }

    @PrePersist
    fun prePersist() {
        this.id = generateId(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Repo

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Repo(id=$id, name='$name', description='$description', createdBy=$createdBy, createAt=$createAt)"
    }

}