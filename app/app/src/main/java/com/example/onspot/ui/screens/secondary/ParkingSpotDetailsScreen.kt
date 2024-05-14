package com.example.onspot.ui.screens.secondary

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.onspot.navigation.Screens
import com.example.onspot.ui.components.CustomAlertDialog
import com.example.onspot.ui.components.CustomButton
import com.example.onspot.ui.components.CustomTopBar
import com.example.onspot.utils.Resource
import com.example.onspot.utils.openPdf
import com.example.onspot.viewmodel.ParkingSpotViewModel
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ParkingSpotDetailsScreen(
    navController: NavController,
    id: String,
    parkingSpotViewModel: ParkingSpotViewModel = viewModel()
) {
    val parkingSpotDetails by parkingSpotViewModel.parkingSpotDetails.collectAsState()

    var address by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var documentUrl by rememberSaveable { mutableStateOf("") }
    var showDeleteConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deleteParkingSpotState = parkingSpotViewModel.deleteParkingSpotState.collectAsState(initial = null)

    LaunchedEffect(key1 = id) {
        parkingSpotViewModel.fetchParkingSpotDetails(id)
    }

    when (parkingSpotDetails) {
        is Resource.Loading -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            if (address.isEmpty() && number.isEmpty()) {
                parkingSpotDetails.data?.let { parkingSpot ->
                    address = parkingSpot.address
                    number = parkingSpot.number.toString()
                    documentUrl = parkingSpot.documentUrl
                }
            }
        }
        is Resource.Error -> {
            LaunchedEffect(key1 = true) {
                Toast.makeText(context, "Error fetching parking spot details", Toast.LENGTH_LONG).show()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                CustomTopBar(
                    title = "Parking spot details",
                    onBackClick = { navController.popBackStack() },
                    icon = Icons.Default.DeleteOutline,
                    onIconClick = { showDeleteConfirmationDialog = true }
                )
            },
            bottomBar = {
                CustomButton(
                    onClick = {
                        scope.launch {
                        }
                    },
                    buttonText = "OK",
                    enabled = true,
                    modifier = Modifier.padding(bottom = 30.dp)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 30.dp, vertical = 30.dp),
            ) {
                Text(text = "Address: $address")
                Text(text = "Number: $number")
                if (documentUrl.isNotEmpty()) {
                    Row {
                        AssistChip(
                            onClick = { openPdf(context, documentUrl, id) },
                            label = { Text(text = "View contract") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "Open PDF",
                                    tint = Color(0xFF9E1B1B)
                                )
                            }
                        )
                        IconButton(
                            onClick = { /*TODO*/ }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit PDF"
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            if (deleteParkingSpotState.value?.isLoading == true) {
                CircularProgressIndicator()
            }
        }
    }
    if (showDeleteConfirmationDialog) {
        CustomAlertDialog(
            title = "Delete parking spot confirmation",
            text = "Are you sure you want to delete this parking spot?",
            confirmButtonText = "Yes",
            dismissButtonText = "No",
            onConfirm = {
                scope.launch {
                    parkingSpotViewModel.deleteParkingSpot(id)
                    showDeleteConfirmationDialog = false
                }
            },
            onDismiss = { showDeleteConfirmationDialog = false }
        )
    }
    LaunchedEffect(key1 = deleteParkingSpotState.value?.isSuccess) {
        scope.launch {
            if (deleteParkingSpotState.value?.isSuccess?.isNotEmpty() == true) {
                val success = deleteParkingSpotState.value?.isSuccess
                navController.navigate(Screens.UserProfileScreen.route)
                Toast.makeText(context, "$success", Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(key1 = deleteParkingSpotState.value?.isError) {
        scope.launch {
            if (deleteParkingSpotState.value?.isError?.isNotEmpty() == true) {
                val error = deleteParkingSpotState.value?.isError
                Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
            }
        }
    }
}