package dev.korryr.digitalid.ui.features.qrReader.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import dev.korryr.digitalid.ui.features.dataModel.Student

@Composable
fun StudentDetailsCard(
    student: Student,
    onImageClick: (String) -> Unit,
    onScanAgain: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Student Details",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Student photo with nice frame
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .height(250.dp)
                    //.size(250.dp)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = student.imageUrl),
                    contentDescription = "Student Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onImageClick(student.imageUrl) },
                    contentScale = ContentScale.FillBounds
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student information with styling
            if (student.studentId != "N/A") {
                InfoRow(label = "Student ID", value = student.studentId)
                InfoRow(label = "Name", value = student.fullName)
                InfoRow(label = "Email", value = student.email)
                InfoRow(label = "Department", value = student.department)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scan again button
            Button(
                onClick = onScanAgain,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Scan Another ID", fontWeight = FontWeight.Medium)
            }
        }
    }
}