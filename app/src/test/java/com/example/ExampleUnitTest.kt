package com.example

import com.example.data.crypto.CryptoEngine
import com.example.data.model.MessageProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testProtocolProperties() {
        assertEquals(160, MessageProtocol.SMS.maxChars)
        assertEquals(1000, MessageProtocol.MMS.maxChars)
        assertEquals(8000, MessageProtocol.RCS.maxChars)

        assertTrue(MessageProtocol.RCS.supportsEncryption)
        assertTrue(MessageProtocol.MMS.supportsMedia)
    }

    @Test
    fun testCryptoEngineSafetyNumbersAndFingerprints() {
        val phone = "+1 (555) 019-2834"
        val fingerprint1 = CryptoEngine.generateKeyFingerprint(phone)
        val fingerprint2 = CryptoEngine.generateKeyFingerprint(phone)

        assertTrue(fingerprint1.startsWith("SHA-256: "))
        assertEquals(fingerprint1, fingerprint2)

        val safetyNumbers = CryptoEngine.generateSafetyNumbers(phone)
        assertNotNull(safetyNumbers)
        assertTrue(safetyNumbers.split(" ").size == 4)
    }

    @Test
    fun testCryptoEngineEncryptionAndDecryption() {
        val phone = "+1 (555) 019-2834"
        val plainText = "One UI 9 Encrypted Message Payload"

        val encResult = CryptoEngine.encryptMessage(plainText, phone)
        assertNotNull(encResult.cipherTextBase64)
        assertNotNull(encResult.ivHex)
        assertNotNull(encResult.tagHex)

        val decrypted = CryptoEngine.decryptMessage(
            encResult.cipherTextBase64,
            encResult.ivHex,
            phone
        )
        assertEquals(plainText, decrypted)
    }
}
