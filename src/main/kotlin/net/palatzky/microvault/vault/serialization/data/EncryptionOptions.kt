package net.palatzky.microvault.vault.serialization.data

import com.fasterxml.jackson.annotation.JsonInclude
import io.quarkus.runtime.annotations.RegisterForReflection
import net.palatzky.microvault.service.VaultService

/**
 * Encryption options
 *
 * @property mode
 * @property salt
 * @property readKey
 * @property writeKey
 * @property key
 * @constructor Create empty Encryption options
 */
@RegisterForReflection
data class EncryptionOptions(
	var mode: VaultService.EncryptionMode,
	var salt: String,
	var readKey: String? = null,
	var writeKey: String? = null,
	var key: String? = null
) {
	constructor(): this(VaultService.EncryptionMode.plain, "")
}
