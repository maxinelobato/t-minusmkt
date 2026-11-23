package com.example.util

import android.content.Context
import android.util.Log
import com.example.ui.components.AiChatMessage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val TAG = "MarketingAiService"

data class SuggestedTask(
    val title: String,
    val daysRemaining: Int,
    val iconType: String
)

data class AssistantResult(
    val spokenText: String,
    val suggestedTask: SuggestedTask? = null
)

class MarketingAiService(context: Context? = null) {

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = content {
                text(
                    """
                    Você é o estrategista oficial de marketing e lançamentos do T-Minus Mkt.
                    Seu objetivo é guiar o usuário na criação e execução de campanhas de alta conversão, tráfego pago, posicionamento e cronogramas de contagem regressiva (T-Minus).
                    
                    Diretrizes:
                    1. Seja estratégico, motivador, conciso e prático (respostas ideais para serem lidas em áudio em menos de 30 segundos).
                    2. Responda no idioma solicitado pelo usuário (Português ou Inglês).
                    3. Quando o usuário pedir para criar, agendar ou sugerir uma etapa prática para o cronograma de lançamento, forneça a justificativa estratégica e adicione OBRIGATORIAMENTE no final da sua resposta uma tag especial de tarefa exatamente no formato:
                       [TASK: título da etapa | dias_restantes_número | ícone]
                       Onde ícone deve ser um dos seguintes: search, article, megaphone, checkcircle ou rocket.
                       Exemplo:
                       "Excelente ideia! Para aquecer seu público antes do carrinho abrir, recomendo preparar uma série de depoimentos.
                       [TASK: Disparar sequência de provas sociais | 4 | megaphone]"
                    4. Se for apenas uma dúvida geral de marketing, responda objetivamente sem a tag [TASK:...].
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun askAssistant(
        userPrompt: String,
        chatHistory: List<AiChatMessage> = emptyList(),
        isPortuguese: Boolean = true
    ): AssistantResult = withContext(Dispatchers.IO) {
        try {
            val contextualPrompt = buildString {
                append("Idioma preferido: ${if (isPortuguese) "Português (Brasil)" else "English"}\n")
                if (chatHistory.isNotEmpty()) {
                    append("Histórico recente da conversa:\n")
                    chatHistory.takeLast(4).forEach { msg ->
                        append("${if (msg.sender == "user") "Usuário" else "Estrategista"}: ${msg.text}\n")
                    }
                    append("\n")
                }
                append("Mensagem do usuário: $userPrompt")
            }

            val response = withTimeout(60_000L) {
                model.generateContent(contextualPrompt)
            }

            val fullText = response.text.orEmpty()
            if (fullText.isBlank()) {
                val fallback = if (isPortuguese) {
                    "Estou analisando sua estratégia de lançamento. Pode repetir ou detalhar melhor?"
                } else {
                    "I'm analyzing your launch strategy. Could you please elaborate?"
                }
                return@withContext AssistantResult(spokenText = fallback)
            }

            // Extract suggested task if present: [TASK: Title | Days | Icon]
            val taskRegex = Regex("\\[TASK:\\s*([^|]+)\\s*\\|\\s*(\\d+)\\s*\\|\\s*([^\\]]+)\\]")
            val match = taskRegex.find(fullText)
            val suggestedTask = match?.let {
                val title = it.groupValues[1].trim()
                val days = it.groupValues[2].toIntOrNull() ?: 7
                val icon = it.groupValues[3].trim().lowercase()
                SuggestedTask(title = title, daysRemaining = days, iconType = icon)
            }

            val cleanText = fullText.replace(taskRegex, "").trim()
            AssistantResult(spokenText = cleanText, suggestedTask = suggestedTask)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Erro na resposta Gemini AI", e)
            val errorMsg = if (isPortuguese) {
                "Não consegui conectar ao serviço de IA no momento. Por favor, tente novamente."
            } else {
                "Could not connect to AI service right now. Please try again."
            }
            AssistantResult(spokenText = errorMsg)
        }
    }
}
