package com.ssabae.springdatajpa.sample.coment

import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
class CommentContent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @NotNull
    @Lob
    @Column(nullable = false, updatable = false)
    var body: String,

    @NotBlank
    @Size(max = 20)
    @Column(length = 20, nullable = false, updatable = false)
    var mimeType: String

) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CommentContent

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "CommentContent(id=$id, body='$body', mimeType='$mimeType')"
    }
}