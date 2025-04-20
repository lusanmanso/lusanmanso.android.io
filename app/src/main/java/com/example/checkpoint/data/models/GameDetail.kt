package com.example.checkpoint.data.models

import com.google.gson.annotations.SerializedName

data class GameDetail(
    val id: Int,
    val slug: String,
    val name: String,
    @SerializedName("name_original") val nameOriginal: String,
    val description: String,
    @SerializedName("description_raw") val descriptionRaw: String,
    val metacritic: Int,
    @SerializedName("released") val released: String?, // Fecha de lanzamiento (YYYY-MM-DD)
    @SerializedName("tba") val tba: Boolean?,
    @SerializedName("background_image") val backgroundImage: String?, // URL Imagen principal
    @SerializedName("background_image_additional") val backgroundImageAdditional: String?,
    val website: String?,
    val rating: Double?,
    @SerializedName("rating_top") val ratingTop: Int?,
//    val ratings: List<Rating>?, // Desglose de ratings
    val playtime: Int?,
    @SerializedName("screenshots_count") val screenshotsCount: Int?,
    @SerializedName("movies_count") val moviesCount: Int?,
    @SerializedName("creators_count") val creatorsCount: Int?,
    @SerializedName("achievements_count") val achievementsCount: Int?,
    @SerializedName("parent_achievements_count") val parentAchievementsCount: Int?,
//    val platforms: List<PlatformInfo>?, // Información detallada de plataformas
//    val stores: List<StoreInfo>?,
//    val developers: List<Developer>?,
//    val genres: List<Genre>?,
//    val tags: List<Tag>?,
//    val publishers: List<Publisher>?,
)