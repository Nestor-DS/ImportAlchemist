# Import Alchemist

Spring Boot web application that rewrites Java source files to a new package namespace and returns the result as a downloadable ZIP — no IDE required.

---
> [!IMPORTANT]
> Known limitations
>- One public type per file is assumed (standard Java convention). Files with multiple top-level types may not have all class names detected.
>- The engine rewrites `package` declarations, `import` statements, and qualified references in code. It does not rename the files themselves — file renaming is the responsibility of the build tool or IDE after migration.
>- Wildcard imports (`import oldpkg.*;`) are not rewritten. Use explicit imports before running the tool.

## What it does

Upload one or more `.java` files and a target package name. The engine:

1. Reads all files into memory in a single pass.
2. Detects which packages belong to the project automatically — no manual configuration. Namespace roots are derived from the `package` declarations in the uploaded files, so even classes absent from the upload but sharing the same namespace get their imports rewritten correctly.
3. Rewrites every file:
   - `package` declaration → replaced with the new package.
   - `import` statements → project imports redirected to the new package; external and JDK imports untouched.
   - Qualified references in code (e.g. `oldpkg.main.SomeClass`) → rewritten to `newPackage.SomeClass`.
4. Returns all rewritten files as a ZIP archive.

---

## Package detection — how it works

The engine infers what counts as a project package from the uploaded files themselves. Given these two uploads:

```
Aplicacion.java  →  package packageprueba_pkg.main;
Usuario.java     →  package paqueteanterior_pkg.modelo;
```

It derives the namespace roots `packageprueba_pkg` and `paqueteanterior_pkg`. Any import whose package starts with one of those roots is treated as a project import and rewritten — including `import paqueteanterior_pkg.main.AppAnterior` even if `AppAnterior.java` was not uploaded.

Imports from JDK namespaces (`java.*`, `javax.*`, `jakarta.*`, `sun.*`, `com.sun.*`) are never touched.

---

## Endpoint

### `GET /`

Returns the web UI (`index` view).

### `POST /refactor`

**Content-Type:** `multipart/form-data`

| Parameter    | Type                  | Required | Validation                                      |
|--------------|-----------------------|----------|-------------------------------------------------|
| `files`      | `List<MultipartFile>` | yes      | At least one file required                      |
| `newPackage` | `String`              | yes      | Valid Java package name (e.g. `com.example.app`)|

**Response:** `application/octet-stream` — ZIP file named `refactored_files.zip`.

Non-`.java` files in the upload are silently ignored.

**Error responses:**

| Status | Cause                                               |
|--------|-----------------------------------------------------|
| 400    | Missing files, blank `newPackage`, invalid package format |
| 413    | Upload exceeds configured size limit                |
| 500    | I/O failure during processing                       |

All error responses return a JSON body:

```json
{ "error": "description of the problem" }
```

**Example with curl:**

```bash
curl -X POST http://localhost:8080/refactor \
  -F "files=@Aplicacion.java" \
  -F "files=@Usuario.java" \
  -F "newPackage=com.example" \
  --output refactored_files.zip
```
