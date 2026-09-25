package com.mojealterego.newgpt.presentation.runtime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mojealterego.newgpt.runtime.AlignmentPolicy
import com.mojealterego.newgpt.runtime.Budget
import com.mojealterego.newgpt.runtime.ComputePlan
import com.mojealterego.newgpt.runtime.EnergyAwareScheduler
import com.mojealterego.newgpt.runtime.EnergyBudget
import com.mojealterego.newgpt.runtime.EpistemicHumilityLoop
import com.mojealterego.newgpt.runtime.HumilityGate
import com.mojealterego.newgpt.runtime.PhysicalSubstratePlanner
import com.mojealterego.newgpt.runtime.SubstrateCandidate

@Composable
fun AbsoluteMaximumScreen(onBack: () -> Unit) {
    val compute: ComputePlan = EnergyAwareScheduler().plan(EnergyBudget(50.0, 0.8, 0.72), 8000, 4)
    val substrate = PhysicalSubstratePlanner().rank(
        listOf(
            SubstrateCandidate("silicon", "CPU/GPU baseline", 1.0, 1.0, 0.98),
            SubstrateCandidate("graphene", "experimental channel", 1.7, 0.55, 0.35),
            SubstrateCandidate("CNT", "experimental transistor", 1.5, 0.62, 0.45)
        )
    )
    val humility = EpistemicHumilityLoop().check(0.93, listOf("external evidence may be incomplete"), listOf("hardware model is estimated"), true)
    val gate = HumilityGate().evaluate(0.93, true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ABSOLUTE MAXIMUM · META RUNTIME") },
                navigationIcon = { IconButton(onClick = onBack) { androidx.compose.material3.Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Runtime governance", style = MaterialTheme.typography.titleLarge)
                        Text("Latency budget: ${Budget(2000).maxMs} ms")
                        Text("Energy plan: ${compute.mode} · parallelism ${compute.maxParallelism} · tokens ${compute.maxTokens}")
                        Text("High-impact human review: ${gate.humanRequired}")
                        Text("Epistemic rollback available: ${humility.rollbackAvailable}")
                    }
                }
            }
            item { Text("95 · Physical substrate design space", style = MaterialTheme.typography.titleLarge) }
            items(substrate) { candidate ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(candidate.material)
                        Text("gain ${candidate.estimatedGain}× · manufacturability ${candidate.manufacturability}")
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("96–99 · Quantum / Energy / Alignment / Humility", style = MaterialTheme.typography.titleLarge)
                        Text("Quantum: adapter boundary with deterministic classical fallback.")
                        Text("Energy: device-aware compute budgeting; no direct thermodynamic sensor for cognition.")
                        Text("Alignment: explicit immutable invariants + policy hash; not a proof of CEV completeness.")
                        Text("Humility: dissent + assumptions + rollback + human gate for high-impact uncertainty.")
                    }
                }
            }
        }
    }
}
