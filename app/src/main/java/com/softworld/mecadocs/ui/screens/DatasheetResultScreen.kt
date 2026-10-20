package com.softworld.mecadocs.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.softworld.mecadocs.R
import com.softworld.mecadocs.data.model.DatasheetResponse
import com.softworld.mecadocs.ui.components.BenchAlertsCard
import com.softworld.mecadocs.ui.components.PinoutCard
import com.softworld.mecadocs.ui.components.QuickSpecsCard
import com.softworld.mecadocs.ui.components.TestCodeCard

/**
 * Tela/Seção de resultado do Datasheet em estilo de Painel Técnico de Engenharia (Hero Header + Cards Modulares).
 */
@Composable
fun DatasheetResultScreen(
    datasheet: DatasheetResponse,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Component Hero (Cabeçalho Técnico Principal)
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.technical_datasheet),
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp,
                        )

                        Text(
                            text = datasheet.nome,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        if (datasheet.categoria.isNotBlank()) {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                            ) {
                                Text(
                                    text = datasheet.categoria.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }

                    // Ações de Topo (Favorito & Compartilhar)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        ) {
                            val removeFavText = stringResource(R.string.remove_favorites)
                            val addFavText = stringResource(R.string.add_favorite)
                            IconButton(onClick = onToggleFavorite) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = if (isFavorite) removeFavText else addFavText,
                                    tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                        ) {
                            val shareDesc = stringResource(R.string.share_datasheet)
                            IconButton(
                                onClick = {
                                    val shareText = buildString {
                                        append("📄 MecaDocs Datasheet: ${datasheet.nome}\n")
                                        append("Categoria: ${datasheet.categoria}\n\n")
                                        append("⚡ Especificações Principais:\n")
                                        append("- Tensão Operação: ${datasheet.specs.tensaoOperacao}\n")
                                        append("- Nível Lógico: ${datasheet.specs.nivelLogico}\n")
                                        append("- Corrente Máxima: ${datasheet.specs.correnteMaxima}\n\n")
                                        append("Exportado via MecaDocs (Lab Station).")
                                    }

                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Compartilhar Datasheet ${datasheet.nome}")
                                    context.startActivity(shareIntent)
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Share,
                                    contentDescription = shareDesc,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                if (datasheet.descricao.isNotBlank()) {
                    Text(
                        text = datasheet.descricao,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                    )
                }
            }
        }

        // Card 1: Especificações Rápidas
        QuickSpecsCard(specs = datasheet.specs)

        // Card 2: Pinout
        PinoutCard(pinout = datasheet.pinout)

        // Card 3: Alertas de Bancada
        BenchAlertsCard(alertas = datasheet.alertas)

        // Card 4: Código de Teste (se disponível)
        datasheet.codigo?.let { codigoExemplo ->
            TestCodeCard(codigo = codigoExemplo)
        }
    }
}
