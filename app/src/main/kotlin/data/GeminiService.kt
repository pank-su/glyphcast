package su.pank.exhelp.app.data


import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterLLMClient
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.AttachmentContent
import ai.koog.prompt.message.ContentPart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService(private val apiKey: String) {


    private val standardCapabilities: List<LLMCapability> = listOf(
        LLMCapability.Temperature,
        LLMCapability.Completion,
        LLMCapability.MultipleChoices,
    )

    /**
     * Capabilities for models that support tools/function calling
     */
    private val toolCapabilities: List<LLMCapability> = listOf(
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
    )

    /**
     * Multimodal capabilities including vision (without tools)
     */
    private val multimodalCapabilities: List<LLMCapability> =
        listOf(LLMCapability.Vision.Image, LLMCapability.Vision.Video, LLMCapability.Audio)

    /**
     * Native structured output capabilities
     */
    private val structuredOutputCapabilities: List<LLMCapability.Schema.JSON> = listOf(
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Schema.JSON.Standard,
    )

    /**
     * Full capabilities including standard, multimodal, tools and native structured output
     */
    private val fullCapabilities: List<LLMCapability> =
        standardCapabilities + multimodalCapabilities + toolCapabilities + structuredOutputCapabilities


    val Gemini3Flash = LLModel(
        provider = LLMProvider.Google,
        id = "google/gemini-3-flash-preview",
        capabilities = fullCapabilities,
        contextLength = 1_048_576,
        maxOutputTokens = 65_536,
    )

    private val client by lazy {
        OpenRouterLLMClient(apiKey)
    }
    
    /**
     * Отправляет скриншот в Gemini и получает описание
     */
    suspend fun analyzeScreenshot(screenshotBytes: ByteArray): String = withContext(Dispatchers.IO) {
        try {
            val prompt = prompt("send_screenshot"){
                system{
                    +"Ты помощник для решения тестов, давай короткие варианты ответов без лишних объяснений. В ответе должен быть обязательно номер вопроса."
                }
                user {
                    image(ContentPart.Image(
                        content = AttachmentContent.Binary.Bytes(screenshotBytes),
                        format="png",
                        mimeType = "image/png",
                        fileName = "capture.png"
                    ))
                }
            }
            
            client.execute(prompt, Gemini3Flash).joinToString{it.content}
        } catch (e: Exception) {
            e.printStackTrace()
            "Ошибка анализа: ${e.message}"
        }
    }
}
