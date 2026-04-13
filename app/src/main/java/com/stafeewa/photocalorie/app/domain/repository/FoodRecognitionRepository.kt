package com.stafeewa.photocalorie.app.domain.repository

import android.graphics.Bitmap
import com.stafeewa.photocalorie.app.domain.entity.FoodRecognitionCandidate

interface FoodRecognitionRepository {

    suspend fun recognizeFoodCandidates(bitmap: Bitmap): List<FoodRecognitionCandidate>
}
