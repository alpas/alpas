/*
 * Copyright © 2019, PearX Team
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package dev.alpas.extensions

/**
 * Converts a string to specific [caseFormat] using the word splitting rules defined in [splitToWords].
 */
fun String.toCase(caseFormat: CaseFormat) = caseFormat.format(this)

/** Converts a string to SCREAMING_SNAKE_CASE using the word splitting rules defined in [splitToWords]. */
fun String.toScreamingSnakeCase() = toCase(CaseFormat.UPPER_UNDERSCORE)

/** Converts a string to snake_case using the word splitting rules defined in [splitToWords]. */
fun String.toSnakeCase() = toCase(CaseFormat.LOWER_UNDERSCORE)

/** Converts a string to PascalCase using the word splitting rules defined in [splitToWords]. */
fun String.toPascalCase() = toCase(CaseFormat.CAPITALIZED_CAMEL)

/** Converts a string to camelCase using the word splitting rules defined in [splitToWords]. */
fun String.toCamelCase() = toCase(CaseFormat.CAMEL)

/** Converts a string to TRAIN-CASE using the word splitting rules defined in [splitToWords]. */
fun String.toTrainCase() = toCase(CaseFormat.UPPER_HYPHEN)

/** Converts a string to kebab-case using the word splitting rules defined in [splitToWords]. */
fun String.toKebabCase() = toCase(CaseFormat.LOWER_HYPHEN)

/** Converts a string to UPPER SPACE CASE using the word splitting rules defined in [splitToWords]. */
fun String.toUpperSpaceCase() = toCase(CaseFormat.UPPER_SPACE)

/** Converts a string to Title Case using the word splitting rules defined in [splitToWords]. */
fun String.toTitleCase() = toCase(CaseFormat.CAPITALIZED_SPACE)

/** Converts a string to lower space case using the word splitting rules defined in [splitToWords]. */
fun String.toLowerSpaceCase() = toCase(CaseFormat.LOWER_SPACE)

/** Converts a string to UPPER.DOT.CASE using the word splitting rules defined in [splitToWords]. */
fun String.toUpperDotCase() = toCase(CaseFormat.UPPER_DOT)

/** Converts a string to dot.case using the word splitting rules defined in [splitToWords]. */
fun String.toDotCase() = toCase(CaseFormat.LOWER_DOT)
