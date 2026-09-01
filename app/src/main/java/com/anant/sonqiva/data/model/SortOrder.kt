package com.anant.sonqiva.data.model

enum class SongSortOrder(val displayName: String) {
    TITLE_ASC("Title (A-Z)"),
    TITLE_DESC("Title (Z-A)"),
    ARTIST_ASC("Artist (A-Z)"),
    DATE_ADDED_DESC("Recently Added"),
    DATE_ADDED_ASC("Oldest Added"),
    DURATION_DESC("Longest Duration"),
    DURATION_ASC("Shortest Duration")
}

enum class AlbumSortOrder(val displayName: String) {
    TITLE_ASC("Title (A-Z)"),
    ARTIST_ASC("Artist (A-Z)"),
    YEAR_DESC("Year (Newest)"),
    TRACK_COUNT_DESC("Most Songs")
}
