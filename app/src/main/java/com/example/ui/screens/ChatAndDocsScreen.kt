package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatMessageEntity
import com.example.data.local.DocumentEntity
import com.example.ui.DispatchViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatAndDocsScreen(
    viewModel: DispatchViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Messages, 1 = Document Chest
    val chatMessages by viewModel.chatMessages.collectAsState()
    val documents by viewModel.documents.collectAsState()

    var isUploadDocDialogOpen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header (Tabs)
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().testTag("chat_docs_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Driver Dispatch Chat", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Forum, contentDescription = "Messages") },
                    modifier = Modifier.testTag("chat_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Logistics Vault", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.FolderZip, contentDescription = "Vault") },
                    modifier = Modifier.testTag("docs_tab")
                )
            }

            // Tab Content
            if (selectedTab == 0) {
                ChatLayout(
                    messages = chatMessages,
                    onSendMessage = { text -> viewModel.sendChatMessage(text) }
                )
            } else {
                DocumentsLayout(
                    documents = documents,
                    onTriggerUpload = { isUploadDocDialogOpen = true },
                    onDeleteDoc = { viewModel.deleteDocument(it) }
                )
            }
        }

        // Upload Document Dialog
        if (isUploadDocDialogOpen) {
            UploadDocDialog(
                onDismiss = { isUploadDocDialogOpen = false },
                onUpload = { type, fileName, notes ->
                    viewModel.uploadDocument(type, null, null, fileName, notes)
                    isUploadDocDialogOpen = false
                }
            )
        }
    }
}

@Composable
fun ChatLayout(
    messages: List<ChatMessageEntity>,
    onSendMessage: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Automatically scroll to the latest messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Chat Stream
        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Chat board standby. Type below to ping active fleet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubble(message = msg)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Message input bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ping Elena, Marcus or Tyr..."); },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                trailingIcon = {
                    if (textInput.isNotEmpty()) {
                        IconButton(onClick = { textInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            IconButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(100.dp))
                    .size(48.dp)
                    .testTag("send_chat_message_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send Message",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessageEntity) {
    val isDispatcher = message.senderRole == "Dispatcher"
    val alignment = if (isDispatcher) Alignment.End else Alignment.Start
    val bubbleColor = if (isDispatcher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isDispatcher) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val bubbleShape = if (isDispatcher) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        horizontalAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = message.senderName,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "• ${message.senderRole}",
                fontSize = 10.sp,
                color = if (isDispatcher) AmberAccent else MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Surface(
            color = bubbleColor,
            shape = bubbleShape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.messageText,
                color = textColor,
                fontSize = 13.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun DocumentsLayout(
    documents: List<DocumentEntity>,
    onTriggerUpload: () -> Unit,
    onDeleteDoc: (DocumentEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Secure Cloud Archives", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("${documents.size} active records secured", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }

            Button(
                onClick = onTriggerUpload,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("upload_vault_btn")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = "Upload")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Scan & Upload")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (documents.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Archive empty. Upload signed PODs, CDLs or Rate Confirmations.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(documents, key = { it.id }) { doc ->
                    DocumentCard(doc = doc, onDelete = { onDeleteDoc(doc) })
                }
            }
        }
    }
}

@Composable
fun DocumentCard(
    doc: DocumentEntity,
    onDelete: () -> Unit
) {
    val icon = when (doc.type) {
        "Rate Confirmation" -> Icons.Default.Description
        "BOL" -> Icons.Default.Inventory
        "POD" -> Icons.Default.AssignmentTurnedIn
        else -> Icons.Default.CardMembership
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .testTag("document_card_${doc.fileName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = "Doc", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = doc.fileName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = doc.type,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = doc.uploadDate,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }

                    if (doc.notes.isNotEmpty()) {
                        Text(
                            text = doc.notes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_doc_${doc.id}")) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun UploadDocDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String, String) -> Unit
) {
    var selectedType by remember { mutableStateOf("Rate Confirmation") }
    var fileName by remember { mutableStateOf("Signed_RC_LD-8802.pdf") }
    var notes by remember { mutableStateOf("Signed at pickup terminal.") }

    val types = listOf("Rate Confirmation", "BOL", "POD", "Driver CDL")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Logistics Cloud Archive Upload") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Select Document Category:")
                types.chunked(2).forEach { rowTypes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowTypes.forEach { tp ->
                            val isSelected = selectedType == tp
                            ElevatedFilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedType = tp
                                    // Update filename suggestion based on type
                                    fileName = when (tp) {
                                        "Rate Confirmation" -> "Signed_RC_LD-8802.pdf"
                                        "BOL" -> "BOL_LD-8802_GARY.pdf"
                                        "POD" -> "POD_LD-8801_CHICAGO.jpg"
                                        else -> "Driver_CDL_Copy.jpg"
                                    }
                                },
                                label = { Text(tp, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("upload_file_name_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onUpload(selectedType, fileName, notes) },
                modifier = Modifier.testTag("submit_doc_upload_dialog")
            ) {
                Text("Secure Upload")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
