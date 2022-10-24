package net.palatzky.microvault.service

import net.palatzky.microvault.encryption.*
import net.palatzky.microvault.encryption.asymmetric.RsaEcbDecryption
import net.palatzky.microvault.encryption.asymmetric.RsaEcbEncryption
import net.palatzky.microvault.encryption.symmetric.AesGcmDecryption
import net.palatzky.microvault.encryption.symmetric.AesGcmEncryption
import net.palatzky.microvault.session.Session
import net.palatzky.microvault.util.*
import net.palatzky.microvault.vault.DecryptionDecorator
import net.palatzky.microvault.vault.EncryptionDecorator
import net.palatzky.microvault.vault.MicroVault
import net.palatzky.microvault.vault.Vault
import net.palatzky.microvault.vault.option.MicroOptions
import net.palatzky.microvault.vault.option.Options
import net.palatzky.microvault.vault.serialization.VaultFactory
import net.palatzky.microvault.vault.serialization.VaultSerializer
import java.nio.file.Files
import java.nio.file.Path
import java.security.Key
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.enterprise.context.Dependent

@Dependent
class VaultService {

	companion object {
		enum class ListFormat {
			TABLE, PLAIN
		}
	}

	private var vault: Vault? = null
	private var options: Options? = null

	fun publish() {
	}

	fun get(key: String): String? {
		val vault = this.vault ?: throw Exception("Vault is not open yet.")
		return vault.get(key)
	}

	fun set(key: String, value: String) {
		val vault = this.vault ?: throw Exception("Vault is not open yet.")
		vault.set(key, value)
	}

	fun open(path: Path, key: Key?) {
		val factory = VaultFactory.fromFile(path, key)

		this.vault = factory.vault
		this.options = factory.options
	}

	fun close() {

	}

	fun list(format: ListFormat): String {
		val vault = this.vault ?: throw Exception("Vault is not open yet.")

		if (format == ListFormat.PLAIN) {
			return vault.entries.map {
				it.key + "    " + it.value
			}.joinToString("\n")
		}
		var keyLength = 0
		var valueLength = 0
		vault.entries.forEach {
			keyLength = keyLength.coerceAtLeast(it.key.length)
			valueLength = valueLength.coerceAtLeast(it.value.length)
		}

		val rows = vault.entries.flatMap {

			if (it.key.length <= 40 && it.value.length <= 60) {
				return@flatMap listOf(it.key to it.value)
			}
			var key = it.key
			var value = it.value

			val rows = mutableListOf<Pair<String, String>>()
			while (key.isNotEmpty() || value.isNotEmpty()) {
				val pairKey = if (key.isNotEmpty()) {
					val keyPart = key.substring(0, 40.coerceAtMost(key.length))
					key = key.substring(40.coerceAtMost(key.length))
					keyPart
				} else ""


				val pairValue = if (value.isNotEmpty()) {
					val valuePart = value.substring(0, 60.coerceAtMost(value.length))
					value = value.substring(60.coerceAtMost(value.length))
					valuePart
				} else ""

				rows.add(pairKey to pairValue)
			}

			return@flatMap rows
		}

		return ("Key".padEnd(40, ' ') + "    " + "Value".padEnd(60, ' ') + '\n')+
				"=".padEnd(104, '=') + "\n" +
				rows.joinToString("\n") {
					it.first.padEnd(40, ' ') + "    " + it.second.padEnd(60, ' ')
				}
	}

	fun write(path: Path) {
		val vault = this.vault ?: throw Exception("Vault is not open yet.")
		val options = this.options ?: throw Exception("Vault is not open yet.")

		val vaultSerializer = VaultSerializer()
		val stream = Files.newOutputStream(path)
		vaultSerializer.serialize(vault, options, stream)
	}

	fun create(path: Path, key: Key, mode: Options.EncryptionMode) {
		// create write/read key depending on encryption mode.
		val (decryptionKey, encryptionKey) = createReadWriteKey(mode)

		// create decryption/encryption depending on mode
		val decryption = createDecryption(mode, decryptionKey)
		val encryption = createEncryption(mode, encryptionKey)

		val salt = createRandomSalt()
		val vault = EncryptionDecorator(encryption, DecryptionDecorator(decryption, MicroVault()))
		vault.set("exampleKey", "exampleValue")

		val options = MicroOptions(
			salt = salt,
			mode = mode,
			encryptionKey = encryptionKey,
			decryptionKey = decryptionKey,
			pbeKey = key
		)

		this.vault = vault
		this.options = options
		this.write(path)
	}

}

// --password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault create --mode=asymmetric
// --password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault open

// --password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault set exampleKey exampleValue2
// --password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault get exampleKey

// --file=C:\Users\kevin\workspace\microsecrets\micro.vault set exampleKey3 exampleValue3
// --password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault get exampleKey3

//--password=password --file=C:\Users\kevin\workspace\microsecrets\micro.vault list --format=TABLE
//--file=C:\Users\kevin\workspace\microsecrets\micro.vault list --format=TABLE