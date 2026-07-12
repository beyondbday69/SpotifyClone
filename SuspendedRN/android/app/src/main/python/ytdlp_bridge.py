import json
import yt_dlp
import os


def search(query, max_results=15):
    """Search YouTube for tracks. Returns JSON string of results."""
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'extract_flat': 'in_playlist',
        'default_search': f'ytsearch{max_results}',
        'skip_download': True,
    }
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        result = ydl.extract_info(query, download=False)
        entries = result.get('entries', []) if result else []
        tracks = []
        for entry in entries:
            if entry:
                tracks.append({
                    'id': entry.get('id', ''),
                    'title': entry.get('title', 'Unknown'),
                    'uploader': entry.get('uploader', entry.get('channel', 'Unknown')),
                    'channel_id': entry.get('channel_id', ''),
                    'duration': entry.get('duration', 0),
                    'thumbnail': entry.get('thumbnail', entry.get('thumbnails', [{}])[0].get('url', '') if entry.get('thumbnails') else ''),
                    'album': entry.get('album', ''),
                })
        return json.dumps(tracks)


def resolve(video_id):
    """Resolve direct audio stream URL for a video ID."""
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'format': 'bestaudio/best',
        'skip_download': True,
    }
    url = f'https://www.youtube.com/watch?v={video_id}'
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=False)
        return info.get('url', '')


def download(video_id, output_dir):
    """Download audio file. Returns the file path."""
    os.makedirs(output_dir, exist_ok=True)
    ydl_opts = {
        'quiet': True,
        'no_warnings': True,
        'format': 'bestaudio/best',
        'outtmpl': os.path.join(output_dir, '%(id)s.%(ext)s'),
        'postprocessors': [{
            'key': 'FFmpegExtractAudio',
            'preferredcodec': 'opus',
        }],
    }
    url = f'https://www.youtube.com/watch?v={video_id}'
    with yt_dlp.YoutubeDL(ydl_opts) as ydl:
        info = ydl.extract_info(url, download=True)
        ext = info.get('ext', 'opus')
        filepath = os.path.join(output_dir, f"{video_id}.{ext}")
        # Check common extensions if file doesn't exist at expected path
        if not os.path.exists(filepath):
            for e in ['opus', 'webm', 'm4a', 'mp3']:
                candidate = os.path.join(output_dir, f"{video_id}.{e}")
                if os.path.exists(candidate):
                    filepath = candidate
                    break
        return filepath
