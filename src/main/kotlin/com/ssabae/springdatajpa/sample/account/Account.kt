package com.ssabae.springdatajpa.sample.account

import org.hibernate.annotations.ResultCheckStyle
import org.hibernate.annotations.SQLDelete
import java.lang.RuntimeException
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.persistence.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.PastOrPresent
import javax.validation.constraints.Size


@Table(indexes = [Index(columnList = "loginId", unique = true)])
@SQLDelete(sql = "UPDATE account SET state = 'DELETED' WHERE id = ?", check = ResultCheckStyle.COUNT)
@Entity
class Account(

    @Id
    var id: UUID? = null,

    @NotBlank
    @Size(max = 50)
    @Column(length = 50, nullable = false, updatable = false)
    var loginId: String,

    @NotBlank
    @Size(max = 100)
    @Column(length = 100, nullable = false, updatable = false)
    var name: String,

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    var state: AccountState = AccountState.ACTIVE,

    @NotBlank
    @Convert(converter = EmailEncryptor::class)
    @Column(nullable = false, columnDefinition = "LONGVARBINARY")
    var email: String,

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    var createdAt: Instant = Instant.now()

) {

    @PostRemove
    fun markDeleted() {
        this.state = AccountState.DELETED
    }

    companion object {
        class EmailEncryptor : AttributeConverter<String, ByteArray> {
            private val transformation: String = "AES/ECB/PKCS5Padding"
            private val algorithm: String = "AES"
            private val keyBytes: ByteArray = "thisisa128bitkey".toByteArray(StandardCharsets.UTF_8)

            override fun convertToDatabaseColumn(attribute: String?): ByteArray? {
                if (attribute == null) return null
                try {
                    val cipher = Cipher.getInstance(transformation)
                    val secretKey = SecretKeySpec(keyBytes, algorithm)
                    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                    return cipher.doFinal(attribute.toByteArray(StandardCharsets.UTF_8))
                } catch (ex: Exception) {
                    throw RuntimeException("Encrypt email is failed", ex)
                }
            }

            override fun convertToEntityAttribute(dbData: ByteArray?): String? {
                if (dbData == null) return null
                try {
                    val cipher = Cipher.getInstance(transformation)
                    val secretKey = SecretKeySpec(keyBytes, algorithm)
                    cipher.init(Cipher.DECRYPT_MODE, secretKey)
                    val decrypted = cipher.doFinal(dbData)
                    return String(decrypted, StandardCharsets.UTF_8)
                } catch (ex: Exception) {
                    throw RuntimeException("Decrypt email is failed.", ex)
                }
            }
        }
    }

    override fun toString(): String {
        return "Account(id=$id, loginId='$loginId', name='$name', state=$state, email='$email', createAy=$createdAt)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

}