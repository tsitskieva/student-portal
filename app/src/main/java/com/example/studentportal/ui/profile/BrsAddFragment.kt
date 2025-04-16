package com.example.studentportal.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studentportal.R
import com.example.studentportal.data.model.Brs
import com.example.studentportal.ui.profile.managers.SelectedBrsManager

class BrsAddFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Убедитесь что используется правильный layout для фрагмента
        return inflater.inflate(R.layout.fragment_brs_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Обработка WindowInsets (если нужно)
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                systemBars.top,
                v.paddingRight,
                systemBars.bottom
            )
            insets
        }

        // Инициализация элементов UI
        val backButton = view.findViewById<ImageView>(R.id.back_to_group_settings)
        val nameInput = view.findViewById<EditText>(R.id.name_of_brs)
        val codeInput = view.findViewById<EditText>(R.id.code_of_brs)
        val addButton = view.findViewById<Button>(R.id.button_to_list_of_brs)

        // Обработка кнопки назад
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        // Обработка кнопки добавления
        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val code = codeInput.text.toString().trim()

            if (name.isEmpty() || code.isEmpty()) {
                showToast("Заполните все поля")
                return@setOnClickListener
            }

            val selectedBrs = SelectedBrsManager.getSelectedBrs(requireContext())
            if (selectedBrs.any { it.code == code }) {
                showToast("Этот код БРС уже добавлен")
                return@setOnClickListener
            }

            val newBrs = Brs(
                name = name,
                code = code,
                isActive = selectedBrs.isEmpty()
            )

            SelectedBrsManager.saveSelectedBrs(
                requireContext(),
                selectedBrs.toMutableList().apply { add(newBrs) }
            )

            // Возврат назад после успешного добавления
            findNavController().navigateUp()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}