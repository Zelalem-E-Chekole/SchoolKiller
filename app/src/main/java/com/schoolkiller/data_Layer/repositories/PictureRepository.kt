package com.schoolkiller.data_Layer.repositories

import com.schoolkiller.data_Layer.daos.PictureDao
import com.schoolkiller.data_Layer.entities.Picture
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ViewModelScoped
class PictureRepository @Inject constructor(
    private val pictureDao: PictureDao
) {

    val allPictures: Flow<List<Picture>> = pictureDao.getAllPictures()

    suspend fun insertPicture(picture: Picture) {
        pictureDao.insertPicture(picture)
    }

    suspend fun deletePicture(picture: Picture) {
        pictureDao.deletePicture(picture)

    }

}