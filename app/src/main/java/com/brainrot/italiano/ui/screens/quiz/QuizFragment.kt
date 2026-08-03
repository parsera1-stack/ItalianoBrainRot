package com.brainrot.italiano.ui.screens.quiz

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentQuizBinding
import com.brainrot.italiano.domain.model.QuizQuestion
import com.brainrot.italiano.ui.viewmodel.QuizState
import com.brainrot.italiano.ui.viewmodel.QuizViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: QuizViewModel by viewModels()
    private val args: QuizFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.startQuiz(args.level)
        observeQuizState()
        setupInputListeners()
        setupExitButton()
        setupNextButton()
    }

    private fun setupExitButton() {
        binding.btnExit.setOnClickListener {
            hideKeyboard()
            findNavController().popBackStack()
        }
    }

    private fun setupNextButton() {
        binding.btnNext.setOnClickListener {
            resetOptions()
            viewModel.loadNextQuestion()
        }
    }

    private fun observeQuizState() {
        viewModel.quizState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is QuizState.Loading -> showLoading()
                is QuizState.Question -> showQuestion(state.question)
                is QuizState.Correct -> showCorrect(state.question)
                is QuizState.Wrong -> showWrong(state.question, state.correctAnswer)
                is QuizState.Finished -> showFinished()
            }
        }
    }

    private fun showLoading() {
        binding.contentLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
    }

    private fun showQuestion(question: QuizQuestion) {
        binding.contentLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        resetOptions()

        // Убрана фраза "Как перевести:"
        binding.tvQuestion.text = question.questionText

        if (question.questionType == com.brainrot.italiano.domain.model.QuestionType.MULTIPLE_CHOICE) {
            showMultipleChoice(question)
        } else {
            showWrittenInput()
        }
    }

    private fun showMultipleChoice(question: QuizQuestion) {
        binding.layoutOptions.visibility = View.VISIBLE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        hideKeyboard()

        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { it.visibility = View.GONE }

        question.options.forEachIndexed { index, option ->
            if (index < buttons.size) {
                buttons[index].text = option
                buttons[index].visibility = View.VISIBLE
                buttons[index].isEnabled = true
                buttons[index].setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_coffee))
                buttons[index].setOnClickListener {
                    viewModel.submitMultipleChoiceAnswer(option)
                }
            }
        }
    }

    private fun showWrittenInput() {
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.VISIBLE
        binding.btnSubmit.visibility = View.VISIBLE
        binding.etAnswer.text?.clear()
        binding.etAnswer.requestFocus()
        showKeyboard()
    }

    private fun setupInputListeners() {
        binding.btnSubmit.setOnClickListener {
            val answer = binding.etAnswer.text.toString()
            if (answer.isNotBlank()) {
                hideKeyboard()
                viewModel.submitAnswer(answer)
            }
        }
    }

    private fun showCorrect(question: QuizQuestion) {
        binding.btnNext.visibility = View.VISIBLE
        if (question.questionType == com.brainrot.italiano.domain.model.QuestionType.MULTIPLE_CHOICE) {
            highlightCorrectAnswer(question.correctAnswer)
        }
    }

    private fun showWrong(question: QuizQuestion, correctAnswer: String) {
        binding.btnNext.visibility = View.VISIBLE
        if (question.questionType == com.brainrot.italiano.domain.model.QuestionType.MULTIPLE_CHOICE) {
            highlightWrongAnswer(question.correctAnswer)
        }
        // Убрано сообщение об ошибке - только цветные кнопки как в уровне 4
    }

    private fun highlightCorrectAnswer(correctAnswer: String) {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { btn ->
            btn.isEnabled = false
            if (btn.text.toString() == correctAnswer) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_green))
            }
        }
    }

    private fun highlightWrongAnswer(correctAnswer: String) {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { btn ->
            btn.isEnabled = false
            when {
                btn.text.toString() == correctAnswer -> {
                    btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success_green))
                }
                else -> {
                    btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error_red))
                }
            }
        }
    }

    private fun resetOptions() {
        val buttons = listOf(binding.btnOption1, binding.btnOption2, binding.btnOption3, binding.btnOption4)
        buttons.forEach { btn ->
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pastel_coffee))
            btn.isEnabled = true
        }
        binding.tvQuestion.text = ""
    }

    private fun showFinished() {
        hideKeyboard()
        binding.contentLayout.visibility = View.VISIBLE
        binding.tvQuestion.text = "Все слова выучены!"
        binding.layoutOptions.visibility = View.GONE
        binding.tilAnswer.visibility = View.GONE
        binding.btnSubmit.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.btnExit.visibility = View.VISIBLE
    }

    private fun showKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etAnswer, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        _binding = null
    }
}
