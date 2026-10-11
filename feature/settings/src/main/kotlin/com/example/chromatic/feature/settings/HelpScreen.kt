package com.example.chromatic.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Offline quick-reference + contact via intents (intents hand off — no permission, v18 §4). */
@Composable
fun HelpScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Row {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
            }
            Text(stringResource(R.string.help), style = MaterialTheme.typography.headlineSmall)
        }

        Text(
            stringResource(R.string.help_reference),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp).semantics { heading() },
        )
        listOf(R.string.help_ref_aa, R.string.help_ref_tint, R.string.help_ref_603010).forEach { res ->
            ElevatedCard(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    stringResource(res),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                )
            }
        }

        Text(
            stringResource(R.string.help_faq_accounts),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(stringResource(R.string.help_faq_storage), style = MaterialTheme.typography.bodySmall)
        Text(stringResource(R.string.help_faq_internet), style = MaterialTheme.typography.bodySmall)

        Text(
            stringResource(R.string.help_contact),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp).semantics { heading() },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@chromatic.app?subject=Chromatic%20support")),
                    )
                },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.help_email)) }
            OutlinedButton(
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chromatic-app/issues")))
                },
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.help_issues)) }
        }
        Spacer(Modifier.padding(bottom = 32.dp))
    }
}
