package com.cointracker.mobile.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Matches Werkzeug's `generate_password_hash` default format: pbkdf2:sha256:260000$salt$hash
 */
class WerkzeugPasswordHasher(
    private val iterations: Int = 260_000,
    private val saltLength: Int = 16,
    private val dkLength: Int = 32
) {
    private val random = SecureRandom()

    fun hash(password: String): String {
        val saltBytes = ByteArray(saltLength)
        random.nextBytes(saltBytes)
        val derived = deriveKey(password, saltBytes, iterations, dkLength)
        val salt = Base64.encodeToString(saltBytes, Base64.NO_WRAP)
        val hash = Base64.encodeToString(derived, Base64.NO_WRAP)
        return "pbkdf2:sha256:$iterations$$salt$$hash"
    }

    fun verify(password: String, storedHash: String): Boolean {
        val parts = storedHash.split("$")
        if (parts.size != 3) return false
        val meta = parts[0].split(":")
        if (meta.size < 3) return false
        val iterationsPart = meta[2].toIntOrNull() ?: return false
        val saltBytes = Base64.decode(parts[1], Base64.NO_WRAP)
        val expected = Base64.decode(parts[2], Base64.NO_WRAP)
        val derived = deriveKey(password, saltBytes, iterationsPart, expected.size)
        return constantTimeEquals(expected, derived)
    }

    private fun deriveKey(password: String, salt: ByteArray, iterations: Int, dkLen: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, dkLen * 8)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}
