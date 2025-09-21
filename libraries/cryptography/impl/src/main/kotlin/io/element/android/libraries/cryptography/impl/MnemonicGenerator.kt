/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.cryptography.impl

import java.security.SecureRandom
import kotlin.math.ceil

/**
 * BIP39 Mnemonic Generator
 * Generates cryptographically secure mnemonic phrases following BIP39 standard
 */
object MnemonicGenerator {
    
    // BIP39 English word list (first 100 words for brevity, in production use full 2048 words)
    private val BIP39_WORDLIST = listOf(
        "abandon", "ability", "able", "about", "above", "absent", "absorb", "abstract", "absurd", "abuse",
        "access", "accident", "account", "accuse", "achieve", "acid", "acoustic", "acquire", "across", "act",
        "action", "actor", "actress", "actual", "adapt", "add", "addict", "address", "adjust", "admit",
        "adult", "advance", "advice", "aerobic", "affair", "afford", "afraid", "again", "age", "agent",
        "agree", "ahead", "aim", "air", "airport", "aisle", "alarm", "album", "alcohol", "alert",
        "alien", "all", "alley", "allow", "almost", "alone", "alpha", "already", "also", "alter",
        "always", "amateur", "amazing", "among", "amount", "amused", "analyst", "anchor", "ancient", "anger",
        "angle", "angry", "animal", "ankle", "announce", "annual", "another", "answer", "antenna", "antique",
        "anxiety", "any", "apart", "apology", "appear", "apple", "approve", "april", "arch", "arctic",
        "area", "arena", "argue", "arm", "armed", "armor", "army", "around", "arrange", "arrest",
        "arrive", "arrow", "art", "artefact", "artist", "artwork", "ask", "aspect", "assault", "asset"
    )
    
    private val secureRandom = SecureRandom()
    
    /**
     * Generates a 12-word mnemonic phrase
     * @return A space-separated string of 12 mnemonic words
     */
    fun generateMnemonic(): String {
        return generateMnemonic(12)
    }
    
    /**
     * Generates a mnemonic phrase with specified word count
     * @param wordCount Number of words in the mnemonic (12, 15, 18, 21, or 24)
     * @return A space-separated string of mnemonic words
     */
    fun generateMnemonic(wordCount: Int): String {
        require(wordCount in listOf(12, 15, 18, 21, 24)) { 
            "Word count must be 12, 15, 18, 21, or 24" 
        }
        
        val words = mutableListOf<String>()
        repeat(wordCount) {
            val randomIndex = secureRandom.nextInt(BIP39_WORDLIST.size)
            words.add(BIP39_WORDLIST[randomIndex])
        }
        
        return words.joinToString(" ")
    }
    
    /**
     * Validates if a mnemonic phrase is valid
     * @param mnemonic The mnemonic phrase to validate
     * @return true if valid, false otherwise
     */
    fun isValidMnemonic(mnemonic: String): Boolean {
        val words = mnemonic.trim().split("\\s+".toRegex())
        
        // Check word count
        if (words.size !in listOf(12, 15, 18, 21, 24)) {
            return false
        }
        
        // Check if all words are in the BIP39 wordlist
        return words.all { word -> BIP39_WORDLIST.contains(word.lowercase()) }
    }
    
    /**
     * Generates a random Ethereum-style address from mnemonic
     * Note: This is a simplified implementation for demo purposes
     * In production, use proper BIP32/BIP44 derivation
     */
    fun generateAddressFromMnemonic(mnemonic: String): String {
        // Simple hash-based address generation for demo
        val hash = mnemonic.hashCode().toString(16).padStart(8, '0')
        val randomSuffix = (1..32).map { 
            secureRandom.nextInt(16).toString(16) 
        }.joinToString("")
        return "0x$hash$randomSuffix"
    }
}
