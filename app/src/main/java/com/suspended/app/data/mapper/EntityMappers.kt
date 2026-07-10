package com.suspended.app.data.mapper

import com.suspended.app.data.local.dao.ArtistInfo
import com.suspended.app.data.local.entity.PlaylistEntity
import com.suspended.app.data.local.entity.TrackEntity
import com.suspended.app.domain.model.Artist
import com.suspended.app.domain.model.Playlist
import com.suspended.app.domain.model.Track

fun TrackEntity.toDomain(): Track {
    return Track(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        albumName = albumName,
        albumId = albumId,
        streamUrl = null,
        localPath = localPath,
        isDownloaded = isDownloaded,
        addedAt = addedAt
    )
}

fun Track.toEntity(
    isInLibrary: Boolean = false,
    lastPlayedAt: Long? = null
): TrackEntity {
    return TrackEntity(
        id = id,
        title = title,
        artist = artist,
        artistId = artistId,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        albumName = albumName,
        albumId = albumId,
        localPath = localPath,
        isDownloaded = isDownloaded,
        isInLibrary = isInLibrary,
        lastPlayedAt = lastPlayedAt,
        addedAt = addedAt
    )
}

fun PlaylistEntity.toDomain(tracks: List<Track> = emptyList()): Playlist {
    return Playlist(
        id = id,
        name = name,
        description = description,
        coverUrl = coverUrl,
        tracks = tracks,
        createdAt = createdAt
    )
}

fun ArtistInfo.toDomain(): Artist {
    return Artist(
        id = artistId ?: artist,
        name = artist,
        thumbnailUrl = null,
        trackCount = trackCount
    )
}
