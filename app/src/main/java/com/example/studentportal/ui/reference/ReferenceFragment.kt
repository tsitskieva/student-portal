package com.example.studentportal.ui.reference

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studentportal.R
import com.example.studentportal.data.model.ReferenceAssistantResponse
import com.example.studentportal.data.model.ReferenceChatMessage
import com.example.studentportal.data.repository.ReferenceRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.IOException

class ReferenceFragment : Fragment() {
    private lateinit var queryInput: TextInputEditText
    private lateinit var sendButton: Button
    private lateinit var topicsTitle: TextView
    private lateinit var topicsGroup: ChipGroup
    private lateinit var chatScroll: NestedScrollView
    private lateinit var chatContainer: LinearLayout

    private val conversationHistory = mutableListOf<ReferenceChatMessage>()
    private var topicsLoaded = false
    private var topicsRetryJob: Job? = null
    private var loadingMessageView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reference, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        queryInput = view.findViewById(R.id.reference_query_input)
        sendButton = view.findViewById(R.id.reference_send_button)
        topicsTitle = view.findViewById(R.id.reference_topics_title)
        topicsGroup = view.findViewById(R.id.reference_topics_group)
        chatScroll = view.findViewById(R.id.reference_chat_scroll)
        chatContainer = view.findViewById(R.id.reference_chat_container)

        setTopicsVisible(false)

        sendButton.setOnClickListener {
            askAssistant(queryInput.text?.toString().orEmpty())
        }

        queryInput.setOnEditorActionListener { _, actionId, event ->
            val handled = actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)

