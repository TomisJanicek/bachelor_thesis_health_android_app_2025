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
import androidx.compose.material.icons.filled.Info
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
import androidx.hilt.navigation.compose.hiltViewModel
import cz.tomasjanicek.bp.model.Examination
//import cz.tomasjanicek.bp.ui.elements.StatusSelector
import cz.tomasjanicek.bp.ui.screens.examination.components.ExaminationItemCard
import cz.tomasjanicek.bp.ui.screens.examination.components.ExaminationSheetContent
import cz.tomasjanicek.bp.ui.screens.examination.components.InfoCard
import cz.tomasjanicek.bp.ui.screens.examination.components.Section
import cz.tomasjanicek.bp.ui.theme.MyGreen
import cz.tomasjanicek.bp.ui.theme.MyRed
import cz.tomasjanicek.bp.utils.getDrawableResourceId
import java.time.Instant
import java.time.ZoneOffset
import cz.tomasjanicek.bp.R

/**
 * Jednoduchý, lokální enum jen pro filtrování v detailu lékaře.
 */
private enum class DetailFilterType(val label: String) {
    SCHEDULED("Naplánované"),
    HISTORY("Historie")
}

/**
 * Jednoduchý, lokální selector jen pro detail lékaře.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailStatusSelector(
    selectedFilter: DetailFilterType,
    onFilterSelected: (DetailFilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        DetailFilterType.values().forEachIndexed { index, filterType ->
            SegmentedButton(
                selected = (selectedFilter == filterType),
                onClick = { onFilterSelected(filterType) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = DetailFilterType.values().size
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MyPink,
                    activeContentColor = MyBlack,
                    inactiveContainerColor = MaterialTheme.colorScheme.background,
                    inactiveContentColor = MaterialTheme.colorScheme.onBackground,
                    inactiveBorderColor = MaterialTheme.colorScheme.onBackground
                )
            ) {
                Text(filterType.label)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailOfExaminationScreen(
    navigationRouter: INavigationRouter,
    doctorId: Long
) {
    val viewModel: DetailOfExaminationViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val relatedExaminations by viewModel.filteredRelatedExaminations.collectAsStateWithLifecycle()
    val showUpcoming by viewModel.showUpcoming.collectAsStateWithLifecycle()
    val allRelatedExaminations by viewModel.allRelatedExaminations.collectAsStateWithLifecycle()
    val selectedExaminationForSheet by viewModel.selectedExaminationForDetail.collectAsStateWithLifecycle()

    LaunchedEffect(doctorId) {
        viewModel.loadDoctorAndExaminations(doctorId)
    }

    when (val state = uiState) {
        DetailOfExaminationUIState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        DetailOfExaminationUIState.AllDeleted -> {
            // Jakmile nastane tento stav, okamžitě navigujeme zpět
            LaunchedEffect(Unit) {
                navigationRouter.returBack()
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
                    val mainExaminationId = (uiState as? DetailOfExaminationUIState.Loaded)
                        ?.examinationWithDoctor?.examination?.id
                    if (mainExaminationId != null) {
                        navigationRouter.navigateToAddEditExaminationScreen(mainExaminationId)
                    }
                },
                onEditDoctorClick = {
                    val doctorIdToEdit = state.examinationWithDoctor.doctor?.id
                    if (doctorIdToEdit != null) {
                        navigationRouter.navigateToDoctorEditScreen(doctorIdToEdit)
                    }
                },
                selectedExaminationForSheet = selectedExaminationForSheet,
                onHideSheet = { viewModel.hideExaminationDetailSheet() },
                onCompleteExamination = { examination, result ->
                    viewModel.completeExamination(examination, result)
                },
                onRelatedItemClick = { clickedExam ->
                    viewModel.showExaminationDetailSheet(clickedExam)
                },
                onDeleteExamination = { examination ->
                    viewModel.deleteExamination(examination)
                },
                onEditExamination = { examinationId ->
                    viewModel.editExamination(examinationId) { id ->
                        navigationRouter.navigateToAddEditExaminationScreen(id)
                    }
                },
                onCancelExamination = { examination, reason ->
                    viewModel.cancelExamination(examination, reason)
                }
            )
        }

        is DetailOfExaminationUIState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = MyRed)
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
    onEditDoctorClick: () -> Unit,
    onRelatedItemClick: (Examination) -> Unit,
    selectedExaminationForSheet: Examination?,
    onHideSheet: () -> Unit,
    onCompleteExamination: (Examination, String) -> Unit,
    onDeleteExamination: (Examination) -> Unit,
    onEditExamination: (Long) -> Unit,
    onCancelExamination: (Examination, String) -> Unit
) {
    val context = LocalContext.current
    val doctor = examinationWithDoctor.doctor

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (selectedExaminationForSheet != null) {
        ModalBottomSheet(
            onDismissRequest = onHideSheet,
            sheetState = sheetState
        ) {
            ExaminationSheetContent(
                examination = selectedExaminationForSheet,
                onCompleteClick = { result ->
                    onCompleteExamination(selectedExaminationForSheet, result)
                },
                onDeleteClick = {
                    onDeleteExamination(selectedExaminationForSheet)
                },
                onEditClick = {
                    selectedExaminationForSheet.id?.let {
                        onEditExamination(it)
                    }
                },
                onRescheduleClick = {
                    selectedExaminationForSheet.id?.let {
                        onEditExamination(it)
                    }
                },
                onCancelClick = { reason ->
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
                    IconButton(onClick = onEditDoctorClick) {
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
        containerColor = MaterialTheme.colorScheme.background,
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
                    val imageName = doctor?.image
                    if (!imageName.isNullOrBlank()) {
                        val imageResId = remember(imageName) {
                            context.resources.getIdentifier(imageName, "drawable", context.packageName)
                        }

                        if (imageResId != 0) {
                            Image(
                                painter = painterResource(id = imageResId),
                                contentDescription = "Fotografie lékaře ${doctor.name}",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
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
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                doctor?.specialization?.first()?.toString() ?: "?",
                                style = MaterialTheme.typography.displayLarge,
                                color = MyWhite
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
                            text = doctor?.specialization ?: "Bez specializace",
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
                                enabled = (doctor?.phone != null) || !doctor?.addressLabel.isNullOrBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MyBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(text = "Zavolat", style = MaterialTheme.typography.labelMedium)
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
                                enabled = (doctor?.email != null) || !doctor?.addressLabel.isNullOrBlank(),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(text = "Email", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            Button(
                                onClick = {
                                    if (doctor?.latitude != null && doctor.longitude != null) {
                                        val lat = doctor.latitude
                                        val lng = doctor.longitude
                                        val intentUri = "geo:$lat,$lng?q=$lat,$lng(${doctor.name})".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(mapIntent)
                                    } else if (!doctor?.addressLabel.isNullOrBlank()) {
                                        val encodedAddress = Uri.encode(doctor.addressLabel)
                                        val geoUri = "geo:0,0?q=$encodedAddress".toUri()
                                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        context.startActivity(mapIntent)
                                    }
                                },
                                enabled = (doctor?.latitude != null && doctor.longitude != null) || !doctor?.addressLabel.isNullOrBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MyBlack
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Text(text = "Navigace", style = MaterialTheme.typography.labelMedium)
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
                ) {
                    val doctorDetails = remember(doctor) {
                        buildString {
                            doctor?.phone?.let { append("Tel: $it") }
                            doctor?.email?.let {
                                if (isNotEmpty()) append("\n")
                                append("Email: $it")
                            }
                            doctor?.addressLabel?.let {
                                if (isNotEmpty()) append("\n")
                                append("Adresa: $it")
                            }
                        }
                    }

                    InfoCard(icon = Icons.Default.Person) {
                        Column {
                            Text(
                                text = doctor?.name ?: "Není přiřazen",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (doctorDetails.isNotBlank()) {
                                Text(
                                    text = doctorDetails,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                            if (doctor?.subtitle?.isNotBlank() == true) {
                                Text(
                                    text = doctor.subtitle!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }

            if (allRelatedExaminations.isNotEmpty()) {
                item {
                    Section(title = "Prohlídky u tohoto lékaře") {
                        // --- KROK 2: Použití nového lokálního selectoru ---
                        DetailStatusSelector(
                            selectedFilter = if (showUpcoming) DetailFilterType.SCHEDULED else DetailFilterType.HISTORY,
                            onFilterSelected = { filterType ->
                                onToggleExaminations(filterType == DetailFilterType.SCHEDULED)
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
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    items(relatedExaminations, key = { it.id!! }) { relatedExam ->
                        ExaminationItemCard(
                            examination = relatedExam,
                            onAddToCalendar = { examToCalendar ->
                                val intent =
                                    Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
                                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, examToCalendar.dateTime)
                                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, examToCalendar.dateTime + 60 * 60 * 1000)
                                        .putExtra(CalendarContract.Events.TITLE, "Lékařská prohlídka: ${examToCalendar.purpose}")
                                        .putExtra(CalendarContract.Events.DESCRIPTION, examToCalendar.note ?: "")
                                        .putExtra(CalendarContract.Events.EVENT_LOCATION, doctor?.addressLabel ?: "")
                                context.startActivity(intent)
                            },
                            onItemClick = { clickedExam ->
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