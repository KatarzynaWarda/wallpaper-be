# wallpaper-be

A backend service for searching and editing wallpapers, built with Kotlin and Ktor.

## Features

- Search for images using the Pixabay API
- Edit images using fal.ai
- JSON response handling
- Image uploads using `multipart/form-data`

## Requirements

- JDK 21
- A Pixabay API key
- A fal.ai API key

## Setup

Set the required API keys:

```powershell
$env:PIXBAY_KEY="your-pixabay-api-key"
$env:FAL_KEY="your-fal-api-key"
```

These are example values, not real API keys.

## Running the application

```powershell
.\gradlew.bat run
```

The server runs at `http://localhost:8080`.

## Endpoints

- `GET /photography?q=nature&image_type=photo`
- `POST /edit-image`