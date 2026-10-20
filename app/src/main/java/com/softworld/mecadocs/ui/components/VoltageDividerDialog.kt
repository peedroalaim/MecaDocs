package com.softworld.mecadocs.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Dialog da Calculadora de Divisor de Tensão (Ferramenta Profissional de Bancada).
 * Calcula o resistor $R_2$ ideal e sugere valores comerciais E24 padrão para
 * adaptar sinais de 5V para 3.3V (ex: conectar sensores 5V ao ESP32/STM32).
 */
@Composable
fun VoltageDividerDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var vinInput by remember { mutableStateOf("5.0") }
    var voutInput by remember { mutableStateOf("3.3") }
    var r1Input by remember { mutableStateOf("10000") } // 10 kΩ

    val vin = vinInput.toDoubleOrNull() ?: 5.0
    val vout = voutInput.toDoubleOrNull() ?: 3.3
    val r1 = r1Input.toDoubleOrNull() ?: 10000.0

    val r2Calculated = if ((vin > vout) && (vout > 0)) {
        (vout * r1) / (vin - vout)
    } else null

    val e24Resistors = listOf(
        1000, 1200, 1500, 1800, 2200, 2700, 3300, 3900, 4700, 5600, 6800, 8200,
        10000, 12000, 15000, 18000, 20000, 22000, 27000, 33000, 47000,
    )

    val closestE24 = r2Calculated?.let { target ->
        e24Resistors.minByOrNull { kotlin.math.abs(it - target) }
    }

    val actualVoutWithE24 = if (r2Calculated != null && closestE24 != null) {
        vin * (closestE24.toDouble() / (r1 + closestE24.toDouble()))
    } else null

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                modifier = Modifier.size(48.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Calculate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BENCH CALCULATOR",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp,
                )
                Text(
                    text = "Voltage Divider",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Esquema Textual Abstrato
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "VIN ──[ R1 ]──+── VOUT\n              |\n            [ R2 ]\n              |\nGND ──────────+",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(10.dp),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = vinInput,
                        onValueChange = { vinInput = it },
                        label = { Text("Vin (V)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = voutInput,
                        onValueChange = { voutInput = it },
                        label = { Text("Vout (V)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }

                OutlinedTextField(
                    value = r1Input,
                    onValueChange = { r1Input = it },
                    label = { Text("Resistor R1 (Ω)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Ex: 10000 para 10 kΩ") },
                )

                // Resultado Profissional de Bancada
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ElectricBolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "RECOMMENDED E24",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                            )
                        }

                        if (r2Calculated != null && closestE24 != null) {
                            Text(
                                text = "R2 Ideal: ${r2Calculated.roundToInt()} Ω (${String.format(Locale.US, "%.2f", r2Calculated / 1000.0)} kΩ)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Text(
                                    text = "R2 Comercial: $closestE24 Ω (${closestE24 / 1000.0} kΩ) ➔ Vout Real = ${String.format(Locale.US, "%.2f", actualVoutWithE24)}V",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                )
                            }
                        } else {
                            Text(
                                text = "Certifique-se de que Vin > Vout para o cálculo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismissRequest,
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Fechar Ferramenta")
            }
        },
        modifier = modifier,
    )
}
