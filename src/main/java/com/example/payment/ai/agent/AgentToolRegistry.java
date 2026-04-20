package com.example.payment.ai.agent;

import com.example.payment.ai.exception.ToolExecutionException;
import com.example.payment.ai.tool.AgentTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AgentToolRegistry {

    private final Map<String, AgentTool> tools;

    public AgentToolRegistry(List<AgentTool> toolList) {
        this.tools = toolList.stream()
                .collect(Collectors.toMap(AgentTool::name, t -> t));
        log.info("AgentToolRegistry initialised with tools: {}", tools.keySet());
    }

    public AgentTool get(String name) {
        AgentTool tool = tools.get(name);
        if (tool == null) {
            throw new ToolExecutionException("Unknown tool: " + name);
        }
        return tool;
    }

    public String describeTools() {
        return tools.values().stream()
                .map(t -> "- " + t.name() + ": " + t.description())
                .collect(Collectors.joining("\n"));
    }
}
