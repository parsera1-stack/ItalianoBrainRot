package com.brainrot.italiano.domain.usecase

import com.brainrot.italiano.data.repository.WordRepository
import com.brainrot.italiano.domain.model.QuestionDirection
import com.brainrot.italiano.domain.model.QuestionType
import com.brainrot.italiano.domain.model.QuizQuestion
import javax.inject.Inject

class GenerateSpellingQuestionUseCase @Inject constructor(
    private val repository: WordRepository
) {

    suspend operator fun invoke(): Result<QuizQuestion> {
        val words = repository.getRandomActiveWordsByPriority(1)
        if (words.isEmpty()) {
            return Result.failure(Exception("Нет доступных слов"))
        }

        val word = words.first()
        val correctAnswer = word.english
        val distractors = generateDistractors(correctAnswer)
        val options = (listOf(correctAnswer) + distractors).shuffled()

        return Result.success(
            QuizQuestion(
                word = word,
                questionText = word.russian,
                correctAnswer = correctAnswer,
                options = options,
                questionDirection = QuestionDirection.RUSSIAN_TO_ENGLISH,
                questionType = QuestionType.SPELLING
            )
        )
    }

    private fun generateDistractors(word: String): List<String> {
        val distractors = mutableSetOf<String>()
        val attempts = mutableListOf<() -> String?>()

        attempts.add { deleteDoubleLetter(word) }
        attempts.add { insertExtraLetter(word) }
        attempts.add { substituteLetter(word) }
        attempts.add { transposeLetters(word) }
        attempts.add { deleteSilentLetter(word) }
        attempts.add { suffixError(word) }
        attempts.add { swapIE(word) }

        val prioritized = attempts.shuffled()

        for (attempt in prioritized) {
            if (distractors.size >= 3) break
            val result = attempt()
            if (result != null && result != word && result !in distractors && result.length >= 3) {
                distractors.add(result)
            }
        }

        while (distractors.size < 3) {
            val randomDistractor = generateRandomDistractor(word, distractors)
            if (randomDistractor != null && randomDistractor !in distractors) {
                distractors.add(randomDistractor)
            }
        }

        return distractors.take(3)
    }

    private fun deleteDoubleLetter(word: String): String? {
        val doubleIndices = mutableListOf<Int>()
        for (i in 0 until word.length - 1) {
            if (word[i].lowercaseChar() == word[i + 1].lowercaseChar()) {
                doubleIndices.add(i)
            }
        }
        if (doubleIndices.isEmpty()) return null
        val indexToRemove = doubleIndices.random()
        return word.removeRange(indexToRemove, indexToRemove + 1)
    }

    private fun insertExtraLetter(word: String): String? {
        if (word.length < 3) return null
        val validPositions = (1 until word.length - 1).filter { word[it] != ' ' }
        if (validPositions.isEmpty()) return null
        val pos = validPositions.random()
        val charToDouble = word[pos]
        return word.substring(0, pos) + charToDouble + word.substring(pos)
    }

    private fun substituteLetter(word: String): String? {
        val substitutions = mapOf(
            'o' to listOf('a', 'u'),
            'O' to listOf('A', 'U'),
            'a' to listOf('o', 'u'),
            'A' to listOf('O', 'U'),
            'u' to listOf('o', 'a'),
            'U' to listOf('O', 'A'),
            'c' to listOf('k', 's'),
            'C' to listOf('K', 'S'),
            'k' to listOf('c', 's'),
            'K' to listOf('C', 'S'),
            's' to listOf('c', 'k'),
            'S' to listOf('C', 'K'),
            'i' to listOf('e'),
            'I' to listOf('E'),
            'e' to listOf('i'),
            'E' to listOf('I'),
            'n' to listOf('m'),
            'N' to listOf('M'),
            'w' to listOf('v'),
            'W' to listOf('V'),
            'v' to listOf('w'),
            'V' to listOf('W')
        )

        val applicableIndices = word.indices.filter { word[it] in substitutions.keys }
        if (applicableIndices.isEmpty()) return null

        val pos = applicableIndices.random()
        val original = word[pos]
        val replacements = substitutions[original] ?: return null
        val replacement = replacements.random()

        return word.substring(0, pos) + replacement + word.substring(pos + 1)
    }

    private fun transposeLetters(word: String): String? {
        if (word.length < 4) return null
        val pos = (1 until word.length - 2).random()
        val chars = word.toCharArray()
        val temp = chars[pos]
        chars[pos] = chars[pos + 1]
        chars[pos + 1] = temp
        return String(chars)
    }

    private fun deleteSilentLetter(word: String): String? {
        val silentPatterns = listOf(
            "kn" to "n",
            "wr" to "r",
            "gn" to "n",
            "gh" to "",
            "ph" to "f",
            "mb" to "m"
        )

        for ((pattern, replacement) in silentPatterns) {
            if (word.contains(pattern, ignoreCase = true)) {
                return word.replaceFirst(pattern, replacement, ignoreCase = true)
            }
        }
        return null
    }

    private fun suffixError(word: String): String? {
        val suffixReplacements = listOf(
            "le" to "el",
            "el" to "le",
            "y" to "ie",
            "ie" to "y",
            "tion" to "sion",
            "sion" to "tion"
        )

        for ((from, to) in suffixReplacements) {
            if (word.endsWith(from, ignoreCase = true)) {
                return word.dropLast(from.length) + to
            }
        }
        return null
    }

    private fun swapIE(word: String): String? {
        when {
            word.contains("ie", ignoreCase = true) -> {
                return word.replace("ie", "ei", ignoreCase = true)
            }
            word.contains("ei", ignoreCase = true) -> {
                return word.replace("ei", "ie", ignoreCase = true)
            }
        }
        return null
    }

    private fun generateRandomDistractor(word: String, existing: Set<String>): String? {
        val methods = listOf(
            { deleteDoubleLetter(word) },
            { insertExtraLetter(word) },
            { substituteLetter(word) },
            { transposeLetters(word) }
        )

        repeat(5) {
            val result = methods.random().invoke()
            if (result != null && result != word && result !in existing) {
                return result
            }
        }
        return null
    }
}
