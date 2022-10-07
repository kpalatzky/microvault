package net.palatzky.microvault.vault

import net.palatzky.microvault.encryption.Decryption
import net.palatzky.microvault.encryption.Encryption
import java.nio.file.Path

class MicroVault : Vault {

	private lateinit var encryptor: Encryption
	private lateinit var decryptor: Decryption

	private val data: MutableMap<String, String> = mutableMapOf()

	override val entries: Set<VaultEntry>
		get() = data.entries.map {
			VaultEntry(it.key, it.value)
		}.toSet()

	override fun get(key: String): String? {
		return data[key]
	}

	override fun set(key: String, value: String) {
		this.data[key] = value
	}
}