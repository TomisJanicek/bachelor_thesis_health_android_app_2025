package cz.tomasjanicek.bp.ui.screens.examination.detail


import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoDisturb
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cz.tomasjanicek.bp.model.ExaminationStatus
import cz.tomasjanicek.bp.model.ExaminationWithDoctor
import cz.tomasjanicek.bp.navigation.INavigationRouter
import cz.tomasjanicek.bp.ui.elements.TagChip
import cz.tomasjanicek.bp.ui.theme.MyBlack
import cz.tomasjanicek.bp.ui.theme.MyPink
import cz.tomasjanicek.bp.ui.theme.MyWhite
import cz.tomasjanicek.bp.utils.DateUtils
import androidx.core.net.toUri
import cz.tomasjanicek.bp.model.Examination
import cz.tomasjanicek.bp.ui.elements.StatusSelector
import cz.tomasjanicek.bp.ui.screens.examination.components.ExaminationItemCard
import cz.tomasjanicek.bp.ui.screens.examination.components.ExaminationSheetContent
import cz.tomasjanicek.bp.ui.screens.examination.components.InfoCard
import cz.tomasjanicek.bp.ui.screens.examination.components.Section
import cz.tomasjanicek.bp.ui.screens.examination.list.ExaminationFilterType
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.utils.getDrawableResourceId
import java.time.Instant
import java.time.ZoneOffset
import cz.tomasjanicek.bp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailOfExaminationScreen(
    navigationRouter: INavigationRouter,
    doctorId: Long // <<< ZMĚNA ZDE: z examinationId na doctorId
) {
    val viewModel: DetailOfExaminationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val relatedExaminations by viewModel.filteredRelatedExaminations.collectAsStateWithLifecycle()
    val showUpcoming by viewModel.showUpcoming.collectAsStateWithLifecycle()
    val allRelatedExaminations by viewModel.allRelatedExaminations.collectAsStateWithLifecycle()
    val selectedExaminationForSheet by viewModel.selectedExaminationForDetail.collectAsStateWithLifecycle()

    LaunchedEffect(doctorId) { // <<< ZMĚNA ZDE
        viewModel.loadDoctorAndExaminations(doctorId) // <<< ZMĚNA ZDE
    }

    when (val state = uiState) {
        DetailOfExaminationUIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DetailOfExaminationUIState.Loaded -> {
            DetailOfExaminationContent(
                examinationWithDoctor = state.examinationWithDoctor,
                allRelatedExaminations = allRelatedExaminations,
                relatedExaminations = relatedExaminations,
                showUpcoming = showUpcoming,
                onToggleExaminations = { isUpcoming ->
                    viewModel.toggleRelatedExaminations(isUpcoming)
                },
                onNavigateBack = { navigationRouter.returBack() },
                onEditClick = {
                    // Bezpečně získáme ID z aktuálního stavu 'Loaded'
                    val mainExaminationId = (uiState as? DetailOfExaminationUIState.Loaded)
                        ?.examinationWithDoctor?.examination?.id
                    if (mainExaminationId != null) {
                    navigationRouter.navigateToAddEditExaminationScreen(mainExaminationId)
                }
                },
                // --- PŘIDEJTE TYTO NOVÉ PARAMETRY ---
                selectedExaminationForSheet = selectedExaminationForSheet,
                onHideSheet = { viewModel.hideExaminationDetailSheet() },
                onCompleteExamination = { examination, result ->
                    viewModel.completeExamination(examination, result)
                },
                onRelatedItemClick = { clickedExam ->
                    viewModel.showExaminationDetailSheet(clickedExam)
                },
                // --- PŘIDEJTE TUTO ČÁST ---
                onDeleteExamination = { examination ->
                    viewModel.deleteExamination(examination)
                },
                onEditExamination = { examinationId ->
                    viewModel.editExamination(examinationId) { id ->
                        navigationRouter.navigateToAddEditExaminationScreen(id)
                    }
                },
                onCancelExamination = { examination, reason -> // <-- PŘIDAT TUTO LAMBDU
                    viewModel.cancelExamination(examination, reason)
                }
                // --- KONEC NOVÉ ČÁSTI ---
            )
        }

        is DetailOfExaminationUIState.Error -> { // Upraveno pro zobrazení chyby
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailOfExaminationContent(
    examinationWithDoctor: ExaminationWithDoctor,
    allRelatedExaminations: List<Examination>,
    relatedExaminations: List<Examination>,
    showUpcoming: Boolean,
    onToggleExaminations: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onEditClick: () -> Unit,
    onRelatedItemClick: (Examination) -> Unit,
    selectedExaminationForSheet: Examination?,
    onHideSheet: () -> Unit,
    onCompleteExamination: (Examination, String) -> Unit,
    onDeleteExamination: (Examination) -> Unit,
    onEditExamination: (Long) -> Unit,
    onCancelExamination: (Examination, String) -> Unit // <-- PŘIDAT TENTO PARAMETR
) {
    val context = LocalContext.current
    val examination = examinationWithDoctor.examination
    val doctor = examinationWithDoctor.doctor

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val formattedDateTime = remember(examination.dateTime) {
        DateUtils.getDateTimeString(examination.dateTime)
    }

    if (selectedExaminationForSheet != null) {
        ModalBottomSheet(
            onDismissRequest = onHideSheet,
            sheetState = sheetState
        ) {
            // Obsah BottomSheetu - vytvoříme ho v dalším kroku
            ExaminationSheetContent(
                examination = selectedExaminationForSheet,
                onCompleteClick = { result ->
                    onCompleteExamination(selectedExaminationForSheet, result)
                },
                onDeleteClick = {
                    onDeleteExamination(selectedExaminationForSheet)
                },
                onEditClick = {
                    // Bezpečně získáme ID a zavoláme funkci pro úpravu
                    selectedExaminationForSheet.id?.let {
                        onEditExamination(it)
                    }
                },
                onRescheduleClick = {
                    // Pro "Naplánovat znovu" můžeme použít stejnou logiku jako pro úpravu,
                    // protože uživatele pošleme na stejnou obrazovku
                    selectedExaminationForSheet.id?.let {
                        onEditExamination(it)
                    }
                },
                        onCancelClick = { reason -> // <-- PŘIDAT TUTO AKCI
                    onCancelExamination(selectedExaminationForSheet, reason)
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zpět",
                            tint = MyBlack
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Upravit",
                            tint = MyBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.onBackground,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    // 1. Získáme bezpečně název obrázku
                    val imageName = doctor?.image

                    // 2. Pokud název existuje a není prázdný...
                    if (!imageName.isNullOrBlank()) {
                        val context = LocalContext.current
                        val imageResId = remember(imageName) {
                            context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        }

                        // 3. ...a pokud se pro něj najde platný obrázek v res/drawable...
                        if (imageResId != 0) {
                            // ...zobrazíme obrázek.
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = "Fotografie lékaře ${doctor?.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Fallback, pokud byl v DB název, ale soubor v drawable chybí (překlep)
                            // Zobrazíme barevný box a debugovací text.
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "DEBUG: Chyba! Soubor '$imageName.png' chybí v res/drawable.",
                                    color = Color.Yellow,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // 4. Pokud název obrázku v datech vůbec není (je null nebo prázdný)...
                        // ...zobrazíme barevný box a debugovací text.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                doctor?.image + "je null nebo prázdný." + doctor?.name,
                                color = Color.Yellow
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.7f)
                                    ),
                                    startY = 300f,
                                    endY = 800f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = doctor?.specialization ?: "Bez doktora",
                            style = MaterialTheme.typography.displaySmall,
                            color = MyWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    doctor?.phone?.let { phoneNumber ->
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = "tel:$phoneNumber".toUri()
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MyBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Zavolat",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    doctor?.email?.let { emailAddress ->
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = "mailto:$emailAddress".toUri()
                                        }
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MyBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Email",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    doctor?.location?.let { address ->
                                        val encodedAddress = Uri.encode(address)
                                        val geoUri = "geo:0,0?q=$encodedAddress".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                        mapIntent.setPackage("com.google.android.apps.maps")
                                        try {
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            // Zde by mohla být logika pro případ, že mapy selžou
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MyBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Navigace",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        icon = Icons.Default.CalendarMonth
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Nejbližší prohlídka",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MyBlack
                                )
                                Text(
                                    text = formattedDateTime,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = MyBlack
                                )
                            }
                            TagChip(type = examination.type)
                        }
                    }

                    val doctorDetails = remember(doctor) {
                        buildString {
                            doctor?.phone?.let { append("Tel: $it") }
                            doctor?.email?.let {
                                if (isNotEmpty()) append(" | ")
                                append("Email: $it")
                            }
                            doctor?.location?.let {
                                if (isNotEmpty()) append("\n")
                                append("Adresa: $it")
                            }
                        }
                    }

                    // Původní InfoCard pro doktora, zabalená do nové
                    InfoCard(
                        icon = Icons.Default.Person
                    ) {
                        Column {
                            Text(
                                text = doctor?.name ?: "Není přiřazen",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MyBlack
                            )
                            if (doctorDetails.isNotBlank()) {
                                Text(
                                    text = doctorDetails,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MyBlack
                                )
                            }
                        }
                    }
                }
            }

            if (!examination.note.isNullOrBlank()) {
                item {
                    Section(title = "Poznámka") {
                        Text(
                            text = examination.note,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            color = MyBlack
                        )
                    }
                }
            }

            if (!examination.result.isNullOrBlank()) {
                item {
                    Section(title = "Výsledek") {
                        Text(
                            text = examination.result,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            color = MyBlack
                        )
                    }
                }
            }

            if (allRelatedExaminations.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Section(title = "Další prohlídky u tohoto lékaře") {
                        StatusSelector(
                            selectedFilter = if (showUpcoming) ExaminationFilterType.SCHEDULED else ExaminationFilterType.HISTORY,
                            onFilterSelected = { filterType ->
                                onToggleExaminations(filterType == ExaminationFilterType.SCHEDULED)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (relatedExaminations.isEmpty()) {
                    item {
                        Text(
                            text = if (showUpcoming) "Žádné budoucí prohlídky." else "Žádné historické prohlídky.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // ZDE JE OPRAVENÝ BLOK - POUZE JEDEN `items`
                    items(relatedExaminations, key = { it.id!! }) { relatedExam ->
                        ExaminationItemCard(
                            examination = relatedExam,
                            onAddToCalendar = { examToCalendar ->
                                val intent =
                                    Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(
                                            CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                            examToCalendar.dateTime
                                        )
                                        // Můžeme přidat hodinu k délce události
                                        .putExtra(
                                            CalendarContract.EXTRA_EVENT_END_TIME,
                                            examToCalendar.dateTime + 60 * 60 * 1000
                                        )
                                        .putExtra(
                                            CalendarContract.Events.TITLE,
                                            "Lékařská prohlídka: ${examToCalendar.purpose}"
                                        )
                                        .putExtra(
                                            CalendarContract.Events.DESCRIPTION,
                                            examToCalendar.note ?: ""
                                        )
                                        .putExtra(
                                            CalendarContract.Events.EVENT_LOCATION,
                                            doctor?.location ?: ""
                                        )

                                context.startActivity(intent)
                            },
                            onItemClick = { clickedExam ->
                                // Oznámíme "ven", že se kliklo na položku
                                onRelatedItemClick(clickedExam)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun ExaminationStatus.toCzechString(): String {
    return when (this) {
        ExaminationStatus.PLANNED -> "Naplánováno"
        ExaminationStatus.COMPLETED -> "Dokončeno"
        ExaminationStatus.CANCELLED -> "Zrušeno"
        ExaminationStatus.OVERDUE -> "Po termínu"
    }
}