            if (handled) {
                askAssistant(queryInput.text?.toString().orEmpty())
                true
            } else {
                false
            }
        }

        appendSystemMessage(
            "Привет! Я AI-помощник по университетской информации. Могу помочь с вопросами про деканат, ИВТиПТ, стипендии, общежитие, приёмную комиссию, профсоюз и социальную поддержку."
        )

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
    }

    private fun startTopicsAutoRetry() {
        topicsRetryJob?.cancel()

        if (topicsLoaded) return

        topicsRetryJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive && !topicsLoaded) {
                loadTopicsInternal(showToastOnFailure = false)
                if (!topicsLoaded) {
                    delay(4000)
                }
            }
        }
    }

    private fun loadTopics(showToastOnFailure: Boolean) {
        viewLifecycleOwner.lifecycleScope.launch {
            loadTopicsInternal(showToastOnFailure)
        }
    }

    private suspend fun loadTopicsInternal(showToastOnFailure: Boolean) {
        try {
            val topics = ReferenceRepository.getTopics()
            renderTopics(topics)
            topicsLoaded = topics.isNotEmpty()
            setTopicsVisible(topics.isNotEmpty())
        } catch (_: Exception) {
            topicsLoaded = false
            setTopicsVisible(false)

            if (showToastOnFailure) {
                Toast.makeText(
                    requireContext(),
                    "Не удалось загрузить темы помощника",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun renderTopics(topics: List<String>) {
        topicsGroup.removeAllViews()

        topics.forEach { topic ->
            val chip = Chip(requireContext()).apply {
                text = topic
                isCheckable = false
                isClickable = true
                setOnClickListener {
                    askAssistant(displayQuery = topic, actualQuery = buildTopicPrompt(topic))
                }
            }
            topicsGroup.addView(chip)
        }
    }

    private fun buildTopicPrompt(topic: String): String {
        return when (topic.trim().lowercase()) {
            "деканат" -> "Расскажи кратко и по делу про деканат: как связаться, где он находится и что важно знать студенту."
            "ивтипт" -> "Расскажи кратко и по делу про ИВТиПТ: что это, где находится и как связаться."
            "общежитие" -> "Расскажи кратко и по делу про общежития: что важно знать студенту, какие есть адреса и куда обращаться."
            "приёмная комиссия", "приемная комиссия" -> "Расскажи кратко и по делу про приёмную комиссию: где она находится, как связаться и что важно знать."
            "профсоюз" -> "Расскажи кратко и по делу про профсоюз: чем он занимается, как связаться и режим работы."
            "социальная поддержка" -> "Расскажи кратко и по делу про социальную поддержку студентов: кто может рассчитывать и куда обращаться."
            "стипендия", "стипендии" -> "Расскажи кратко и по делу про стипендии: какие бывают виды и что важно знать студенту."
            "университет" -> "Расскажи кратко и по делу про университет в рамках доступной справочной информации."
            else -> topic
        }
    }

    private fun askAssistant(displayQuery: String, actualQuery: String = displayQuery) {
        val trimmedDisplay = displayQuery.trim()
        val trimmedActual = actualQuery.trim()

        if (trimmedActual.isBlank()) {
            Toast.makeText(requireContext(), "Введите вопрос", Toast.LENGTH_SHORT).show()
            return
        }

        appendUserMessage(trimmedDisplay)
        conversationHistory.add(ReferenceChatMessage(role = "user", text = trimmedDisplay))
        queryInput.setText("")

        viewLifecycleOwner.lifecycleScope.launch {
            setLoading(true)
            try {
                val result = ReferenceRepository.ask(trimmedActual, conversationHistory.takeLast(8))
                hideLoadingMessage()
                appendAssistantMessage(result)
                conversationHistory.add(
                    ReferenceChatMessage(
                        role = "assistant",
                        text = result.answer
                    )
                )

                if (!topicsLoaded) {
                    loadTopicsInternal(showToastOnFailure = false)
                }
            } catch (e: IOException) {
                hideLoadingMessage()
                appendSystemMessage(
                    "Помощник сейчас недоступен. Проверьте, запущен ли backend, и попробуйте снова."
                )
                Toast.makeText(
                    requireContext(),
                    "Нет соединения с сервером помощника",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                hideLoadingMessage()
                appendSystemMessage(
                    "Не удалось получить ответ. Попробуйте ещё раз позже."
                )
                Toast.makeText(
                    requireContext(),
                    "Не удалось получить ответ",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun appendUserMessage(message: String) {
        val wrapper = createMessageWrapper(Gravity.END)
        val bubble = createBubbleContainer(R.drawable.reference_message_user_background)

        val textView = TextView(requireContext()).apply {
            text = message
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            textSize = 15f
        }

        bubble.addView(textView)
        wrapper.addView(bubble)
        chatContainer.addView(wrapper)
        scrollToBottom()
    }

    private fun appendAssistantMessage(result: ReferenceAssistantResponse) {
        val wrapper = createMessageWrapper(Gravity.START)
        val bubble = createBubbleContainer(R.drawable.reference_message_bot_background)

        val headerView = TextView(requireContext()).apply {
            text = "AI-помощник"
            setTextColor(0xFFC4B5FD.toInt())
            textSize = 12f
            setBackgroundResource(R.drawable.reference_category_background)
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }

        val bodyView = TextView(requireContext()).apply {
            text = result.answer
            setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            textSize = 15f
            setLineSpacing(0f, 1.15f)
            setPadding(0, dp(12), 0, 0)
        }

        bubble.addView(headerView)
        bubble.addView(bodyView)

        if (result.sources.isNotEmpty()) {
            val sourcesTitleView = TextView(requireContext()).apply {
                text = "Источники"
                setTextColor(0xFF9AA0AE.toInt())
                textSize = 12f
                setPadding(0, dp(12), 0, 0)
                setTypeface(typeface, Typeface.BOLD)
            }

            val sourcesBodyView = TextView(requireContext()).apply {
                text = result.sources.joinToString("\n• ", prefix = "• ")
                setTextColor(0xFFB8BBC6.toInt())
                textSize = 12f
                setLineSpacing(0f, 1.1f)
                setPadding(0, dp(6), 0, 0)
            }

            bubble.addView(sourcesTitleView)
            bubble.addView(sourcesBodyView)
        }

        wrapper.addView(bubble)
        chatContainer.addView(wrapper)
        scrollToBottom()
    }

    private fun appendSystemMessage(message: String) {
        val wrapper = createMessageWrapper(Gravity.START)
        val bubble = createBubbleContainer(R.drawable.reference_message_system_background)

        val textView = TextView(requireContext()).apply {
            text = message
            setTextColor(0xFFB8BBC6.toInt())
            textSize = 14f
        }

        bubble.addView(textView)
        wrapper.addView(bubble)
        chatContainer.addView(wrapper)
        scrollToBottom()
    }

    private fun showLoadingMessage() {
        if (loadingMessageView != null) return

        val wrapper = createMessageWrapper(Gravity.START)
        val bubble = createBubbleContainer(R.drawable.reference_message_system_background)

        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val dotsView = TextView(requireContext()).apply {
            text = "AI думает..."
            setTextColor(0xFFB8BBC6.toInt())
            textSize = 14f
        }

        row.addView(dotsView)
        bubble.addView(row)
        wrapper.addView(bubble)

        loadingMessageView = wrapper
        chatContainer.addView(wrapper)
        scrollToBottom()
    }

    private fun hideLoadingMessage() {
        loadingMessageView?.let { view ->
            chatContainer.removeView(view)
        }
        loadingMessageView = null
    }

    private fun createMessageWrapper(gravity: Int): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(12)
            }
            this.gravity = gravity
        }
    }

    private fun createBubbleContainer(backgroundRes: Int): LinearLayout {
        return LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(backgroundRes)
            setPadding(dp(14), dp(12), dp(14), dp(12))
            layoutParams = LinearLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.78f).toInt(),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            showLoadingMessage()
        } else {
            hideLoadingMessage()
        }

        sendButton.isEnabled = !isLoading
        queryInput.isEnabled = !isLoading
        topicsGroup.children.forEach { it.isEnabled = !isLoading }
    }

    private fun setTopicsVisible(isVisible: Boolean) {
        topicsTitle.visibility = if (isVisible) VISIBLE else GONE
        topicsGroup.visibility = if (isVisible) VISIBLE else GONE
    }

    private fun scrollToBottom() {
        chatScroll.post {
            chatScroll.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}