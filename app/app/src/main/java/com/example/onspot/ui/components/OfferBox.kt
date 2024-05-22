package com.example.onspot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.onspot.data.model.ParkingSpot
import com.example.onspot.ui.theme.RegularFont
import com.example.onspot.ui.theme.lightPurple
import com.example.onspot.viewmodel.OfferViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Composable
fun OfferBox(
    parkingSpots: List<ParkingSpot>,
    showOfferBox: MutableState<Boolean>,
    showMap: MutableState<Boolean>,
    offerViewModel: OfferViewModel,
    modifier: Modifier = Modifier
) {
    var parkingSpotId by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var isStartDateEmpty by rememberSaveable { mutableStateOf(true) }
    var startTime by rememberSaveable { mutableStateOf(LocalTime.NOON) }
    var isStartTimeEmpty by rememberSaveable { mutableStateOf(true) }
    var endDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var isEndDateEmpty by rememberSaveable { mutableStateOf(true) }
    var endTime by rememberSaveable { mutableStateOf(LocalTime.NOON) }
    var isEndTimeEmpty by rememberSaveable { mutableStateOf(true) }
    val isButtonEnabled = parkingSpotId.isNotEmpty() && !isStartDateEmpty &&
            !isStartTimeEmpty && !isEndDateEmpty && !isEndTimeEmpty

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(color = lightPurple, shape = RoundedCornerShape(20.dp)),
    ) {
        Column {
            Text(
                modifier = Modifier.padding(20.dp),
                text = "Which of your parking spots would you like to offer?",
                fontSize = 15.sp,
                fontFamily = RegularFont
            )
            DropDownMenuComponent(
                label = "Select one of your parking spots",
                options = parkingSpots,
                onTextSelected = { parkingSpotId = it}
            )
            Text(
                modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp),
                text = "When will your parking spot be available?",
                fontSize = 15.sp,
                fontFamily = RegularFont
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                Text(text = "•", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "From:",
                    fontFamily = RegularFont,
                    fontSize = 15.sp,
                )
            }
            Row(modifier = Modifier.padding(horizontal = 15.dp)) {
                DatePicker(
                    label = "Date",
                    onDateSelected = {
                        startDate = it
                        isStartDateEmpty = false
                    },
                    greaterThan = LocalDate.now().minusDays(1),
                    modifier = Modifier.weight(0.5f)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TimePicker(
                    label = "Time",
                    enabled = !isStartDateEmpty,
                    onTimeSelected = {
                        startTime = it
                        isStartTimeEmpty = false
                    },
                    greaterThan = if (startDate == LocalDate.now()) LocalTime.now().truncatedTo(
                        ChronoUnit.MINUTES) else LocalTime.MIN,
                    modifier = Modifier.weight(0.5f)
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp)
            ) {
                Text(text = "•", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                Text(
                    text = "To:",
                    fontFamily = RegularFont,
                    fontSize = 15.sp,
                )
            }
            Row(modifier = Modifier.padding(horizontal = 15.dp)) {
                DatePicker(
                    label = "Date",
                    enabled = !isStartDateEmpty,
                    onDateSelected = {
                        endDate = it
                        isEndDateEmpty = false
                    },
                    greaterThan = startDate.minusDays(1),
                    modifier = Modifier.weight(0.5f)

                )
                Spacer(modifier = Modifier.width(10.dp))
                TimePicker(
                    label = "Time",
                    enabled = !isStartTimeEmpty && !isEndDateEmpty,
                    onTimeSelected = {
                        endTime = it
                        isEndTimeEmpty = false
                    },
                    greaterThan = if(startDate == endDate) startTime.plusHours(1) else LocalTime.MIN,
                    modifier = Modifier.weight(0.5f)
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    enabled = isButtonEnabled,
                    onClick = {
                        offerViewModel.createMarker(UUID.randomUUID().toString(), startDate.toString(),
                            startTime.toString(), endDate.toString(), endTime.toString(), parkingSpotId)
                        showOfferBox.value = false
                        showMap.value = true
                    }
                ) {
                    Text(text = "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownMenuComponent(
    label: String,
    options: List<ParkingSpot>,
    onTextSelected: (String) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var selectedText by rememberSaveable { mutableStateOf("No parking spot selected") }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default,
                label = { Text(text = label) },
                singleLine = true,
                maxLines = 1,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                if (options.isNotEmpty()) {
                    options.forEach { parkingSpot ->
                        DropdownMenuItem(
                            onClick = {
                                selectedText = parkingSpot.address
                                onTextSelected(parkingSpot.uuid)
                                expanded = false
                            },
                            text = { Text(text = parkingSpot.address) }
                        )
                    }
                } else {
                    DropdownMenuItem(
                        text = { Text("You have not registered any parking spots yet.") },
                        onClick = { },
                        enabled = false
                    )
                }
            }
        }
    }
}