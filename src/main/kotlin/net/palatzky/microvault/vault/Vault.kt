package net.palatzky.microvault.vault

/**
 * Interface to describe a generic vault
 *
 * @constructor Create empty Vault
 */
interface Vault {

	val entries: Set<VaultEntry>;

	/**
	 * Returns the value for the given key
	 *
	 * @param key
	 * @return value of key
	 */
	fun get(key: String): String?

	/**
	 * Sets the value for the given key
	 *
	 * @param key
	 * @param value
	 */
	fun set(key: String, value: String)
}