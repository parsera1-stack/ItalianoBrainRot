package com.brainrot.italiano.ui.screens.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.brainrot.italiano.R
import com.brainrot.italiano.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardLevel1.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToQuiz(level = 1)
            )
        }

        binding.cardLevel2.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToQuiz(level = 2)
            )
        }

        binding.cardLevel3.setOnClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeToQuiz(level = 3)
            )
        }

        binding.cardLevel4.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_spellingQuizFragment
            )
        }

        binding.btnParent.setOnClickListener {
            showPinDialog()
        }
    }

    private fun showPinDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 0)
        }

        val etPin = EditText(requireContext()).apply {
            hint = "Введите PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }

        layout.addView(etPin)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Родительский раздел")
            .setMessage("Введите PIN-код для доступа")
            .setView(layout)
            .setPositiveButton("Войти") { _, _ ->
                val pin = etPin.text.toString()
                if (pin == "5005") {
                    findNavController().navigate(
                        R.id.action_home_to_parent
                    )
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Ошибка")
                        .setMessage("Неверный PIN-код")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
