"""
FastAPI backend that wraps the existing yt-dlp bridge.
Keep yt-dlp logic exactly the same — just expose it over HTTP.
"""
import os
import json
import shutil
from fastapi import FastAPI, HTTPException
from fastapi.responses import FileResponse, JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Optional

import ytdlp_bridge

app = FastAPI(title="Suspended API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

DOWNLOAD_DIR = os.path.join(os.path.dirname(__file__), "downloads")


class SearchRequest(BaseModel):
    query: str
    max_results: int = 15


class ResolveRequest(BaseModel):
    video_id: str


class DownloadRequest(BaseModel):
    video_id: str


@app.get("/api/health")
async def health():
    return {"status": "ok"}


@app.post("/api/search")
async def search(req: SearchRequest):
    """Search YouTube for tracks."""
    try:
        result_json = ytdlp_bridge.search(req.query, req.max_results)
        tracks = json.loads(result_json)
        # Normalize to frontend-friendly format
        normalized = []
        for t in tracks:
            normalized.append({
                "id": t.get("id", ""),
                "title": t.get("title", "Unknown"),
                "artist": t.get("uploader") or t.get("channel") or "Unknown Artist",
                "artistId": t.get("channel_id", ""),
                "duration": t.get("duration", 0),
                "thumbnailUrl": t.get("thumbnail", ""),
                "albumName": t.get("album", ""),
            })
        return {"tracks": normalized}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/resolve")
async def resolve(req: ResolveRequest):
    """Resolve a direct audio stream URL for a video ID."""
    try:
        url = ytdlp_bridge.resolve(req.video_id)
        if not url:
            raise HTTPException(status_code=404, detail="No stream URL found")
        return {"url": url, "videoId": req.video_id}
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/download")
async def download(req: DownloadRequest):
    """Download audio file for a video ID. Returns the local file path."""
    try:
        filepath = ytdlp_bridge.download(req.video_id, DOWNLOAD_DIR)
        return {"path": filepath, "videoId": req.video_id}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.get("/api/download-file/{video_id}")
async def download_file(video_id: str):
    """Serve a downloaded audio file."""
    # Find the file with any common extension
    for ext in ["opus", "webm", "m4a", "mp3"]:
        filepath = os.path.join(DOWNLOAD_DIR, f"{video_id}.{ext}")
        if os.path.exists(filepath):
            media_type = {
                "opus": "audio/opus",
                "webm": "audio/webm",
                "m4a": "audio/mp4",
                "mp3": "audio/mpeg",
            }.get(ext, "application/octet-stream")
            return FileResponse(filepath, media_type=media_type, filename=f"{video_id}.{ext}")
    raise HTTPException(status_code=404, detail="File not found")


@app.delete("/api/download/{video_id}")
async def delete_download(video_id: str):
    """Delete a downloaded audio file."""
    deleted = False
    for ext in ["opus", "webm", "m4a", "mp3"]:
        filepath = os.path.join(DOWNLOAD_DIR, f"{video_id}.{ext}")
        if os.path.exists(filepath):
            os.remove(filepath)
            deleted = True
    if not deleted:
        raise HTTPException(status_code=404, detail="File not found")
    return {"deleted": True}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
