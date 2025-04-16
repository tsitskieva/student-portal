package com.example.studentportal.ui.brs

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studentportal.databinding.FragmentBrsBinding
import com.example.studentportal.R
import com.example.studentportal.data.model.Discipline
import com.example.studentportal.data.model.Semester
import com.example.studentportal.ui.brs.adapter.DisciplineAdapter
import com.example.studentportal.ui.profile.managers.SelectedBrsManager

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
    private val sharedViewModel: SharedViewModel by activityViewModels()

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

        sharedViewModel.refreshBrs.observe(viewLifecycleOwner) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.clearData()
                checkBrsAvailability()
            }
        }

        setupRecyclerView()
        setupObservers()
        setupUI()
        checkBrsAvailability()
    }

    private fun checkBrsAvailability() {
        val brsList = SelectedBrsManager.getSelectedBrs(requireContext())
        if (brsList.isEmpty()) {
            viewModel.clearData()
            showEmptyBrsState()
        } else {
            loadData()
        }
    }

    private fun showEmptyBrsState() {
        binding.apply {
            semesterSelector.root.visibility = View.GONE
            recyclerViewDisciplines.visibility = View.GONE
            emptyState.visibility = View.VISIBLE

            // Добавляем кнопку для перехода к добавлению БРС
            btnAddBrs.setOnClickListener {
                findNavController().navigate(R.id.action_brs_to_brsSettings)
            }
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
                if (semesters.isEmpty()) {
                    emptyState.visibility = View.VISIBLE
                    semesterSelector.root.visibility = View.GONE
                } else {
                    emptyState.visibility = View.GONE
                    semesterSelector.root.visibility = View.VISIBLE
                }
                recyclerViewDisciplines.visibility = View.GONE
            } else {
                emptyState.visibility = View.GONE
                semesterSelector.root.visibility = View.VISIBLE
                recyclerViewDisciplines.visibility = View.VISIBLE
                (recyclerViewDisciplines.adapter as? DisciplineAdapter)?.updateItems(disciplines)
            }
        }
    }

    private fun setupSemesterSpinner(semesters: List<Semester>) {
        this.semesters = semesters
        binding.semesterSelector.root.visibility = if (semesters.isNotEmpty()) View.VISIBLE else View.GONE
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    private fun clearUI() {
        binding.recyclerViewDisciplines.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        semesters = emptyList()
        setupSemesterSpinner(emptyList())
    }
}