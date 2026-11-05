package com.example.data.crypto

import java.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

data class EncryptionResult(
    val cipherTextBase64: String,
    val ivHex: String,
    val tagHex: String,
    val rawBytesLength: Int
)

object CryptoEngine {
    private const val AES_KEY_SIZE_BYTES = 32 // 256 bits
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH_BITS = 128

    private val secureRandom = SecureRandom()

    // Deterministic key derived per conversation/contact using SHA-256
    fun deriveSharedSecret(contactPhone: String, userMasterSalt: String = "OneUI9_Encrypted_Salt_v1"): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(userMasterSalt.toByteArray(Charsets.UTF_8))
        return digest.digest(contactPhone.toByteArray(Charsets.UTF_8))
    }

    fun encryptMessage(plainText: String, contactPhone: String): EncryptionResult {
        val secretKeyBytes = deriveSharedSecret(contactPhone)
        val secretKey = SecretKeySpec(secretKeyBytes, "AES")

        val iv = ByteArray(GCM_IV_LENGTH)
        secureRandom.nextBytes(iv)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val cipherBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val cipherTextBase64 = Base64.getEncoder().encodeToString(cipherBytes)

        // In GCM mode, the auth tag is appended to the ciphertext bytes
        val tagBytes = if (cipherBytes.size >= 16) {
            cipherBytes.copyOfRange(cipherBytes.size - 16, cipherBytes.size)
        } else {
            ByteArray(16)
        }

        return EncryptionResult(
            cipherTextBase64 = cipherTextBase64,
            ivHex = iv.toHex(),
            tagHex = tagBytes.toHex(),
            rawBytesLength = cipherBytes.size
        )
    }

    fun decryptMessage(cipherTextBase64: String, ivHex: String, contactPhone: String): String {
        return try {
            val secretKeyBytes = deriveSharedSecret(contactPhone)
            val secretKey = SecretKeySpec(secretKeyBytes, "AES")

            val iv = ivHex.decodeHex()
            val cipherBytes = Base64.getDecoder().decode(cipherTextBase64)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Return placeholder if key mismatch
            "[Decryption Error: Invalid Key or Tampered Packet]"
        }
    }

    fun generateKeyFingerprint(contactPhone: String): String {
        val keyBytes = deriveSharedSecret(contactPhone)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyBytes)
        // Format as colon-separated hex bytes, e.g. SHA-256: 4C:91:E2:B0...
        val hexList = hash.take(8).map { String.format("%02X", it) }
        return "SHA-256: " + hexList.joinToString(":")
    }

    fun generateSafetyNumbers(contactPhone: String): String {
        val keyBytes = deriveSharedSecret(contactPhone)
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(keyBytes)
        // Convert first 20 bytes into 4 groups of 5-digit security verification codes
        val numbers = StringBuilder()
        for (i in 0 until 4) {
            var valGroup = 0
            for (j in 0 until 4) {
                val byteVal = hash[(i * 4 + j) % hash.size].toInt() and 0xFF
                valGroup = (valGroup shl 8) or byteVal
            }
            val formatted = String.format("%05d", Math.abs(valGroup % 100000))
            if (numbers.isNotEmpty()) numbers.append(" ")
            numbers.append(formatted)
        }
        return numbers.toString()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }
}
