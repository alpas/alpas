/*
 * Copyright © 2019, PearX Team
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.alpas.extensions

private val BOUNDARIES = arrayOf(' ', '-', '_', '.')

private fun StringBuilder.toStringAndClear() = toString().also { clear() }

/**
 * Splits a string to multiple words by using the following rules:
 * - All ' ', '-', '_', '.' characters are considered word boundaries.
 * - If a lowercase character is followed by an uppercase character, a word boundary is considered to be prior to the uppercase character.
 * - If multiple uppercase characters are followed by a lowercase character, a word boundary is considered to be prior to the last uppercase character.
 *
 * Examples:
 * - XMLBufferedReader => XML|Buffered|Reader
 * - newFile => new|File
 * - net.pearx.lib => net|pearx|lib
 * - NewDataClass => New|Data|Class
 */
fun String.splitToWords(): List<String> = mutableListOf<String>().also { list ->
    val word = StringBuilder()
    for (index in 0 until length) {
        val char = this[index]
        if (char in BOUNDARIES) {
            list.add(word.toStringAndClear())
        } else {
            if (char.isUpperCase()) {
                if ((index > 0 && this[index - 1].isLowerCase()) ||
                    (index > 0 && index < length - 1 && this[index - 1].isUpperCase() && this[index + 1].isLowerCase())
                ) {
                    list.add(word.toStringAndClear())
                }
            }
            word.append(char)
        }
    }
    list.add(word.toStringAndClear())
}
