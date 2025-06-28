Berikut adalah file dokumentasi dalam format **Markdown** (`authentication-user-profile.md`) yang terstruktur dan siap digunakan:

---

````markdown
# Dokumentasi API: Autentikasi & Manajemen Profil Pengguna

**Versi API**: 1.0  
**Base URL**: `https://api.tekor.com/api/v1`  

Dokumen ini menyediakan spesifikasi teknis untuk endpoint yang berkaitan dengan otentikasi dan pengelolaan profil pengguna di platform TE-KOR. Semua request dan response menggunakan format **JSON**.

---

## 1. Authentication Endpoints

Endpoint yang berhubungan dengan registrasi, login, dan keamanan akun.

### 1.1 Register User

**Endpoint**: `POST /auth/register`  
**Authorization**: Public  
**Description**: Mendaftarkan pengguna baru ke dalam sistem. Setelah berhasil, sistem akan mengirimkan email verifikasi.

#### Request Body

| Field     | Type   | Description                  | Required |
|-----------|--------|------------------------------|----------|
| fullName  | String | Nama lengkap pengguna.       | Yes      |
| username  | String | Username unik.               | Yes      |
| email     | String | Alamat email unik dan valid. | Yes      |
| password  | String | Password pengguna (min. 8 karakter). | Yes |

#### Contoh Request

```json
{
  "fullName": "Calon PMI Sukses",
  "username": "suksespmi25",
  "email": "calon.pmi@example.com",
  "password": "PasswordSuperKuat123"
}
````

#### Success Response `201 Created`

```json
{
  "status": "success",
  "message": "Registration successful. Please check your email for verification.",
  "data": {
    "id": 124,
    "fullName": "Calon PMI Sukses",
    "username": "suksespmi25",
    "email": "calon.pmi@example.com",
    "isVerified": false
  }
}
```

---

### 1.2 User Login

**Endpoint**: `POST /auth/login`
**Authorization**: Public
**Description**: Memvalidasi kredensial pengguna dan mengembalikan JWT (Access & Refresh Token) jika berhasil.

#### Request Body

| Field      | Type   | Description                                 | Required |
| ---------- | ------ | ------------------------------------------- | -------- |
| identifier | String | Username atau email pengguna yang terdaftar | Yes      |
| password   | String | Password pengguna                           | Yes      |

#### Contoh Request

```json
{
  "username": "suksespmi25",
  "password": "PasswordSuperKuat123"
}
```

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Login successful.",
  "data": {
    "user": {
      "id": 124,
      "fullName": "Calon PMI Sukses",
      "role": "USER"
    },
    "token": {
      "accessToken": "jwt.access.token.string.short-lived",
      "refreshToken": "jwt.refresh.token.string.long-lived"
    }
  }
}
```

#### Error Responses

* `400 Bad Request`: Field `username` atau `password` kosong.
* `401 Unauthorized`: Kredensial tidak valid.

---

### 1.3 Change Password

**Endpoint**: `POST /auth/change-password`
**Authorization**: Bearer Token (User)
**Description**: Memungkinkan pengguna untuk mengubah password mereka. Memerlukan password saat ini untuk verifikasi keamanan.

#### Request Body

| Field           | Type   | Description                                              | Required |
| --------------- | ------ | -------------------------------------------------------- | -------- |
| currentPassword | String | Password yang sedang digunakan saat ini                  | Yes      |
| newPassword     | String | Password baru (harus memenuhi kriteria keamanan)         | Yes      |
| confirmPassword | String | Konfirmasi password baru (harus sama dengan newPassword) | Yes      |

#### Contoh Request

```json
{
  "currentPassword": "PasswordSuperKuat123",
  "newPassword": "PasswordSuperBaru456!",
  "confirmPassword": "PasswordSuperBaru456!"
}
```

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Password updated successfully."
}
```

#### Error Responses

* `400 Bad Request`: `newPassword` tidak cocok atau tidak memenuhi standar.
* `401 Unauthorized`: Pengguna belum login.
* `403 Forbidden`: `currentPassword` yang dimasukkan salah.

---

## 2. User Profile Management Endpoints

Endpoint untuk mengelola data profil pengguna yang sedang login.

### 2.1 Get My Profile

**Endpoint**: `GET /users/me`
**Authorization**: Bearer Token (User)
**Description**: Mengembalikan informasi detail dari pengguna yang terotentikasi.

#### Request

Tidak memerlukan body.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "User profile fetched successfully.",
  "data": {
    "id": 124,
    "fullName": "Calon PMI Sukses",
    "username": "suksespmi25",
    "email": "calon.pmi@example.com",
    "imageUrl": "https://path/to/image.jpg",
    "isVerified": true,
    "createdAt": "2025-06-27T10:00:00Z"
  }
}
```

#### Error Responses

* `401 Unauthorized`: Pengguna belum login.

---

### 2.2 Update Profile Data

**Endpoint**: `PATCH /users/me`
**Authorization**: Bearer Token (User)
**Description**: Memperbarui sebagian data profil pengguna. Hanya kirim field yang ingin diubah.

#### Request Body

| Field    | Type   | Description             | Required |
| -------- | ------ | ----------------------- | -------- |
| fullName | String | Nama lengkap baru       | No       |
| username | String | Username baru yang unik | No       |

#### Contoh Request

```json
{
  "fullName": "Budi Santoso C.P."
}
```

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "User profile updated successfully.",
  "data": {
    "id": 124,
    "fullName": "Budi Santoso C.P.",
    "username": "suksespmi25",
    "email": "calon.pmi@example.com",
    "imageUrl": "https://path/to/image.jpg",
    "isVerified": true
  }
}
```

#### Error Responses

* `400 Bad Request`: Input tidak valid (misal: username sudah digunakan).
* `401 Unauthorized`: Pengguna belum login.

---

### 2.3 Update Profile Picture

**Endpoint**: `POST /users/me/avatar`
**Authorization**: Bearer Token (User)
**Description**: Endpoint ini menggunakan `multipart/form-data` untuk menerima file gambar.

#### Request Body (Content-Type: multipart/form-data)

| Field  | Type | Description                               | Required |
| ------ | ---- | ----------------------------------------- | -------- |
| avatar | File | File gambar (jpg, png) yang akan diunggah | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Profile picture updated successfully.",
  "data": {
    "imageUrl": "https://storage.provider.com/new-avatar-124.jpg"
  }
}
```

#### Error Responses

* `400 Bad Request`: Tidak ada file yang diunggah, format file salah, atau ukuran file terlalu besar.
* `401 Unauthorized`: Pengguna belum login.

```

---
```
