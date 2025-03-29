package dev.korryr.digitalid.ui.features.qrReader.domain

import dev.korryr.digitalid.ui.features.dataModel.Student
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

//interface StudentApiService {
//    @GET("student/details")
//    suspend fun getStudentDetails(
//        @Query("student_id") studentId: String,
//        @Query("qr_code_data") qrCodeData: String
//    ): Student
//}

interface StudentApiService {
    @GET("student-summary/{studentId}")
    suspend fun getStudentSummary(
        @Path("studentId") studentId: String
    ): Student
}