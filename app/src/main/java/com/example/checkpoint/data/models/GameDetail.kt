package com.example.checkpoint.data.models

import com.google.gson.annotations.SerializedName

public data class GameDetail(
    val id: Int,
    val slug: String,
    val name: String,
    @SerializedName("name_original") val nameOriginal: String,
    val description: String,
    @SerializedName("description_raw") val descriptionRaw: String,
    val metacritic: Int?,
    @SerializedName("released") val released: String?,
    @SerializedName("tba") val tba: Boolean?,
    @SerializedName("background_image") val backgroundImage: String?,
    @SerializedName("background_image_additional") val backgroundImageAdditional: String?,
    val website: String?,
    val rating: Double?,
    @SerializedName("rating_top") val ratingTop: Int?,
    val ratings: List<Rating>?,
    val playtime: Int?,
    @SerializedName("screenshots_count") val screenshotsCount: Int?,
    @SerializedName("movies_count") val moviesCount: Int?,
    @SerializedName("creators_count") val creatorsCount: Int?,
    @SerializedName("achievements_count") val achievementsCount: Int?,
    @SerializedName("parent_achievements_count") val parentAchievementsCount: Int?,
    val platforms: List<PlatformInfo>?,
    val stores: List<StoreInfo>?,
    val developers: List<Developer>?,
    val genres: List<Genre>?,
    val tags: List<Tag>?,
    val publishers: List<Publisher>?,
)

public data class Rating(
    val id: Int,
    val title: String?,
    val count: Int?,
    val percent: Double?
)

public data class PlatformInfo(
    val platform: PlatformDetail?,
    @SerializedName("released_at") val releasedAt: String?,
    val requirements: Requirements?
)

public data class PlatformDetail(
    val id: Int,
    val slug: String?,
    val name: String?
)

public data class Requirements(
    val minimum: String?,
    val recommended: String?
)

public data class StoreInfo(
    val store: StoreDetail?
)

public data class StoreDetail(
    val id: Int,
    val slug: String?,
    val name: String?
)

public data class Developer(
    val id: Int,
    val name: String?,
    val slug: String?
)

public data class Genre(
    val id: Int,
    val name: String?,
    val slug: String?
)

public data class Tag(
    val id: Int,
    val name: String?,
    val slug: String?,
    val language: String?,
    @SerializedName("games_count") val gamesCount: Int?,
    @SerializedName("image_background") val imageBackground: String?
)

public data class Publisher(
    val id: Int,
    val name: String?,
    val slug: String?
)