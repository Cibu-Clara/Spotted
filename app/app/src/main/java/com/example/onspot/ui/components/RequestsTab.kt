package com.example.onspot.ui.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.onspot.data.model.ReservationDetails
import com.example.onspot.ui.theme.RegularFont
import com.example.onspot.ui.theme.blue
import com.example.onspot.ui.theme.green
import com.example.onspot.ui.theme.purple
import com.example.onspot.ui.theme.red
import com.example.onspot.ui.theme.yellow
import com.example.onspot.utils.Resource
import com.example.onspot.viewmodel.ReservationViewModel
import com.example.onspot.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@Composable
fun RequestsTab(
    reservationViewModel: ReservationViewModel
) {
    val reservationDetails by reservationViewModel.reservationsDetails.collectAsState()
    val context = LocalContext.current

    when (reservationDetails) {
        is Resource.Loading -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        }
        is Resource.Success -> {
            val reservations = (reservationDetails as Resource.Success<List<ReservationDetails>>).data
            ReservationsList(reservations ?: emptyList(), reservationViewModel)
        }
        is Resource.Error -> {
            LaunchedEffect(key1 = true) {
                Toast.makeText(context, "Error fetching reservations", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@Composable
fun ReservationsList(reservations: List<ReservationDetails>, reservationViewModel: ReservationViewModel) {
    if (reservations.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You do not have any parking spot",
                fontSize = 20.sp,
                fontFamily = RegularFont,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Text(
                text = "reservations at the moment",
                fontSize = 20.sp,
                fontFamily = RegularFont,
                color = Color.Gray,
            )
        }
    }
    LazyColumn {
        items(reservations) { details ->
            ReservationCard(details, reservationViewModel)
        }
    }
}

@Composable
fun ReservationCard(
    details: ReservationDetails,
    reservationViewModel: ReservationViewModel,
    searchViewModel: SearchViewModel = viewModel()
) {
    val statusColor = when (details.reservation.status) {
        "Pending" -> yellow
        "Accepted" -> green
        "Rejected" -> red
        "Canceled" -> Color.Gray
        "Expired" -> Color.Gray
        "Completed" -> blue
        else -> Color.Black
    }
    var showMoreDetails by rememberSaveable { mutableStateOf(false) }
    var showCancelDialog by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val deleteReservationState = reservationViewModel.deleteReservationState.collectAsState(initial = null)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "• ", color = statusColor)
                Text(
                    text = details.reservation.status,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = RegularFont,
                    fontSize = 14.sp
                )
            }
            if (details.reservation.status == "Completed") {
                Text(
                    text = "Leave review for ${details.user.firstName}",
                    fontSize = 13.sp,
                    fontFamily = RegularFont,
                    textDecoration = TextDecoration.Underline,
                    color = purple,
                    modifier = Modifier
                        .padding(bottom = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { }
                )
            } else if (details.reservation.status == "Accepted" || details.reservation.status == "Pending") {
                Text(
                    text = "Cancel reservation",
                    fontSize = 14.sp,
                    fontFamily = RegularFont,
                    color = purple,
                    modifier = Modifier
                        .padding(vertical = 5.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showCancelDialog = true }
                )
            }
            Text(
                text = "${details.parkingSpot.address}, ${details.parkingSpot.city}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = RegularFont
            )
            Text(
                text = "${details.vehicle.licensePlate} ${details.vehicle.make} ${details.vehicle.model}",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color.DarkGray,
                fontFamily = RegularFont
            )
            Text(
                text = "Starting on ${details.reservation.startDate} at ${details.reservation.startTime}",
                fontSize = 14.sp,
                fontFamily = RegularFont
            )
            Text(
                text = "Ending on ${details.reservation.endDate} at ${details.reservation.endTime}",
                fontSize = 14.sp,
                fontFamily = RegularFont
            )
            if (details.reservation.status == "Pending" || details.reservation.status == "Accepted") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMoreDetails = !showMoreDetails }
                        .padding(top = 5.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (showMoreDetails) {
                        Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Less details")
                    } else {
                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "More details")
                    }
                }
                if (showMoreDetails) {
                    when (details.reservation.status) {
                        "Pending" -> {
                            Text(
                                text = "More details will be provided after ${details.user.firstName} accepts your reservation request.",
                                fontFamily = RegularFont,
                                color = Color.Gray,
                                fontStyle = FontStyle.Italic
                            )
                        }
                        "Accepted" -> {
                            Column(
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "Bay number: ${details.parkingSpot.bayNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = RegularFont,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                if (details.parkingSpot.additionalInfo != "") {
                                    Row {
                                        Text(
                                            text = details.parkingSpot.additionalInfo,
                                            fontFamily = RegularFont,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = " — ${details.user.firstName}",
                                            fontFamily = RegularFont,
                                            fontSize = 14.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "No additional information.",
                                        fontFamily = RegularFont,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp
                                    )
                                }
                                Box {
                                    Image(
                                        painter = rememberAsyncImagePainter(details.parkingSpot.photoUrl),
                                        contentDescription = "Parking Spot Picture",
                                        modifier = Modifier
                                            .height(250.dp)
                                            .padding(20.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCancelDialog) {
        CustomAlertDialog(
            title = "Cancel confirmation",
            text = "Are you sure you want to cancel this reservation?",
            onConfirm = {
                scope.launch {
                    Log.e("lool", details.vehicle.chosen.toString())
                    searchViewModel.toggleVehicleChosen(details.vehicle.uuid)
                    Log.e("lool", details.vehicle.chosen.toString())

                    reservationViewModel.deleteReservation(details.reservation.uuid)
                }
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false },
            confirmButtonText = "Yes",
            dismissButtonText = "No"
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        if (deleteReservationState.value?.isLoading == true) {
            CircularProgressIndicator()
        }
    }
    LaunchedEffect(key1 = deleteReservationState.value?.isSuccess) {
        scope.launch {
            if (deleteReservationState.value?.isSuccess?.isNotEmpty() == true) {
                val success = deleteReservationState.value?.isSuccess
                Toast.makeText(context, "$success", Toast.LENGTH_LONG).show()
            }
        }
    }
    LaunchedEffect(key1 = deleteReservationState.value?.isError) {
        scope.launch {
            if (deleteReservationState.value?.isError?.isNotEmpty() == true) {
                val error = deleteReservationState.value?.isError
                Toast.makeText(context, "$error", Toast.LENGTH_LONG).show()
            }
        }
    }
}
