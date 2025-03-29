package dev.korryr.digitalid.ui.features.qrReader.repo

import android.util.Log
import dev.korryr.digitalid.ui.features.dataModel.Result
import dev.korryr.digitalid.ui.features.dataModel.Student
import dev.korryr.digitalid.ui.features.qrReader.domain.StudentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentRepository @Inject constructor(
    private val apiService: StudentApiService
) {
    fun getStudentDetails(studentId: String, qrCodeData: String): Flow<Result<Student>> = flow {
        emit(Result.Loading)
        Log.d("StudentRepository", "Fetching student details for ID: $studentId")
        try {
            val studentDetails = apiService.getStudentSummary(studentId)
            Log.d("StudentRepository", "Received student details: $studentDetails")
            emit(Result.Success(studentDetails))
        } catch (e: Exception) {
            Log.e("StudentRepository", "Error fetching student details", e)
            emit(Result.Error("Failed to fetch student details: ${e.localizedMessage}"))
        }
    }.flowOn(Dispatchers.IO)

}