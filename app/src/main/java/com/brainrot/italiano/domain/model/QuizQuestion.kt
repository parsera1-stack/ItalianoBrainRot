package com.brainrot.italiano.domain.model

/**
 * Модель вопроса для квиза
 */
data class QuizQuestion(
    val word: Word,
    val questionText: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val questionDirection: QuestionDirection,
    val questionType: QuestionType
)

enum class QuestionDirection {
    RUSSIAN_TO_ENGLISH,
    ENGLISH_TO_RUSSIAN
}

enum class QuestionType {
    MULTIPLE_CHOICE,
    WRITTEN,
    MIXED,
    SPELLING
}
