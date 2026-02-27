# File Uploads

This project uses a **two-step file upload flow** for book cover art:

1. Upload the binary with REST (`multipart/form-data`)
2. Attach the uploaded file to a book with GraphQL mutation

This keeps GraphQL focused on metadata/state changes and keeps binary transport concerns in REST.

## Why This Pattern

- GraphQL mutations remain small and reliable
- Binary uploads can be validated independently (content type, max size)
- Upload and attach can be retried separately
- Keeps storage concerns in infrastructure while domain remains pure

## Flow

1. Client uploads an image to `POST /api/uploads/book-covers`
2. API stores the file on disk and creates a staged upload record (`cover_uploads`)
3. API returns `uploadId`
4. Client calls GraphQL `attachBookCover(bookId, uploadId, description)`
5. API validates upload state (exists, not expired, not consumed), updates the book cover metadata, and marks upload as consumed

## Endpoints

### Upload Cover Binary (REST)

`POST /api/uploads/book-covers`

Content type: `multipart/form-data` with a `file` part.

Example:

```bash
curl -X POST \
  -F "file=@/path/to/cover.png;type=image/png" \
  http://localhost:8080/api/uploads/book-covers
```

Example response:

```json
{
  "uploadId": "6b1c1bff-f1de-4d5d-9138-3e4dc1702f1d",
  "originalFilename": "cover.png",
  "contentType": "image/png",
  "sizeBytes": 34821,
  "expiresAt": "2026-02-28T09:20:00Z"
}
```

### Attach Cover to Book (GraphQL)

Mutation:

```graphql
mutation AttachCover($bookId: BookId!, $uploadId: BookId!, $description: String!) {
  attachBookCover(bookId: $bookId, uploadId: $uploadId, description: $description) {
    id
    title
    coverDescription
    coverContentType
    coverUrl
  }
}
```

Variables:

```json
{
  "bookId": "11111111-1111-1111-1111-111111111111",
  "uploadId": "6b1c1bff-f1de-4d5d-9138-3e4dc1702f1d",
  "description": "Front cover art with original title treatment"
}
```

### Download Cover Binary (REST)

`GET /api/books/{bookId}/cover`

Serves the binary image if the book has a cover attached.

## Validation Rules

- Upload API accepts only `image/*` content types
- Upload size must be between `1` and configured max bytes
- Staged uploads must not be expired when attached
- Staged uploads can only be consumed once
- Cover description is required and max length is 1000 chars

## Storage and Lifecycle

- File binary: local filesystem (default `${java.io.tmpdir}/bookapi/covers`)
- Staged upload metadata: `cover_uploads` table
- Book cover metadata: columns on `books`
  - `cover_storage_path`
  - `cover_content_type`
  - `cover_description`

## Configuration

Configured in `application.yml`:

- `bookapi.upload.cover.directory`
- `bookapi.upload.cover.max-bytes`
- `bookapi.upload.cover.ttl-hours`
- `bookapi.upload.cover.test-ui.enabled` (default `false`)

## Manual Test UI (Feature-Flagged)

For manual human testing, there is a minimal internal page:

- URL: `GET /internal/cover-upload-test`
- Default: disabled

Enable it locally:

```bash
BOOKAPI_UPLOAD_COVER_TEST_UI_ENABLED=true ./gradlew bootRun
```

The page lets you provide:

- Book ID
- Cover description text
- Image file to upload

Then it performs:

1. REST upload (`POST /api/uploads/book-covers`)
2. GraphQL attach (`attachBookCover`)

## Code Locations

- REST controller: `infrastructure/web/BookCoverController.kt`
- GraphQL mutation: `graphql/mutations/BookMutation.kt` (`attachBookCover`)
- Domain types: `domain/types/BookCover.kt`, `domain/types/CoverUpload.kt`
- Ports/adapters:
  - `domain/repositories/CoverUploadRepository.kt`
  - `infrastructure/persistence/JpaCoverUploadRepository.kt`
