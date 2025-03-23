package com.example.studentportal.ui.brs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentportal.databinding.FragmentBrsBinding
import com.example.studentportal.R
import com.example.studentportal.data.model.Discipline
import com.example.studentportal.data.model.Semester
import com.example.studentportal.ui.brs.adapter.DisciplineAdapter
import com.example.studentportal.ui.utils.TokenManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BRSFragment : Fragment() {
    private var _binding: FragmentBrsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: BRSViewModel by viewModels { BRSViewModelFactory(requireContext().applicationContext) }
    private lateinit var semesters: List<Semester>
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnNext: ImageButton
    private var currentSemesterIndex = 0
    private lateinit var tvSemesterNumber: TextView
    private lateinit var tvCourse: TextView
    private lateinit var tvYear: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBrsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnPrevious = view.findViewById(R.id.btnPrevious)
        btnNext = view.findViewById(R.id.btnNext)
        tvSemesterNumber = view.findViewById(R.id.tvSemesterNumber)
        tvCourse = view.findViewById(R.id.tvCourse)
        tvYear = view.findViewById(R.id.tvYear)

        btnPrevious.setOnClickListener { showPreviousSemester() }
        btnNext.setOnClickListener { showNextSemester() }

        binding.logoutButton.setOnClickListener {
            performLogout()
        }
        setupRecyclerView()
        setupObservers()
        setupUI()

        if (TokenManager.isLoggedIn(requireContext())) {
            loadData()
        } else {
            showLoginDialog()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewDisciplines.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = DisciplineAdapter(emptyList()) { disciplineId ->
                findNavController().navigate(
                    BRSFragmentDirections.actionBrsToDetail(disciplineId)
                )
            }
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            errorMessage.observe(viewLifecycleOwner) { showError(it) }
            isLoading.observe(viewLifecycleOwner) { updateLoadingState(it) }
            semesters.observe(viewLifecycleOwner) { setupSemesterSpinner(it) }
            disciplines.observe(viewLifecycleOwner) { updateDisciplinesList(it) }
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                updateLoadingState(isLoading)
                if (!isLoading) {
                    binding.swipeRefresh.isRefreshing = false
                }
            }
        }
    }

    private fun setupUI() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.visibility = View.GONE
            viewModel.refreshData()
        }
    }

    private fun loadData() {
        viewModel.loadSemesters()
    }

    private fun updateLoadingState(isLoading: Boolean) {
        with(binding) {
            if (isLoading) {
                progressBar.show()
            } else {
                progressBar.hide()
            }
            swipeRefresh.visibility = if (isLoading) View.GONE else View.VISIBLE
            swipeRefresh.isRefreshing = false
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateDisciplinesList(disciplines: List<Discipline>) {
        binding.apply {
            if (disciplines.isEmpty()) {
                tvEmpty.visibility = if (semesters.isEmpty()) View.VISIBLE else View.GONE
                recyclerViewDisciplines.visibility = View.GONE
            } else {
                recyclerViewDisciplines.visibility = View.VISIBLE
                tvEmpty.visibility = View.GONE
                (recyclerViewDisciplines.adapter as? DisciplineAdapter)?.updateItems(disciplines)

            }
        }
    }

    private fun setupSemesterSpinner(semesters: List<Semester>) {
        this.semesters = semesters
        currentSemesterIndex = viewModel.spinnerPosition.value ?: 0
        updateSemesterDisplay()
    }

    private fun showPreviousSemester() {
        if (currentSemesterIndex > 0) {
            currentSemesterIndex--
            updateSemesterDisplay()
        }
    }

    private fun showNextSemester() {
        if (currentSemesterIndex < semesters.size - 1) {
            currentSemesterIndex++
            updateSemesterDisplay()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateSemesterDisplay() {
        if (semesters.isNotEmpty() && currentSemesterIndex in semesters.indices) {
            val currentSemester = semesters[currentSemesterIndex]

            val absoluteSemesterNumber = semesters.size - currentSemesterIndex
            tvSemesterNumber.text = "$absoluteSemesterNumber семестр"
            tvCourse.text = "${(absoluteSemesterNumber + 1) / 2} курс"
            tvYear.text = "${currentSemester.year} год"

            // Сохраняем позицию и загружаем дисциплины
            viewModel.selectedSemesterId = currentSemester.id
            viewModel.saveSpinnerPosition(currentSemesterIndex)
            viewModel.loadDisciplines()

            btnPrevious.isEnabled = currentSemesterIndex > 0
            btnNext.isEnabled = currentSemesterIndex < semesters.size - 1
        }
    }

    private fun showLoginDialog() {
        if (TokenManager.isLoggedIn(requireContext())) return

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login, null)
        val etLogin = dialogView.findViewById<EditText>(R.id.etLogin)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Авторизация в БРС")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Войти") { _, _ ->
                val login = etLogin.text.toString()
                val password = etPassword.text.toString()
                if (login.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(login, password)
                }
            }
            .setNegativeButton("Отмена") { _, _ ->
                parentFragmentManager.popBackStack()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun performLogout() {
        TokenManager.clearToken(requireContext())
        viewModel.clearData()
        showLoginDialog()
        clearUI()
    }

    override fun onResume() {
        super.onResume()
        if (!TokenManager.isLoggedIn(requireContext())) {
            showLoginDialog()
        }
    }

    private fun clearUI() {
        binding.recyclerViewDisciplines.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        semesters = emptyList()
        setupSemesterSpinner(emptyList())
    }
}