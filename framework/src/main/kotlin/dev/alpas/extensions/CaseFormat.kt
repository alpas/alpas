/*
 * Copyright © 2019, PearX Team
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package dev.alpas.extensions

/**
 * An enumeration that represents a case format that can be used to join a collection of words into one string.
 */
enum class CaseFormat(
    private val wordUppercase: Boolean,
    private val wordSplitter: Char?,
    private val wordCapitalize: Boolean = false,
    private val firstWordCapitalize: Boolean = false
) {
    /** SCREAMING_SNAKE_CASE */
    UPPER_UNDERSCORE(true, '_'),
    /** snake_case */
    LOWER_UNDERSCORE(false, '_'),
    /** PascalCase */
    CAPITALIZED_CAMEL(false, null, true, true),
    /** camelCase */
    CAMEL(false, null, true, false),
    /** TRAIN-CASE */
    UPPER_HYPHEN(true, '-'),
    /** kebab-case */
    LOWER_HYPHEN(false, '-'),
    /** UPPER SPACE CASE */
    UPPER_SPACE(true, ' '),
    /** Title Case */
    CAPITALIZED_SPACE(false, ' ', true, true),
    /** lower space case */
    LOWER_SPACE(false, ' '),
    /** UPPER.DOT.CASE */
    UPPER_DOT(true, '.'),
    /** dot.case */
    LOWER_DOT(false, '.');

    /**
     * Joins [words] using this case format, appending the result to [appendable]
     */
    fun formatTo(appendable: Appendable, words: Iterable<String>) {
        appendable.apply {
            for ((index, word) in words.withIndex()) {
                if (wordSplitter != null && index != 0)
                    append(wordSplitter)
                append(
                    when {
                        wordUppercase -> word.toUpperCase()
                        index == 0 -> {
                            when {
                                firstWordCapitalize -> word.toLowerCase().capitalize()
                                else -> word.toLowerCase()
                            }
                        }
                        wordCapitalize -> word.toLowerCase().capitalize()
                        else -> word.toLowerCase()
                    }
                )
            }
        }
    }

    /**
     * Joins [words] using this case format, appending the result to [appendable]
     */
    fun formatTo(appendable: Appendable, vararg words: String) = formatTo(appendable, words.asIterable())

    /**
     * Joins [words] using this case format and returns the result.
     */
    fun format(words: Iterable<String>): String = StringBuilder().also { formatTo(it, words) }.toString()

    /**
     * Joins [words] using this case format and returns the result.
     */
    fun format(vararg words: String) = format(words.asIterable())

    /**
     * Converts a string to this [CaseFormat] using the word splitting rules defined in [splitToWords]
     */
    fun format(string: String) = format(string.splitToWords())
}

