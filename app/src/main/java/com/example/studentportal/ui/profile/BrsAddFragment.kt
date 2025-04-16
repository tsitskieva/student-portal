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
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
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
        return inflater.inflate(R.layout.fragment_brs_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handleWindowInsets(view)
        setupUIElements(view)
    }

    private fun setupUIElements(view: View) {
        val backButton = view.findViewById<ImageView>(R.id.back_to_group_settings)
        val nameInput = view.findViewById<EditText>(R.id.name_of_brs)
        val codeInput = view.findViewById<EditText>(R.id.code_of_brs)
        val addButton = view.findViewById<Button>(R.id.button_to_list_of_brs)

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        addButton.setOnClickListener {
            val name = nameInput.text.toString().trim()
            val code = codeInput.text.toString().trim()

            if (!validateInput(name, code)) return@setOnClickListener

            val newBrs = createBrs(name, code) ?: return@setOnClickListener

            if (isBrsAlreadyExists(newBrs.code)) {
                showToast("Этот код БРС уже добавлен")
                return@setOnClickListener
            }

            saveBrs(newBrs)
            notifyParentAndNavigateBack()
        }
    }

    private fun handleWindowInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                right = systemBars.right,
                bottom = systemBars.bottom
            )
            insets
        }
    }

    private fun validateInput(name: String, code: String): Boolean {
        if (name.isEmpty() || code.isEmpty()) {
            showToast("Заполните все поля")
            return false
        }
        return true
    }

    private fun createBrs(name: String, code: String): Brs? {
        return try {
            Brs(
                name = name,
                code = code,
                isActive = SelectedBrsManager.getSelectedBrs(requireContext()).isEmpty()
            )
        } catch (e: Exception) {
            showToast("Ошибка создания БРС")
            null
        }
    }

    private fun isBrsAlreadyExists(code: String): Boolean {
        return SelectedBrsManager.getSelectedBrs(requireContext())
            .any { it.code == code }
    }

    private fun saveBrs(newBrs: Brs) {
        val updatedList = SelectedBrsManager.getSelectedBrs(requireContext()).toMutableList().apply {
            add(newBrs)
        }
        SelectedBrsManager.saveSelectedBrs(requireContext(), updatedList)
    }

    private fun notifyParentAndNavigateBack() {
        setFragmentResult(REQUEST_KEY, Bundle().apply {
            putBoolean(RESULT_KEY, true)
        })
        findNavController().navigateUp()
    }


    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val REQUEST_KEY = "BrsAddFragmentRequest"
        const val RESULT_KEY = "brsAdded"
    }
}