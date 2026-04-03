package com.example.studentportal.ui.reference

import android.content.res.ColorStateList
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studentportal.R
import com.example.studentportal.data.model.ReferenceChatMessage
import com.example.studentportal.data.repository.ReferenceRepository
import android.widget.ImageView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.core.content.res.ResourcesCompat

class ReferenceFragment : Fragment() {
    private lateinit var rootScroll: NestedScrollView
    private lateinit var topicsSection: LinearLayout
    private lateinit var topicsTitle: TextView
    private lateinit var topicsGroup: GridLayout
    private lateinit var historyContainer: LinearLayout

    private lateinit var initialSection: LinearLayout
    private lateinit var initialInput: EditText
    private lateinit var initialSubmitButton: Button

    private lateinit var followUpSection: LinearLayout
    private lateinit var followUpInput: EditText
    private lateinit var followUpSendButton: ImageButton
    private lateinit var newQuestionButton: TextView

    private lateinit var loadingControls: LinearLayout
    private lateinit var cancelButton: TextView
    private lateinit var loadingQuestionText: TextView
    private lateinit var loadingBar1: View
    private lateinit var loadingBar2: View
    private lateinit var loadingBar3: View

    private val conversationHistory = mutableListOf<ReferenceChatMessage>()
    private val uiEntries = mutableListOf<UiEntry>()
    private val loadingAnimators = mutableListOf<ObjectAnimator>()

    private val fixedTopics = listOf(
        "Деканат",
        "Стипендии",
        "Общежитие",
        "Соцподдержка",
        "Профсоюз",
        "ИВТиПТ"
    )

    private var topicsLoaded = false
    private var topicsRetryJob: Job? = null
    private var requestJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rootScroll = view.findViewById(R.id.reference_root_scroll)
        topicsSection = view.findViewById(R.id.reference_topics_section)
        topicsTitle = view.findViewById(R.id.reference_topics_title)
        topicsGroup = view.findViewById(R.id.reference_topics_group)
        historyContainer = view.findViewById(R.id.reference_history_container)

        initialSection = view.findViewById(R.id.reference_initial_question_section)
        initialInput = view.findViewById(R.id.reference_initial_question_input)
        initialSubmitButton = view.findViewById(R.id.reference_initial_submit_button)

        followUpSection = view.findViewById(R.id.reference_follow_up_section)
        followUpInput = view.findViewById(R.id.reference_follow_up_input)
        followUpSendButton = view.findViewById(R.id.reference_follow_up_send_button)
        newQuestionButton = view.findViewById(R.id.reference_new_question_button)

        loadingControls = view.findViewById(R.id.reference_loading_controls)
        cancelButton = view.findViewById(R.id.reference_cancel_button)
        loadingQuestionText = view.findViewById(R.id.reference_loading_question_text)
        loadingBar1 = view.findViewById(R.id.reference_loading_bar_1)
        loadingBar2 = view.findViewById(R.id.reference_loading_bar_2)
        loadingBar3 = view.findViewById(R.id.reference_loading_bar_3)

        initialSubmitButton.setOnClickListener {
            askAssistant(
                displayQuery = initialInput.text?.toString().orEmpty(),
                actualQuery = initialInput.text?.toString().orEmpty()
            )
        }

        followUpSendButton.setOnClickListener {
            askAssistant(
                displayQuery = followUpInput.text?.toString().orEmpty(),
                actualQuery = followUpInput.text?.toString().orEmpty()
            )
        }

        newQuestionButton.setOnClickListener {
            resetConversation()
        }

        cancelButton.setOnClickListener {
            cancelCurrentRequest()
        }

        initialInput.setOnEditorActionListener { _, actionId, event ->
            val handled = actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (handled) {
                askAssistant(initialInput.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        followUpInput.setOnEditorActionListener { _, actionId, event ->
            val handled = actionId == EditorInfo.IME_ACTION_SEND ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (handled) {
                askAssistant(followUpInput.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        renderScreen()
        loadTopics(showToastOnFailure = false)
        startTopicsAutoRetry()
    }

    override fun onResume() {
        super.onResume()
        if (!topicsLoaded) {
            loadTopics(showToastOnFailure = false)
            startTopicsAutoRetry()
        }
    }

    override fun onPause() {
        super.onPause()
        topicsRetryJob?.cancel()
        stopLoadingBarsAnimation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requestJob?.cancel()
        topicsRetryJob?.cancel()
        stopLoadingBarsAnimation()
    }

    private fun startTopicsAutoRetry() {
        topicsRetryJob?.cancel()
        if (topicsLoaded) return

        topicsRetryJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive && !topicsLoaded) {
                loadTopicsInternal(showToastOnFailure = false)
                if (!topicsLoaded) delay(4000)
            }
        }
    }

    private fun loadTopics(showToastOnFailure: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            loadTopicsInternal(showToastOnFailure)
        }
    }

    private suspend fun loadTopicsInternal(showToastOnFailure: Boolean) {
        renderTopics()
        topicsLoaded = fixedTopics.isNotEmpty()
        updateTopicsVisibility()
    }

    private fun renderTopics() {
        topicsGroup.removeAllViews()

        fixedTopics.forEachIndexed { index, topic ->
            val tile = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.reference_topic_tile_background)
                setPadding(dp(15), dp(10), dp(15), dp(10))
                minimumHeight = dp(44)
            }

            val textView = TextView(requireContext()).apply {
                text = topic
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 14f
                typeface = ResourcesCompat.getFont(requireContext(), R.font.raleway)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val arrowView = ImageView(requireContext()).apply {
                setImageResource(R.drawable.ic_reference_topic_arrow)
                layoutParams = LinearLayout.LayoutParams(dp(16), dp(16))
            }

            tile.addView(textView)
            tile.addView(arrowView)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = LinearLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                rowSpec = GridLayout.spec(GridLayout.UNDEFINED)
                setMargins(
                    if (index % 2 == 0) 0 else dp(5),
                    0,
                    if (index % 2 == 0) dp(5) else 0,
                    dp(10)
                )
            }

            tile.layoutParams = params
            tile.setOnClickListener {
                askAssistant(displayQuery = topic, actualQuery = buildTopicPrompt(topic))
            }

            topicsGroup.addView(tile)
        }
    }

