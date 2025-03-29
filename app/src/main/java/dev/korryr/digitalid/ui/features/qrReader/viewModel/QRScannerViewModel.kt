package dev.korryr.digitalid.ui.features.qrReader.viewModel

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.korryr.digitalid.ui.features.dataModel.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.korryr.digitalid.ui.features.dataModel.Student
import dev.korryr.digitalid.ui.features.qrReader.domain.useCase.GetStudentDetailsUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val getStudentDetailsUseCase: GetStudentDetailsUseCase
) : ViewModel() {

    private val _studentState = mutableStateOf<Result<Student>>(Result.Loading)
    val studentState: State<Result<Student>> = _studentState

    /**
     * Processes the raw QR code string.
     * - If it starts with "http", it is treated as an image URL.
     * - Otherwise, it is expected to be in the format "studentId|qr_code_data"
     */
    fun scanQRCode(qrRaw: String) {
        viewModelScope.launch {
            if (qrRaw.startsWith("http", ignoreCase = true)) {
                // Directly use the scanned data as an image URL.
                val student = Student(
                    studentId = "N/A", // default or placeholder value
                    fullName = "N/A",  // default or placeholder value
                    email = "N/A",     // default or placeholder value
                    department = "N/A",// default or placeholder value
                    imageUrl = qrRaw,
                    qrCodeData = ""
                )
                _studentState.value = Result.Success(student)
                Log.d("QRScannerViewModel", "Scanned image URL: $qrRaw")
            } else {
                // Assume the QR code encodes "studentId|qr_code_data"
                val parts = qrRaw.split("|")
                if (parts.size >= 2) {
                    val studentId = parts[0]
                    val qrCodeData = parts[1]
                    Log.d("QRScannerViewModel", "Fetching student details for ID: $studentId")
                    getStudentDetailsUseCase(studentId, qrCodeData)
                        .onEach { result ->
                            Log.d("QRScannerViewModel", "Received result: $result")
                            _studentState.value = result
                        }
                        .catch { e ->
                            Log.e("QRScannerViewModel", "Error fetching student details", e)
                            _studentState.value = Result.Error(e.localizedMessage ?: "Unknown error")
                        }
                        .launchIn(this)
                } else {
                    _studentState.value = Result.Error("Invalid QR Code format")
                    Log.e("QRScannerViewModel", "Invalid QR code format: $qrRaw")
                }
            }
        }
    }

    fun resetScan() {
        _studentState.value = Result.Loading
    }
}