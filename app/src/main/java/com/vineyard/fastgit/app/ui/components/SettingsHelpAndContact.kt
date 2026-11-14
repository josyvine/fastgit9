package com.vineyard.fastgit.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.vineyard.fastgit.app.ui.theme.GhErrorRed

private data class HelpGuideSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val steps: List<String>
)

@Composable
fun SettingsHelpContent() {
    val sections = remember {
        listOf(
            HelpGuideSection(
                title = "A. Getting Started & Authentication",
                subtitle = "Sign in with GitHub OAuth, Device Flow, Demo Mode, and Multi-Account switching",
                icon = Icons.Default.Login,
                steps = listOf(
                    "GitHub OAuth Login: Tap 'Sign in with GitHub' to authorize via browser and return automatically via deep link.",
                    "Device Flow Login: Use GitHub Device Flow to generate a one-time code, enter it on github.com/login/device, and wait for automatic approval.",
                    "Custom OAuth Client ID: Optionally configure or reset your own GitHub OAuth Client ID from the Auth screen.",
                    "Demo Mode: Explore FastGit offline with sample repositories, commits, and notifications without signing in.",
                    "Multi-Account Support: In Settings → Auth (or Profile), tap Switch / Manage Accounts to add multiple GitHub accounts, switch active sessions anytime, or sign out individually/all at once."
                )
            ),
            HelpGuideSection(
                title = "B. Home Workspace",
                subtitle = "Quick Actions, Recent Repositories, and Recent Commits",
                icon = Icons.Default.Home,
                steps = listOf(
                    "Quick Actions: Tap 'Create Repo' to start a new GitHub project or 'Import Repo' to clone an existing public/accessible GitHub repository URL into your account.",
                    "Recent Repositories: Shows your 5 most recently updated repositories—tap any card to open its workspace.",
                    "Recent Commits: Displays the latest commits from your most recently updated repository."
                )
            ),
            HelpGuideSection(
                title = "C. Repositories & Global Search",
                subtitle = "Browse all repositories, filter, search GitHub, create, import, download ZIP, or delete",
                icon = Icons.Default.Folder,
                steps = listOf(
                    "Full Repository Loading: Scroll down in the Repos tab to automatically load all repositories across your account (229+ and growing). Swipe down anytime to Pull-to-Refresh.",
                    "Filter Chips: Narrow down repositories by All, Public, Private, Sources, or Forks.",
                    "Dual Search: Type in 'Search repositories...' to search both your own repositories (including older/private ones) AND global public GitHub repositories across other users.",
                    "Create Repo (+): Tap the green '+' button to create a repository with a name, optional description, Public/Private toggle, and README initialization.",
                    "Import Repo (Cloud Icon): Paste a GitHub repository URL, choose a target repository name and visibility, and FastGit downloads, extracts, and commits all files with live progress and Cancel support.",
                    "Card Actions: Open a repository, download it as a .zip archive to Downloads/FastGit, or delete a repository with confirmation."
                )
            ),
            HelpGuideSection(
                title = "D. Repository Workspace & Code Editor",
                subtitle = "Files, Branches, Code Editor, Commits, Issues, Pull Requests, Releases & Actions",
                icon = Icons.Default.Code,
                steps = listOf(
                    "Code & File Tree: Browse folders recursively, switch branches, create new branches, or delete branches.",
                    "File Operations: Create new files, upload individual files, or upload a .zip file (auto-extracted preserving folder structure), download files/folders, or delete files.",
                    "Code Editor: Tap any code/text file to view or edit with syntax highlighting and line numbers, then commit changes directly with a custom commit message.",
                    "Commits Tab: View commit history, authors, and commit SHAs for the active branch.",
                    "Issues Tab: Filter All/Open/Closed issues, create new issues, and open or close existing issues.",
                    "Pull Requests Tab: Browse Pull Requests, create a new PR between branches, and merge open Pull Requests.",
                    "Actions (CI/CD) Tab: View GitHub Actions workflows, trigger manual workflow_dispatch runs, monitor workflow status, inspect job steps & live job logs, and download build artifacts (such as .apk files).",
                    "Releases Tab: View published releases, download release assets, or create a new release with tag, title, notes, draft, and prerelease options."
                )
            ),
            HelpGuideSection(
                title = "E. Alerts (Notifications)",
                subtitle = "Track GitHub activity and mark notifications as read",
                icon = Icons.Default.Notifications,
                steps = listOf(
                    "Open the Alerts tab to view Pull Request, Issue, Commit, and Workflow notifications from GitHub.",
                    "Tap an unread notification to mark it as read, and swipe/refresh to load the latest alerts."
                )
            ),
            HelpGuideSection(
                title = "F. Profile Overview",
                subtitle = "Account details, stats, and pinned/recent projects",
                icon = Icons.Default.Person,
                steps = listOf(
                    "Displays your GitHub avatar, name, @username, bio, company, location, public repository count, followers, and following.",
                    "Tap any listed repository under Profile to jump straight into its workspace."
                )
            ),
            HelpGuideSection(
                title = "G. Settings Tools (App, Keys, URLs, Auth, Help & About)",
                subtitle = "Themes, Cache, Keystore Secrets Propagation, Raw URL Downloader, and Support",
                icon = Icons.Default.Settings,
                steps = listOf(
                    "App Tab: Tap Theme Mode to cycle between Light, Dark, and System themes. View your Downloads path (Internal Storage / Downloads / FastGit) and tap Clear Cache to free offline DB/cached trees.",
                    "Keys Tab (Automated Keystore Secrets): Expand 'Add New Keystore Profile' to save your Alias, Keystore Base64, Keystore Password, Key Alias, and Key Password locally. Select a target repository and profile, then tap 'Propagate Keystore Secrets' to encrypt (Libsodium Sealed Box) and upload KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, and KEY_PASSWORD to GitHub Actions Secrets.",
                    "URLs Tab (Raw URL Downloader): Select any repository and a specific folder (or root) to recursively scan all files and export a numbered list of raw.githubusercontent.com URLs to Downloads/FastGit/Urls.",
                    "Auth Tab: View your active session, switch or manage signed-in accounts, add another account, or log out.",
                    "Help Tab: Complete A-to-Z feature reference guide (this screen).",
                    "About Tab: View app version details and tap Contact Us to open the full-screen support message form."
                )
            ),
            HelpGuideSection(
                title = "H. Live Log Console Overlay",
                subtitle = "Real-time diagnostics and API process monitoring",
                icon = Icons.Default.Terminal,
                steps = listOf(
                    "Tap the floating console button at the bottom-right corner on any screen to open the Live Log Console.",
                    "Monitor real-time API calls, OAuth exchanges, ZIP import progress, secret propagation logs, and errors.",
                    "Use the console controls to clear logs or close the overlay without interrupting your current screen."
                )
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "FastGit Complete Guide (A to Z)",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Step-by-step documentation for every feature and tool in FastGit.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        sections.forEach { section ->
            HelpGuideCard(section = section)
        }
    }
}

@Composable
private fun HelpGuideCard(section: HelpGuideSection) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = ButtonDefaults.outlinedButtonBorder(enabled = true),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = section.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = section.title,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp
                    )
                    Text(
                        text = section.subtitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    section.steps.forEach { step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = step,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsFullScreenDialog(
    isSending: Boolean,
    errorMessage: String?,
    onSend: (name: String, email: String, message: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .imePadding()
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Contact Us",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { if (!isSending) onDismiss() },
                            enabled = !isSending
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Send us your questions, feedback, or bug reports directly and we will get back to you.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        lineHeight = 19.sp
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name *") },
                        placeholder = { Text("Enter your name") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        enabled = !isSending,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email *") },
                        placeholder = { Text("Enter your email address") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        enabled = !isSending,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Message *") },
                        placeholder = { Text("Type your message here...") },
                        minLines = 6,
                        maxLines = 12,
                        enabled = !isSending,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!errorMessage.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = GhErrorRed.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage,
                                color = GhErrorRed,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { onSend(name, email, message) },
                        enabled = !isSending,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.surface,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sending...",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surface
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Send Message",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.surface
                            )
                        }
                    }
                }
            }
        }
    }
}