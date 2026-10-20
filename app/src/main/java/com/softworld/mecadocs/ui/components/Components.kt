package com.softworld.mecadocs.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Input
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.SettingsInputComponent
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softworld.mecadocs.data.model.AlertaItem
import com.softworld.mecadocs.data.model.CodigoExemplo
import com.softworld.mecadocs.data.model.PinoutItem
import com.softworld.mecadocs.data.model.SpecsData
import com.softworld.mecadocs.ui.theme.AmberWarning
import com.softworld.mecadocs.ui.theme.AmberWarningBorder
import com.softworld.mecadocs.ui.theme.AmberWarningContainer
import com.softworld.mecadocs.ui.theme.AmberWarningOnContainer
import com.softworld.mecadocs.ui.theme.CodeBackgroundDark
import com.softworld.mecadocs.ui.theme.CodeTextLight
import com.softworld.mecadocs.ui.theme.InfoBlue
import com.softworld.mecadocs.ui.theme.InfoBlueContainer
import com.softworld.mecadocs.ui.theme.InfoBlueOnContainer
import com.softworld.mecadocs.ui.theme.RedDanger
import com.softworld.mecadocs.ui.theme.RedDangerContainer
import com.softworld.mecadocs.ui.theme.RedDangerOnContainer

/**
 * Card 1: Especificações Rápidas (Quick Specs) - Estilo Matriz Técnica.
 */