    private fun buildTopicPrompt(topic: String): String {
        return when (topic.trim().lowercase()) {
            "деканат" -> "Расскажи кратко и по делу про деканат: как связаться, где он находится и что важно знать студенту."
            "стипендии" -> "Расскажи кратко и по делу про стипендии: какие бывают виды и что важно знать студенту."
            "общежитие" -> "Расскажи кратко и по делу про общежитие: какие есть варианты, что важно знать студенту и куда обращаться."
            "соцподдержка" -> "Расскажи кратко и по делу про социальную поддержку студентов: кто может рассчитывать и куда обращаться."
            "профсоюз" -> "Расскажи кратко и по делу про профсоюз: чем он занимается, как связаться и режим работы."
            "ивтипт" -> "Расскажи кратко и по делу про ИВТиПТ: что это, где находится и как связаться."
            else -> topic
        }
    }

    private fun askAssistant(displayQuery: String, actualQuery: String = displayQuery) {
        val trimmedDisplay = displayQuery.trim()
        val trimmedActual = actualQuery.trim()
        if (trimmedActual.isBlank() || requestJob != null) {
            if (trimmedActual.isBlank()) {
                Toast.makeText(requireContext(), "Введите вопрос", Toast.LENGTH_SHORT).show()
            }
            return
        }

        uiEntries.add(UiEntry(question = trimmedDisplay, answer = null, status = UiStatus.LOADING))
        conversationHistory.add(ReferenceChatMessage(role = "user", text = trimmedActual))
        initialInput.setText("")
        followUpInput.setText("")

        requestJob = viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = ReferenceRepository.ask(trimmedActual, conversationHistory.takeLast(8))
                val safeAnswer = result.answer.trim().ifBlank {
                    if (result.canAnswer) {
                        "Ответ получен, но текст оказался пустым. Попробуйте уточнить вопрос."
                    } else {
                        "Я не смог найти ответ на этот вопрос. Попробуйте уточнить формулировку."
                    }
                }

                uiEntries[uiEntries.lastIndex] = uiEntries.last().copy(
                    answer = safeAnswer,
                    status = if (result.canAnswer) UiStatus.FOUND else UiStatus.NOT_FOUND
                )
                conversationHistory.add(ReferenceChatMessage(role = "assistant", text = safeAnswer))

                if (!topicsLoaded) {
                    loadTopicsInternal(showToastOnFailure = false)
                }
            } catch (_: IOException) {
                uiEntries[uiEntries.lastIndex] = uiEntries.last().copy(
                    answer = "Не удалось получить ответ. Проверьте, запущен ли backend, и попробуйте снова.",
                    status = UiStatus.NOT_FOUND
                )
                Toast.makeText(requireContext(), "Нет соединения с сервером помощника", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                uiEntries[uiEntries.lastIndex] = uiEntries.last().copy(
                    answer = "Не удалось получить ответ. Попробуйте ещё раз позже.",
                    status = UiStatus.NOT_FOUND
                )
                Toast.makeText(requireContext(), "Не удалось получить ответ", Toast.LENGTH_SHORT).show()
            } finally {
                requestJob = null
                renderScreen()
                scrollToBottom()
            }
        }

