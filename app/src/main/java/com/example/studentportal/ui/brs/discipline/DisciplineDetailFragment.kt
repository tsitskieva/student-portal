package com.example.studentportal.ui.brs.discipline

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studentportal.R
import com.example.studentportal.data.model.Module
import com.example.studentportal.databinding.FragmentDisciplineDetailBinding
import com.example.studentportal.ui.brs.adapter.ModuleAdapter

class DisciplineDetailFragment : Fragment() {
    private lateinit var binding: FragmentDisciplineDetailBinding
    private val viewModel: DisciplineDetailViewModel by viewModels {
        DisciplineDetailViewModelFactory(requireContext().applicationContext)
    }
    private val args: DisciplineDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDisciplineDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val disciplineId = args.disciplineId

        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.loadDisciplineDetails(disciplineId)
        observeViewModel()
        setupScrollBehavior()
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        viewModel.disciplineDetails.observe(viewLifecycleOwner) { details ->
            if (details == null) {
                showErrorMessage("Данные не найдены")
                return@observe
            }

            binding.apply {

                val (total, maxTotal, admission) = calculateTotalScores(details.modules, details.exam)

                tvTitle.text = details.name
                tvFixedCurrentScore.text = details.score.toString()
                tvFixedMaxScore.text = details.maxScore.toString()
                rvModules.adapter = ModuleAdapter(details.modules)

                val finalView = binding.finalRatingCard.findViewById<View>(R.id.included_submodule_final)

                finalView.findViewById<TextView>(R.id.tvCurrentScore).text = total.toString()
                finalView.findViewById<TextView>(R.id.tvMaxScore).text = maxTotal.toString()

                val progress = when {
                    maxTotal > 0 -> {
                        if (total == 0) 0.01f else total.toFloat() / maxTotal.toFloat()
                    }
                    total > 0 -> 1f
                    else -> 0.01f
                }

                val progressParams = progressIndicatorFixed.layoutParams as LinearLayout.LayoutParams
                progressParams.weight = progress
                progressIndicatorFixed.layoutParams = progressParams

                val progressView = finalView.findViewById<View>(R.id.progressIndicator)
                val params = progressView.layoutParams as LinearLayout.LayoutParams
                params.weight = progress
                progressView.layoutParams = params

                val color = when {
                    maxTotal == 0 || total == 0 -> Color.parseColor("#FF4444")
                    total < 0.6 * maxTotal -> Color.parseColor("#FF4444")
                    total < 0.7 * maxTotal -> Color.parseColor("#FFC107")
                    else -> Color.parseColor("#4CAF50")
                }
                progressIndicatorFixed.setBackgroundColor(color)
                progressView.setBackgroundColor(color)

                details.exam?.let {
                    showExamCard(it)
                    showAdmissionCard(admission.first, admission.second)
                }

                // Показываем все элементы
                tvTitle.visibility = View.VISIBLE
                tvFixedCurrentScore.visibility = View.VISIBLE
                tvFixedMaxScore.visibility = View.VISIBLE
                rvModules.visibility = View.VISIBLE

            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let { showErrorMessage(it) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showExamCard(exam: Module) {
        binding.examCard.apply {
            visibility = View.VISIBLE
            val examView = findViewById<View>(R.id.included_submodule_exam)
            examView.findViewById<TextView>(R.id.tvTitle).text = "Промежуточный итог"

            val totalScore = exam.submodules.sumOf { it.score }
            val maxScore = exam.submodules.sumOf { it.maxScore }

            examView.findViewById<TextView>(R.id.tvCurrentScore).text = totalScore.toString()
            examView.findViewById<TextView>(R.id.tvMaxScore).text = maxScore.toString()

            val progress = when {
                maxScore > 0 -> {
                    if (totalScore == 0) 0.01f else totalScore.toFloat() / maxScore
                }
                totalScore > 0 -> 1f
                else -> 0.01f
            }
            val progressView = examView.findViewById<View>(R.id.progressIndicator)
            val params = progressView.layoutParams as LinearLayout.LayoutParams
            params.weight = progress
            progressView.layoutParams = params

            val color = when {
                maxScore == 0 || totalScore == 0 -> Color.parseColor("#FF4444")
                totalScore < 22 -> Color.parseColor("#FF4444")
                totalScore < 30 -> Color.parseColor("#FFC107")
                else -> Color.parseColor("#4CAF50")
            }
            progressView.setBackgroundColor(color)
        }
    }

    private fun calculateTotalScores(modules: List<Module>, exam: Module?): Triple<Int, Int, Pair<Int, Int>> {
        var totalScore = 0
        var maxTotalScore = 0
        var admissionScore = 0
        var admissionMax = 0

        modules.forEach { module ->
            module.submodules.forEach { sub ->
                totalScore += sub.score
                maxTotalScore += sub.maxScore
                if (module.type != "EXAM") {
                    admissionScore += sub.score
                    admissionMax += sub.maxScore
                }
            }
        }

        exam?.submodules?.forEach { sub ->
            totalScore += sub.score
            maxTotalScore += sub.maxScore
        }

        return Triple(totalScore, maxTotalScore, Pair(admissionScore, admissionMax))
    }

    @SuppressLint("SetTextI18n")
    private fun showAdmissionCard(admissionScore: Int, admissionMax: Int) {
        binding.admissionCard.apply {
            visibility = View.VISIBLE
            val admissionView = findViewById<View>(R.id.included_submodule_admission)
            admissionView .findViewById<TextView>(R.id.tvTitle).text = "Промежуточный итог"

            admissionView.findViewById<TextView>(R.id.tvCurrentScore).text = admissionScore.toString()
            admissionView.findViewById<TextView>(R.id.tvMaxScore).text = admissionMax.toString()

            val progress = when {
                admissionMax > 0 -> {
                    if (admissionScore == 0) 0.01f else admissionScore.toFloat() / admissionMax
                }
                admissionScore > 0 -> 1f
                else -> 0.01f
            }

            val progressView = admissionView.findViewById<View>(R.id.progressIndicator)
            val params = progressView.layoutParams as LinearLayout.LayoutParams
            params.weight = progress
            progressView.layoutParams = params

            val color = when {
                admissionMax == 0 || admissionScore == 0 -> "#FF4444"
                admissionScore < 38 -> "#FF4444"
                admissionScore < 45 -> "#FFC107"
                else -> "#4CAF50"
            }
            progressView.setBackgroundColor(Color.parseColor(color))
        }
    }

    private fun setupScrollBehavior() {
        binding.nestedScrollView.viewTreeObserver.addOnGlobalLayoutListener {
            updateFixedScoreVisibility()
        }

        binding.nestedScrollView.setOnScrollChangeListener { _, _, _, _, _ ->
            updateFixedScoreVisibility()
        }
    }

    private fun updateFixedScoreVisibility() {
        val scrollView = binding.nestedScrollView
        val child = scrollView.getChildAt(0)
        val isContentScrollable = child.height > scrollView.height
        val scrollRange = child.height - scrollView.height
        val currentScroll = scrollView.scrollY

        val isAtBottom = currentScroll >= scrollRange - 2

        binding.fixedScoreView.visibility = if (isContentScrollable && !isAtBottom) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun showErrorMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}