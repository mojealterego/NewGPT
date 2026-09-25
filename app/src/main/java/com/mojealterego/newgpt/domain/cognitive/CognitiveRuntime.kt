package com.mojealterego.newgpt.domain.cognitive

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CognitiveRuntime @Inject constructor() {
    fun decisionCycle(goal: String): DecisionCycle {
        val goalNode = DecisionNode(
            id = UUID.randomUUID().toString(),
            label = goal.trim().take(500),
            kind = DecisionKind.GOAL,
            confidence = 1f
        )
        return DecisionCycle(goalNode)
    }

    fun add(
        cycle: DecisionCycle,
        label: String,
        kind: DecisionKind,
        confidence: Float = 0.5f,
        cost: Float = 0f,
        risk: Float = 0f,
        parents: List<String> = cycle.nodes.takeLast(1).map { it.id }
    ): DecisionCycle {
        val node = DecisionNode(
            id = UUID.randomUUID().toString(),
            label = label.take(1000),
            kind = kind,
            parentIds = parents,
            confidence = confidence.coerceIn(0f, 1f),
            cost = cost.coerceAtLeast(0f),
            risk = risk.coerceIn(0f, 1f)
        )
        return cycle.copy(nodes = cycle.nodes + node)
    }

    fun choose(candidates: List<DecisionNode>): DecisionNode? =
        candidates.maxByOrNull {
            (it.confidence * 0.55f) - (it.risk * 0.30f) - (it.cost * 0.15f)
        }

    fun reflect(
        cycle: DecisionCycle,
        observation: String,
        correction: String
    ): DecisionCycle {
        val observationNode = add(cycle, observation, DecisionKind.OBSERVATION, 0.7f)
        return add(
            observationNode,
            correction,
            DecisionKind.REFLECTION,
            confidence = 0.8f,
            parents = observationNode.nodes.takeLast(1).map { it.id }
        )
    }
}

data class DecisionCycle(
    val root: DecisionNode,
    val nodes: List<DecisionNode> = listOf(root)
)