        renderScreen()
        scrollToBottom()
    }

    private fun cancelCurrentRequest() {
        requestJob?.cancel()
        requestJob = null

        if (uiEntries.lastOrNull()?.status == UiStatus.LOADING) {
            uiEntries.removeLast()
        }

        if (conversationHistory.isNotEmpty() && conversationHistory.last().role.equals("user", ignoreCase = true)) {
            conversationHistory.removeLast()
        }

        renderScreen()
    }

    private fun resetConversation() {
        requestJob?.cancel()
        requestJob = null
        uiEntries.clear()
        conversationHistory.clear()
        initialInput.setText("")
        followUpInput.setText("")
        renderScreen()
        scrollToTop()
    }

    private fun renderScreen() {
        renderHistory()
        updateTopicsVisibility()

        val hasEntries = uiEntries.isNotEmpty()
        val isLoading = uiEntries.any { it.status == UiStatus.LOADING } || requestJob != null
        val hasCompletedEntries = uiEntries.any { it.status != UiStatus.LOADING }

        initialSection.visibility = if (!hasEntries && !isLoading) VISIBLE else GONE
        followUpSection.visibility = if (hasCompletedEntries && !isLoading) VISIBLE else GONE
        loadingControls.visibility = if (hasEntries && isLoading) VISIBLE else GONE

        if (hasEntries && isLoading) {
            loadingQuestionText.text = uiEntries.lastOrNull()?.question.orEmpty()
            startLoadingBarsAnimation()
        } else {
            stopLoadingBarsAnimation()
        }
    }

    private fun updateTopicsVisibility() {
        val hasCompletedEntries = uiEntries.any { it.status != UiStatus.LOADING }
        val shouldShow = topicsLoaded && !hasCompletedEntries
        topicsSection.visibility = if (shouldShow) VISIBLE else GONE
        topicsTitle.visibility = if (shouldShow) VISIBLE else GONE
        topicsGroup.visibility = if (shouldShow) VISIBLE else GONE
    }

    private fun renderHistory() {
        historyContainer.removeAllViews()

        val visibleEntries = uiEntries.filter { it.status != UiStatus.LOADING }

        if (visibleEntries.isEmpty()) {
            historyContainer.visibility = GONE
            return
        }

        historyContainer.visibility = VISIBLE

        visibleEntries.forEachIndexed { index, entry ->
            historyContainer.addView(createEntryCard(entry, index == 0))
        }
    }

    private fun createEntryCard(entry: UiEntry, isFirst: Boolean): View {
        val outer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(
                requireContext(),
                when (entry.status) {
                    UiStatus.FOUND -> R.drawable.reference_result_found_background
                    UiStatus.NOT_FOUND -> R.drawable.reference_result_not_found_background
                    UiStatus.LOADING -> R.drawable.reference_result_loading_background
                }
            )
            setPadding(dp(14), dp(14), dp(14), dp(14))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = if (isFirst) 0 else dp(10)
            }
        }

        val statusText = TextView(requireContext()).apply {
            text = when (entry.status) {
                UiStatus.FOUND -> "Ответ найден"
                UiStatus.NOT_FOUND -> "Ничего не найдено"
                UiStatus.LOADING -> "Ищем ответ на вопрос"
            }
            setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    when (entry.status) {
                        UiStatus.FOUND -> R.color.reference_found_title
                        UiStatus.NOT_FOUND -> R.color.reference_not_found_title
                        UiStatus.LOADING -> R.color.reference_loading_title
                    }
                )
            )
            textSize = 13f
            typeface = ResourcesCompat.getFont(requireContext(), R.font.raleway)
        }
        outer.addView(statusText)

        val questionView = TextView(requireContext()).apply {
            text = entry.question
            setTextColor(ContextCompat.getColor(requireContext(), R.color.reference_question_label))
            textSize = 16f
            typeface = ResourcesCompat.getFont(requireContext(), R.font.raleway_medium)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(5)
            }
        }
        outer.addView(questionView)

        if (entry.status != UiStatus.LOADING) {
            val answerCard = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = ContextCompat.getDrawable(requireContext(), R.drawable.reference_answer_inner_background)
                setPadding(dp(12), dp(12), dp(12), dp(12))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(10)
                }
            }

            val answerView = TextView(requireContext()).apply {
                text = entry.answer.orEmpty()
                setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                textSize = 16f
                lineHeight = dp(22)
                typeface = ResourcesCompat.getFont(requireContext(), R.font.raleway_medium)
            }
            answerCard.addView(answerView)
            outer.addView(answerCard)
        }

        return outer
    }

    private fun scrollToBottom() {
        rootScroll.post { rootScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun scrollToTop() {
        rootScroll.post { rootScroll.fullScroll(View.FOCUS_UP) }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private data class UiEntry(
        val question: String,
        val answer: String?,
        val status: UiStatus
    )

    private enum class UiStatus {
        LOADING,
        FOUND,
        NOT_FOUND
    }

    private fun startLoadingBarsAnimation() {
        stopLoadingBarsAnimation()

        val bars = listOf(loadingBar1, loadingBar2, loadingBar3)
        bars.forEachIndexed { index, bar ->
            val animator = ObjectAnimator.ofFloat(bar, View.ALPHA, 0.35f, 1f, 0.35f).apply {
                duration = 1200
                startDelay = (index * 180).toLong()
                repeatCount = ValueAnimator.INFINITE
                repeatMode = ValueAnimator.RESTART
            }
            loadingAnimators.add(animator)
            animator.start()
        }
    }

    private fun stopLoadingBarsAnimation() {
        loadingAnimators.forEach { it.cancel() }
        loadingAnimators.clear()

        listOf(loadingBar1, loadingBar2, loadingBar3).forEach {
            it.alpha = 1f
        }
    }
}
