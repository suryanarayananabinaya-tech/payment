package com.example.payment.ai.workflow;

import com.example.payment.ai.agent.AgentFinalResponse;
import com.example.payment.ai.agent.AgentOrchestrator;
import com.example.payment.ai.classifier.QueryClassifier;
import com.example.payment.ai.dto.AiQueryRequest;
import com.example.payment.ai.dto.PromptInputDto;
import com.example.payment.ai.llm.LlmClient;
import com.example.payment.ai.mapper.LlmResponseMapper;
import com.example.payment.ai.model.LlmResponse;
import com.example.payment.ai.prompt.PromptBuilder;
import com.example.payment.ai.rag.RagRetriever;
import com.example.payment.ai.retriever.PaymentContextRetriever;
import com.example.payment.ai.validator.AiRequestValidator;
import com.example.payment.ai.validator.AiResponseValidator;
import com.example.payment.ai.validator.PromptSafetyValidator;
import com.example.payment.ai.validator.ResponseSafetyValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AiWorkflowEngine {

    private final QueryClassifier queryClassifier;
    private final AiRequestValidator requestValidator;
    private final AiResponseValidator responseValidator;
    private final PromptSafetyValidator promptSafetyValidator;
    private final ResponseSafetyValidator responseSafetyValidator;
    private final PaymentContextRetriever paymentContextRetriever;
    private final RagRetriever ragRetriever;
    private final PromptBuilder promptBuilder;
    private final LlmClient llmClient;
    private final LlmResponseMapper llmResponseMapper;
    private final AgentOrchestrator agentOrchestrator;

    public AiWorkflowEngine(
            QueryClassifier queryClassifier,
            AiRequestValidator requestValidator,
            AiResponseValidator responseValidator,
            PromptSafetyValidator promptSafetyValidator,
            ResponseSafetyValidator responseSafetyValidator,
            PaymentContextRetriever paymentContextRetriever,
            RagRetriever ragRetriever,
            PromptBuilder promptBuilder,
            LlmClient llmClient,
            LlmResponseMapper llmResponseMapper,
            AgentOrchestrator agentOrchestrator) {
        this.queryClassifier = queryClassifier;
        this.requestValidator = requestValidator;
        this.responseValidator = responseValidator;
        this.promptSafetyValidator = promptSafetyValidator;
        this.responseSafetyValidator = responseSafetyValidator;
        this.paymentContextRetriever = paymentContextRetriever;
        this.ragRetriever = ragRetriever;
        this.promptBuilder = promptBuilder;
        this.llmClient = llmClient;
        this.llmResponseMapper = llmResponseMapper;
        this.agentOrchestrator = agentOrchestrator;
    }

    // =========================
    // NORMAL WORKFLOW
    // =========================
    public AiWorkflowResult executeWorkflow(AiQueryRequest request, String sessionId, String traceId) {

        AiWorkflowContext context = initContext(request, sessionId, traceId);

        try {
            stepValidateInput(context);
            stepClassifyQuery(context);
            stepRetrieveContext(context);
            stepBuildPrompt(context);
            stepRunLlm(context);
            stepValidateResponse(context);

            return success(context);

        } catch (Exception e) {
            return failure(context, traceId, e);
        }
    }

    // =========================
    // AGENT WORKFLOW
    // =========================
    public AiWorkflowResult executeAgentWorkflow(AiQueryRequest request, String sessionId, String traceId) {

        AiWorkflowContext context = initContext(request, sessionId, traceId);

        try {
            stepValidateInput(context);
            stepClassifyQuery(context);
            stepRetrieveContext(context);
            context.setSystemPrompt(
                    promptBuilder.buildSystemPrompt(context.getQueryType())
            );
            stepBuildPrompt(context);
            stepRunAgent(context);
            stepValidateResponse(context);
            return success(context);

        } catch (Exception e) {
            return failure(context, traceId, e);
        }
    }

    // =========================
    // COMMON HELPERS
    // =========================
    private AiWorkflowContext initContext(AiQueryRequest request, String sessionId, String traceId) {
        return AiWorkflowContext.builder()
                .request(request)
                .sessionId(sessionId)
                .traceId(traceId)
                .currentStep(AiWorkflowStep.VALIDATE_INPUT)
                .failed(false)
                .build();
    }

    private AiWorkflowResult success(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.COMPLETE);

        log.info("Workflow complete [traceId={}, queryType={}]",
                context.getTraceId(), context.getQueryType());

        return AiWorkflowResult.builder()
                .answer(context.getRawAnswer())
                .sessionId(context.getSessionId())
                .traceId(context.getTraceId())
                .queryType(context.getQueryType())
                .sources(List.of()) // future: populate RAG sources
                .success(true)
                .build();
    }

    private AiWorkflowResult failure(AiWorkflowContext context, String traceId, Exception e) {
        log.error("Workflow failed at step {} [traceId={}]: {}",
                context.getCurrentStep(), traceId, e.getMessage(), e);

        return AiWorkflowResult.builder()
                .answer("We could not process your request. Please try again.")
                .sessionId(context.getSessionId())
                .traceId(traceId)
                .queryType(context.getQueryType())
                .sources(List.of())
                .success(false)
                .failureReason(e.getMessage())
                .build();
    }

    // =========================
    // STEPS
    // =========================

    private void stepValidateInput(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.VALIDATE_INPUT);
        requestValidator.validate(context.getRequest());
        promptSafetyValidator.validate(context.getRequest().getQuery());
    }

    private void stepClassifyQuery(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.CLASSIFY_QUERY);
        context.setQueryType(queryClassifier.classify(context.getRequest().getQuery()));
    }

    private void stepRetrieveContext(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.RETRIEVE_CONTEXT);
        context.setPaymentContext(paymentContextRetriever.retrieve(context.getRequest().getUserId()));
        context.setKnowledgeContext(ragRetriever.retrieve(context.getRequest().getQuery()));
    }

    private void stepBuildPrompt(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.BUILD_PROMPT);
        context.setAssembledPrompt(
                promptBuilder.buildUserPrompt(toPromptInput(context))
        );
    }

    private void stepRunLlm(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.RUN_LLM);

        LlmResponse llmResponse = llmClient.complete(
                promptBuilder.buildSystemPrompt(context.getQueryType()),
                context.getAssembledPrompt()
        );

        context.setRawAnswer(
                llmResponseMapper.toInternal(llmResponse).getAnswer()
        );
    }

    private void stepRunAgent(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.RUN_AGENT);

        AgentFinalResponse agentResponse = agentOrchestrator.run(context);

        context.setRawAnswer(agentResponse.getAnswer());
    }

    private void stepValidateResponse(AiWorkflowContext context) {
        context.setCurrentStep(AiWorkflowStep.VALIDATE_RESPONSE);
        responseValidator.validate(context.getRawAnswer());
        responseSafetyValidator.validate(context.getRawAnswer());
    }

    private PromptInputDto toPromptInput(AiWorkflowContext context) {
        return PromptInputDto.builder()
                .userQuery(context.getRequest().getQuery())
                .userId(context.getRequest().getUserId())
                .sessionId(context.getSessionId())
                .queryType(context.getQueryType())
                .paymentContext(context.getPaymentContext())
                .knowledgeContext(context.getKnowledgeContext())
                .build();
    }
}