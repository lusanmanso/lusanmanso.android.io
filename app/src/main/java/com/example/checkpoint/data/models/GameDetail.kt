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

data class Rating(
    val id: Int,
    val title: String?,
    val count: Int?,
    val percent: Double?
)

data class PlatformInfo(
    val platform: PlatformDetail?,
    @SerializedName("released_at") val releasedAt: String?,
    val requirements: Requirements?
)

data class PlatformDetail(
    val id: Int,
    val slug: String?,
    val name: String?
    // ... otros campos de Platform si los da la API de detalle ...
)

data class Requirements(
    val minimum: String?,
    val recommended: String?
)

data class StoreInfo(
    val store: StoreDetail?
    // ... otros campos ...
)

data class StoreDetail(
    val id: Int,
    val slug: String?,
    val name: String?
    // ...
)

data class Developer(
    val id: Int,
    val name: String?,
    val slug: String?
    // ...
)