@Composable
fun QuickSpecsCard(
    specs: SpecsData,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "TECHNICAL MATRIX / SPECS",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SpecItemRow(label = "Operating Voltage", value = specs.tensaoOperacao, icon = Icons.Rounded.Power)
                SpecItemRow(label = "Input Voltage", value = specs.tensaoEntrada, icon = Icons.AutoMirrored.Rounded.Input)
                SpecItemRow(label = "Max Current", value = specs.correnteMaxima, icon = Icons.Rounded.FlashOn)
                SpecItemRow(label = "Logic Level", value = specs.nivelLogico, icon = Icons.Rounded.Memory)

                if (specs.clock.isNotBlank() && specs.clock != "N/A") {
                    SpecItemRow(label = "Clock / CPU", value = specs.clock, icon = Icons.Rounded.GraphicEq)
                }
                if (specs.memoria.isNotBlank() && specs.memoria != "N/A") {
                    SpecItemRow(label = "Memory / Flash", value = specs.memoria, icon = Icons.Rounded.SdStorage)
                }

                if (specs.interfaces.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = "INTERFACES & BUSES",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            letterSpacing = 1.sp,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            specs.interfaces.forEach { interfaceName ->
                                SuggestionChip(
                                    onClick = { },
                                    label = {
                                        Text(
                                            text = interfaceName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontFamily = FontFamily.Monospace,
                                        )
                                    },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    ),
                                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecItemRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * Card 2: Mapeamento de Pinos (Pinout) - Estilo Laboratório.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinoutCard(
    pinout: List<PinoutItem>,
    modifier: Modifier = Modifier,
) {
    var pinFilterQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Alimentação", "Digital/PWM", "Analógico", "Comunicação")

    val filteredPinout = pinout.filter { item ->
        val matchesQuery = pinFilterQuery.isBlank() ||
                item.pino.contains(pinFilterQuery, ignoreCase = true) ||
                item.funcao.contains(pinFilterQuery, ignoreCase = true) ||
                item.notas.contains(pinFilterQuery, ignoreCase = true)

        val matchesCategory = when (selectedCategory) {
            "Alimentação" -> item.pino.contains("V", ignoreCase = true) || item.pino.contains("GND", ignoreCase = true) || item.funcao.contains("alimentação", ignoreCase = true) || item.funcao.contains("terra", ignoreCase = true)
            "Digital/PWM" -> item.pino.contains("GPIO", ignoreCase = true) || item.pino.contains("D", ignoreCase = true) || item.funcao.contains("PWM", ignoreCase = true) || item.funcao.contains("digital", ignoreCase = true)
            "Analógico" -> item.pino.contains("ADC", ignoreCase = true) || item.pino.contains("A", ignoreCase = true) || item.funcao.contains("analógic", ignoreCase = true) || item.funcao.contains("DAC", ignoreCase = true)
            "Comunicação" -> item.pino.contains("TX", ignoreCase = true) || item.pino.contains("RX", ignoreCase = true) || item.pino.contains("SDA", ignoreCase = true) || item.pino.contains("SCL", ignoreCase = true) || item.funcao.contains("UART", ignoreCase = true) || item.funcao.contains("I2C", ignoreCase = true) || item.funcao.contains("SPI", ignoreCase = true)
            else -> true
        }

        matchesQuery && matchesCategory
    }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SettingsInputComponent,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "PINOUT MAPPING",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = "${pinout.size} PINS",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            if (pinout.isNotEmpty()) {
                OutlinedTextField(
                    value = pinFilterQuery,
                    onValueChange = { pinFilterQuery = it },
                    placeholder = { Text("Buscar GPIO, 3V3, GND, I2C...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                    trailingIcon = {
                        if (pinFilterQuery.isNotEmpty()) {
                            IconButton(onClick = { pinFilterQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Error,
                                    contentDescription = "Limpar filtro",
                                )
                            }
                        }
                    },
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (filteredPinout.isEmpty()) {
                Text(
                    text = if (pinout.isEmpty()) "Nenhum detalhe de pinout disponível." else "Nenhum pino encontrado para os filtros.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        filteredPinout.forEachIndexed { index, item ->
                            PinoutRowItem(item = item)
                            if (index < filteredPinout.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinoutRowItem(item: PinoutItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(6.dp),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            modifier = Modifier.widthIn(min = 68.dp),
        ) {
            Text(
                text = item.pino,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.funcao,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (item.notas.isNotBlank()) {
                Text(
                    text = item.notas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Card 3: Alertas de Bancada (Bench Safety).
 */
@Composable
fun BenchAlertsCard(
    alertas: List<AlertaItem>,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = AmberWarning,
                )
                Text(
                    text = "BENCH SAFETY & ALERTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = AmberWarning,
                    letterSpacing = 1.5.sp,
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            if (alertas.isEmpty()) {
                Text(
                    text = "Nenhum alerta crítico registrado para este componente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    alertas.forEach { alerta ->
                        AlertItemCard(alerta = alerta)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertItemCard(alerta: AlertaItem) {
    val (bgColor, borderColor, textColor, badgeText) = when (alerta.nivel.uppercase()) {
        "PERIGO" -> Quadruple(
            RedDangerContainer,
            RedDanger,
            RedDangerOnContainer,
            "PERIGO",
        )
        "INFO" -> Quadruple(
            InfoBlueContainer,
            InfoBlue,
            InfoBlueOnContainer,
            "INFO",
        )
        else -> Quadruple(
            AmberWarningContainer,
            AmberWarningBorder,
            AmberWarningOnContainer,
            "ATENÇÃO",
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = if (alerta.nivel.uppercase() == "PERIGO") Icons.Rounded.Error else Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = borderColor,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = alerta.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                    )
                }

                Surface(
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            Text(
                text = alerta.descricao,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.9f),
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Card 4: Código de Teste - Estilo Mini Editor Técnico.
 */
@Composable
fun TestCodeCard(
    codigo: CodigoExemplo,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "TEST CODE / FIRMWARE",
                        style = MaterialTheme.typography.labelMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp,
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Text(
                        text = codigo.linguagem,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            if (codigo.descricao.isNotBlank()) {
                Text(
                    text = codigo.descricao,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Bloco estilo Mini Editor de Código
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CodeBackgroundDark)
                    .padding(12.dp),
            ) {
                Text(
                    text = codigo.codigo,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                    ),
                    color = CodeTextLight,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Botão de Copiar Código com feedback "Copied ✓"
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(codigo.codigo))
                    copied = true
                    Toast.makeText(context, "Código copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (copied) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primaryContainer,
                    contentColor = if (copied) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (copied) "COPIED ✓" else "COPY CODE",
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
