package net.palatzky.microvault.encryption.symmetric

import net.palatzky.microvault.encryption.Encryption
import java.nio.ByteBuffer
import java.security.Key
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * Aes gcm encryption
 *
 * @property key
 * @constructor Create empty Aes gcm encryption
 */
class AesGcmEncryption(
	override val key: Key,
) : Encryption {

	companion object {
		const val GCM_IV_LENGTH = 12
	}

	override fun encrypt(content: String, authenticationData: String?): ByteArray {
		val cipher = Cipher.getInstance("AES/GCM/NoPadding")

		val gcmData = ByteArray(AesGcmEncryption.GCM_IV_LENGTH) //NEVER REUSE THIS IV WITH SAME KEY
		SecureRandom().nextBytes(gcmData)

		val parameterSpec = GCMParameterSpec(128, gcmData);
		cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec)

		if (authenticationData != null) {
			cipher.updateAAD(authenticationData.toByteArray(Charsets.UTF_8));
		}

		val cipherText = cipher.doFinal(content.toByteArray(Charsets.UTF_8))

		val byteBuffer = ByteBuffer.allocate(gcmData.size + cipherText.size)
		byteBuffer.put(gcmData)
		byteBuffer.put(cipherText)
		return byteBuffer.array()
	}
}