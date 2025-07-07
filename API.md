Berikut adalah file dokumentasi dalam format **Markdown** (`authentication-user-profile.md`) yang terstruktur dan siap digunakan:

---

````markdown
# Dokumentasi API: Autentikasi & Manajemen Profil Pengguna

**Versi API**: 1.0  
**Base URL**: `https://api.tekor.com/api/v1`

Dokumen ini menyediakan spesifikasi teknis untuk endpoint yang berkaitan dengan otentikasi dan pengelolaan profil pengguna di platform TE-KOR. Semua request dan response menggunakan format **JSON**.

---

# Authentication Endpoints

Endpoint yang berhubungan dengan registrasi, login, dan keamanan akun.

---

## 1.1 Register User

- **Endpoint**: `POST /auth/register`
- **Authorization**: Public
- **Description**: Mendaftarkan pengguna baru ke dalam sistem. Setelah berhasil, sistem akan mengirimkan email verifikasi.

### Request Body

| Field    | Type   | Description                 | Validation Rules               | Required |
| -------- | ------ | --------------------------- | ------------------------------ | -------- |
| fullName | String | Nama lengkap pengguna       | Min 2 chars, letters only      | Yes      |
| username | String | Username unik               | Min 4 chars, start with letter | Yes      |
| email    | String | Alamat email unik dan valid | Must be valid email format     | Yes      |
| password | String | Password pengguna           | Min 8 characters               | Yes      |

### Contoh Request

```json
{
  "fullName": "John Doe",
  "username": "johndoe123",
  "email": "john.doe@example.com",
  "password": "SecurePass123!"
}
```

### Success Response `201 Created`

```json
{
  "status": "CREATED",
  "message": "Registration successful. Please check your email for verification.",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "username": "johndoe123",
    "email": "john.doe@example.com",
    "verified": false
  }
}
```

### Error Responses

- `400 Bad Request`: Validasi gagal (format email salah, password terlalu pendek, dll)
- `409 Conflict`: Email atau username sudah terdaftar

---

## 1.2 User Login

- **Endpoint**: `POST /auth/login`
- **Authorization**: Public
- **Description**: Memvalidasi kredensial pengguna dan mengembalikan JWT tokens.

### Request Body

| Field    | Type   | Description       | Required |
| -------- | ------ | ----------------- | -------- |
| username | String | Username pengguna | Yes      |
| password | String | Password pengguna | Yes      |

### Contoh Request

```json
{
  "username": "johndoe123",
  "password": "SecurePass123!"
}
```

### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Login successful.",
  "data": {
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "username": "johndoe123",
      "email": "john.doe@example.com"
    },
    "token": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  }
}
```

### Error Responses

- `400 Bad Request`: Field tidak lengkap
- `401 Unauthorized`: Kredensial tidak valid
- `403 Forbidden`: Akun belum terverifikasi

---

## 1.3 Email Verification

- **Endpoint**: `GET /auth/verify`
- **Authorization**: Public
- **Description**: Verifikasi email user menggunakan token yang dikirim via email.

### Query Parameters

| Parameter | Type   | Description            | Required |
| --------- | ------ | ---------------------- | -------- |
| token     | String | Token verifikasi email | Yes      |

### Success Response `302 Found`

Redirect ke halaman frontend:

- `?status=verified` jika sukses
- `?status=invalid` jika token tidak valid

---

## 1.4 Forgot Password

- **Endpoint**: `POST /auth/forgot-password`
- **Authorization**: Public
- **Description**: Meminta link reset password dikirim ke email.

### Request Body

| Field | Type   | Description     | Required |
| ----- | ------ | --------------- | -------- |
| email | String | Email terdaftar | Yes      |

### Contoh Request

```json
{
  "email": "john.doe@example.com"
}
```

### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "If the email is registered, a password reset link has been sent."
}
```

---

## 1.5 Reset Password

- **Endpoint**: `POST /auth/reset-password`
- **Authorization**: Public
- **Description**: Reset password menggunakan token yang valid.

### Request Body

| Field           | Type   | Description                     | Required |
| --------------- | ------ | ------------------------------- | -------- |
| token           | String | Token dari email reset password | Yes      |
| newPassword     | String | Password baru                   | Yes      |
| confirmPassword | String | Konfirmasi password baru        | Yes      |

### Contoh Request

```json
{
  "token": "reset-token-123",
  "newPassword": "NewSecurePass456!",
  "confirmPassword": "NewSecurePass456!"
}
```

### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Your password has been successfully reset. Please log in."
}
```

### Error Responses

- `400 Bad Request`: Token tidak valid atau password tidak cocok
- `410 Gone`: Token sudah kadaluarsa

---

## 2. User Profile Endpoints

### 2.1 Get User Profile

**Endpoint**: `GET /api/v1/users`

**Authorization**: Bearer Token (USER or ADMIN)

**Description**: Mendapatkan profil user yang sedang login

#### Headers

| Header        | Value            | Required |
| ------------- | ---------------- | -------- |
| Authorization | Bearer `<token>` | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Successfully get profile",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe",
    "username": "johndoe123",
    "email": "john.doe@example.com",
    "imageUrl": "https://example.com/avatar.jpg",
    "isVerified": true,
    "createdAt": "2023-07-20T15:30:45"
  }
}
```

#### Error Responses

- `401 Unauthorized`: Token tidak valid/tidak ada
- `403 Forbidden`: Role tidak memenuhi syarat
- `404 Not Found`: User tidak ditemukan

---

### 2.2 Update User Profile

**Endpoint**: `PATCH /api/v1/users`

**Authorization**: Bearer Token (USER only)

**Description**: Memperbarui profil user

#### Request Body

| Field    | Type   | Description       | Validation Rules       | Required |
| -------- | ------ | ----------------- | ---------------------- | -------- |
| fullName | String | Nama lengkap baru | Min 2 chars, not blank | Yes      |

#### Contoh Request

```json
{
  "fullName": "John Doe Updated"
}
```

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Profile updated successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "John Doe Updated",
    "username": "johndoe123",
    "email": "john.doe@example.com",
    "imageUrl": "https://example.com/avatar.jpg",
    "isVerified": true
  }
}
```

#### Error Responses

- `400 Bad Request`: Validasi gagal
- `401 Unauthorized`: Tidak terautentikasi
- `403 Forbidden`: Bukan role USER

---

### 2.3 Update Profile Picture

**Endpoint**: `POST /api/v1/users/avatar`

**Authorization**: Bearer Token (USER or ADMIN)

**Content-Type**: multipart/form-data

**Description**: Mengunggah foto profil baru

#### Parameters

| Parameter | Type          | Description | Required |
| --------- | ------------- | ----------- | -------- |
| avatar    | MultipartFile | File gambar | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Profile picture updated successfully",
  "data": {
    "imageUrl": "https://storage.com/new-avatar.jpg"
  }
}
```

#### Error Responses

- `400 Bad Request`: File tidak valid
- `413 Payload Too Large`: File terlalu besar (>2MB)
- `415 Unsupported Media Type`: Format file tidak didukung (hanya JPEG/PNG)

---

### 2.4 Change Password

**Endpoint**: `POST /api/v1/users/change-password`

**Authorization**: Bearer Token (USER only)

**Description**: Mengubah password user yang sedang login

#### Request Body

| Field           | Type   | Description              | Validation Rules       | Required |
| --------------- | ------ | ------------------------ | ---------------------- | -------- |
| oldPassword     | String | Password saat ini        | -                      | Yes      |
| newPassword     | String | Password baru            | Min 8 characters       | Yes      |
| confirmPassword | String | Konfirmasi password baru | Must match newPassword | Yes      |

#### Contoh Request

```json
{
  "oldPassword": "oldPassword123!",
  "newPassword": "newSecurePass456!",
  "confirmPassword": "newSecurePass456!"
}
```

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Password updated successfully"
}
```

#### Error Responses

- `400 Bad Request`: Validasi gagal/password tidak cocok
- `401 Unauthorized`: Password lama salah
- `403 Forbidden`: Tidak terautentikasi

---

### 2.5 Get All Users (Admin Only)

**Endpoint**: `GET /api/v1/users/all`

**Authorization**: Bearer Token (ADMIN only)

**Description**: Mendapatkan daftar semua user

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Successfully retrieved all users",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "fullName": "John Doe",
      "username": "johndoe123",
      "email": "john.doe@example.com",
      "isVerified": true
    },
    {
      "id": "110e8400-e29b-41d4-a716-446655440000",
      "fullName": "Jane Smith",
      "username": "janesmith",
      "email": "jane.smith@example.com",
      "isVerified": false
    }
  ]
}
```

#### Error Responses

- `401 Unauthorized`: Tidak terautentikasi
- `403 Forbidden`: Bukan role ADMIN

---

## 3. Transaction Endpoints

### 3.1 Create Transaction

**Endpoint**: `POST /api/v1/transactions/create`

**Authorization**: Bearer Token (USER or ADMIN)

**Description**: Membuat transaksi baru dan mengembalikan data untuk pembayaran

#### Request Body

| Field         | Type | Description              | Required |
| ------------- | ---- | ------------------------ | -------- |
| testPackageId | UUID | ID paket tes yang dibeli | No\*     |
| bundleId      | UUID | ID bundle yang dibeli    | No\*     |

> \*Note: Harus mengisi salah satu (testPackageId atau bundleId)

#### Contoh Request

```json
{
  "testPackageId": "550e8400-e29b-41d4-a716-446655440000",
  "bundleId": "110e8400-e29b-41d4-a716-446655440000"
}
```

#### Success Response `201 Created`

```json
{
  "status": "CREATED",
  "message": "Transaction created successfully, please proceed to payment.",
  "data": {
    "orderId": "TRX-123456789",
    "redirectUrl": "https://app.midtrans.com/snap/v2/vtweb/...",
    "transactionStatus": "pending",
    "amount": 250000.0,
    "packageName": "Peckage A",
    "bundleName": "Bundle A",
    "createdAt": "2023-07-20T15:30:45"
  }
}
```

#### Error Responses

- `400 Bad Request`: Tidak ada ID paket/bundle yang valid
- `401 Unauthorized`: Tidak terautentikasi
- `404 Not Found`: Paket/bundle tidak ditemukan

---

### 3.2 Midtrans Webhook Notification

**Endpoint**: `POST /api/v1/transactions/midtrans/webhook`

**Authorization**: Midtrans Server

**Description**: Endpoint untuk menerima notifikasi pembayaran dari Midtrans

#### Request Body

Payload dari Midtrans (format bervariasi tergantung status)

#### Success Response `200 OK`

Kosong (empty body)

---

### 3.3 Check Transaction Status

**Endpoint**: `GET /api/v1/transactions/status/{orderId}`

**Authorization**: Public

**Description**: Memeriksa status transaksi berdasarkan order ID

#### Path Parameters

| Parameter | Type   | Description        | Required |
| --------- | ------ | ------------------ | -------- |
| orderId   | String | ID order transaksi | Yes      |

#### Success Response `200 OK`

```json
{
  "orderId": "TRX-123456789",
  "redirectUrl": null,
  "transactionStatus": "settlement",
  "amount": 250000.0,
  "packageName": "Peckage A",
  "bundleName": "Bundle A",
  "createdAt": "2023-07-20T15:30:45"
}
```

#### Error Responses

- `404 Not Found`: Transaksi tidak ditemukan

---

### 3.4 Get Transaction History

**Endpoint**: `GET /api/v1/transactions/history`

**Authorization**: Bearer Token (USER or ADMIN)

**Description**: Mendapatkan riwayat transaksi user yang sedang login

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Transaction history retrieved successfully.",
  "data": [
    {
      "orderId": "TRX-123456789",
      "redirectUrl": null,
      "transactionStatus": "settlement",
      "amount": 250000.0,
      "packageName": "Peckage A",
      "bundleName": "Bundle A",
      "createdAt": "2023-07-20T15:30:45"
    },
    {
      "orderId": "TRX-987654321",
      "redirectUrl": null,
      "transactionStatus": "pending",
      "amount": 150000.0,
      "packageName": "Peckage A",
      "bundleName": null,
      "createdAt": "2023-07-21T10:15:30"
    }
  ]
}
```

#### Error Responses

- `401 Unauthorized`: Tidak terautentikasi

---

### 3.5 Get Transactions by User ID

**Endpoint**: `GET /api/v1/transactions/{userId}`

**Authorization**: Public\*

**Description**: Mendapatkan detail transaksi berdasarkan user ID

> \*Note: Sebaiknya ditambahkan authorization untuk keamanan

#### Path Parameters

| Parameter | Type   | Description | Required |
| --------- | ------ | ----------- | -------- |
| userId    | String | ID user     | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "200",
  "message": "Successfully get transactions by user ID",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "midtransOrderId": "TRX-123456789",
      "amount": 250000.0,
      "status": "settlement",
      "createdAt": "2023-07-20T15:30:45",
      "testPackage": {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Peckage A",
        "description": "Peckage A."
      },
      "bundle": {
        "id": "770e8400-e29b-41d4-a716-446655440000",
        "name": "Bundle A",
        "description": "BundleA"
      }
    }
  ]
}
```

#### Error Responses

- `404 Not Found`: User tidak memiliki transaksi

---

## 4. Bundle Endpoints

### 4.1 Create Bundle (Admin Only)

**Endpoint**: `POST /api/v1/bundles`

**Authorization**: Bearer Token (ADMIN only)

**Description**: Membuat bundle baru (hanya untuk admin)

#### Request Body

| Field       | Type       | Description                  | Validation Rules  | Required |
| ----------- | ---------- | ---------------------------- | ----------------- | -------- |
| name        | String     | Nama bundle                  | Min 3 characters  | Yes      |
| description | String     | Deskripsi bundle             | Min 10 characters | Yes      |
| price       | BigDecimal | Harga bundle                 | Min 0.01          | Yes      |
| packageIds  | List<UUID> | Daftar ID paket dalam bundle | Min 1 item        | Yes      |

#### Contoh Request

```json
{
  "name": "Bundle B",
  "description": "Bundle 3 in 1",
  "price": 500000.0,
  "packageIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "660e8400-e29b-41d4-a716-446655440000"
  ]
}
```

#### Success Response `201 Created`

```json
{
  "status": "CREATED",
  "message": "Successfully created bundle",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Bundle B",
    "description": "Bundle B 3 in 1",
    "imageUrl": "https://example.com/bundle-health.jpg",
    "price": 500000.0,
    "packages": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Peckage a",
        "description": "Peckage A"
      },
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Peckage B",
        "description": "Peckage B"
      }
    ]
  }
}
```

#### Error Responses

- `400 Bad Request`: Validasi gagal
- `401 Unauthorized`: Tidak terautentikasi
- `403 Forbidden`: Bukan role ADMIN
- `404 Not Found`: Salah satu paket tidak ditemukan

---

### 4.2 Get All Bundles

**Endpoint**: `GET /api/v1/bundles`

**Authorization**: Public

**Description**: Mendapatkan daftar semua bundle yang tersedia

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Successfully get all bundles",
  "data": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "name": "Bundle B",
      "description": "Bundle B 3 in 1.",
      "imageUrl": "https://example.com/bundle-health.jpg",
      "price": 500000.0,
      "packages": [
        {
          "id": "550e8400-e29b-41d4-a716-446655440000",
          "name": "Peckage A"
        }
      ]
    },
    {
      "id": "880e8400-e29b-41d4-a716-446655440000",
      "name": "Bundle C",
      "description": "Bundle C",
      "imageUrl": "https://example.com/bundle-diabetes.jpg",
      "price": 350000.0,
      "packages": [
        {
          "id": "990e8400-e29b-41d4-a716-446655440000",
          "name": "Peckage C"
        }
      ]
    }
  ]
}
```

---

### 4.3 Get Bundle by ID

**Endpoint**: `GET /api/v1/bundles/{id}`

**Authorization**: Public

**Description**: Mendapatkan detail bundle berdasarkan ID

#### Path Parameters

| Parameter | Type | Description | Required |
| --------- | ---- | ----------- | -------- |
| id        | UUID | ID bundle   | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Successfully get bundle by id",
  "data": {
    "id": "770e8400-e29b-41d4-a716-446655440000",
    "name": "Bundle B",
    "description": "Bundle B 3 n 1",
    "imageUrl": "https://example.com/bundle-health.jpg",
    "price": 500000.0,
    "packages": [
      {
        "id": "550e8400-e29b-41d4-a716-446655440000",
        "name": "Peckage A",
        "description": "Pecakage A",
        "price": 250000.0
      },
      {
        "id": "660e8400-e29b-41d4-a716-446655440000",
        "name": "Peckage B",
        "description": "Peckage B",
        "price": 150000.0
      }
    ]
  }
}
```

#### Error Responses

- `404 Not Found`: Bundle tidak ditemukan

---

## 5. Test Package Endpoints

### 5.1 Create Test Package from Excel (Admin Only)

**Endpoint**: `POST /api/v1/test-packages`

**Authorization**: Bearer Token (ADMIN only)

**Content-Type**: `multipart/form-data`

**Description**: Membuat paket tes baru dari file Excel beserta soal-soalnya

#### Request Body (Form Data)

| Field         | Type          | Description                    | Required |
| ------------- | ------------- | ------------------------------ | -------- |
| name          | String        | Nama paket (contoh: "Paket A") | ✓        |
| description   | String        | Deskripsi paket                | ✓        |
| imageUrl      | String        | URL gambar paket               | ✗        |
| price         | BigDecimal    | Harga normal                   | ✓        |
| discountPrice | BigDecimal    | Harga diskon                   | ✗        |
| file          | MultipartFile | File Excel (.xlsx)             | ✓        |

#### Success Response `201 Created`

```json
{
  "status": "CREATED",
  "message": "Peckage A berhasil dibuat dengan 40 soal",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Peckage A"
  }
}
```

---

### 5.2 Update Test Package (Admin Only)

**Endpoint**: `PUT /api/v1/test-packages/{id}`

**Authorization**: Bearer Token (ADMIN only)

**Description**: Memperbarui informasi paket tes

#### Path Parameters

| Parameter | Type   | Description     | Required |
| --------- | ------ | --------------- | -------- |
| id        | String | ID test package | ✓        |

#### Request Body

```json
{
  "name": "Pecakge A Updated",
  "price": 275000.0
}
```

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Peckage A berhasil diperbarui",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Peckage A Updated",
    "price": 275000.0
  }
}
```

---

### 5.3 Get Test Package by ID

**Endpoint**: `GET /api/v1/test-packages/{id}`

**Authorization**: Public

**Description**: Mendapatkan detail paket tes

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Detail peckage A",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Peckage A",
    "description": "Peckage A",
    "price": 250000.0,
    "totalQuestions": 40
  }
}
```

---

### 5.4 Delete Test Package (Admin Only)

**Endpoint**: `DELETE /api/v1/test-packages/{id}`

**Authorization**: Bearer Token (ADMIN only)

**Description**: Menghapus paket tes

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Peckage A berhasil dihapus",
  "data": null
}
```

---

### 5.5 Get All Packages and Bundles

**Endpoint**: `GET /api/v1/test-packages`

**Authorization**: Public

**Description**: Mendapatkan semua paket dan bundle

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Daftar PPeckage & Bundle",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Peckage A",
      "type": "package",
      "price": 250000.0
    },
    {
      "id": "770e8400-e29b-41d4-a716-446655440000",
      "name": "Bundle A",
      "type": "bundle",
      "price": 500000.0
    }
  ]
}
```

---

### 5.6 Get All Test Packages

**Endpoint**: `GET /api/v1/test-packages/all`

**Authorization**: Public

**Description**: Mendapatkan semua paket tes saja

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Daftar Test Peckage",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Peckage A",
      "price": 250000.0,
      "totalQuestions": 40
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "name": "Peckage B",
      "price": 350000.0,
      "totalQuestions": 40
    }
  ]
}
```

---

### Error Responses (Umum untuk semua endpoint)

- `400 Bad Request`: Data tidak valid
- `401 Unauthorized`: Tidak terautentikasi
- `403 Forbidden`: Tidak memiliki akses
- `404 Not Found`: Data tidak ditemukan
- `415 Unsupported Media Type`: Format file tidak didukung

---

## 6. Vocabulary Endpoints

### 6.1 Upload Vocabulary Data from Excel

**Endpoint**: `POST /api/v1/vocabularies/upload`

**Authorization**: Required

**Content-Type**: multipart/form-data

**Description**: Mengupload data vocabulary dari file Excel

#### Request Body (Form Data)

| Field | Type          | Description        | Required |
| ----- | ------------- | ------------------ | -------- |
| file  | MultipartFile | File Excel (.xlsx) | Yes      |

**Format Excel:**

- Kolom 1: Kata dalam bahasa Korea
- Kolom 2: Terjemahan
- Kolom 3: Romanisasi
- Kolom 4: Kategori (harus sesuai enum yang ada)

#### Success Response `201 Created`

```json
{
  "status": "CREATED",
  "message": "Vocabulary data uploaded successfully",
  "data": {
    "fileName": "korean_vocab.xlsx",
    "uploadedCount": 50,
    "categories": ["FOOD", "DAILY"]
  }
}
```

#### Error Responses

- `400 Bad Request`: Format file tidak valid
- `415 Unsupported Media Type`: Bukan file Excel
- `500 Internal Server Error`: Gagal memproses file

---

### 6.2 Get All Categories

**Endpoint**: `GET /api/v1/vocabularies/categories`

**Authorization**: Public

**Description**: Mendapatkan daftar semua kategori vocabulary

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Categories retrieved successfully",
  "data": ["FOOD", "NOUN", "ADJECTIVE"]
}
```

---

### 6.3 Get Vocabularies by Category

**Endpoint**: `GET /api/v1/vocabularies?category={category}`

**Authorization**: Public

**Description**: Mendapatkan vocabulary berdasarkan kategori

#### Query Parameters

| Parameter | Type   | Description                      | Required |
| --------- | ------ | -------------------------------- | -------- |
| category  | String | Nama kategori (case insensitive) | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Vocabularies retrieved successfully for category: FOOD",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "koreanWord": "사과",
      "translation": "apel",
      "romanization": "sagwa",
      "category": "FOOD"
    },
    {
      "id": "660e8400-e29b-41d4-a716-446655440000",
      "koreanWord": "바나나",
      "translation": "pisang",
      "romanization": "banana",
      "category": "FOOD"
    }
  ]
}
```

#### Error Responses

- `400 Bad Request`: Kategori tidak valid
- `404 Not Found`: Tidak ada vocabulary untuk kategori tersebut

---

### 6.4 Delete Vocabulary

**Endpoint**: `DELETE /api/v1/vocabularies/{id}`

**Authorization**: Required

**Description**: Menghapus vocabulary berdasarkan ID

#### Path Parameters

| Parameter | Type | Description   | Required |
| --------- | ---- | ------------- | -------- |
| id        | UUID | ID vocabulary | Yes      |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Vocabulary deleted successfully",
  "data": "Deleted ID: 550e8400-e29b-41d4-a716-446655440000"
}
```

#### Error Responses

- `404 Not Found`: Vocabulary tidak ditemukan

---

## 7. AI Evaluation Endpoint

### 7.1 Get AI Evaluation for Test Attempt

**Endpoint**: `GET /api/v1/ai-evaluations/{testAttemptId}`

**Authorization**: Bearer Token (USER role required)

**Description**: Mendapatkan atau memicu evaluasi AI untuk hasil test attempt

#### Path Parameters

| Parameter     | Type   | Description                     | Required |
| ------------- | ------ | ------------------------------- | -------- |
| testAttemptId | String | ID attempt test yang dievaluasi | ✓        |

#### Success Response `200 OK`

```json
{
  "status": "OK",
  "message": "Successfully retrieved AI evaluation.",
  "data": "### 1. ANALISIS HASIL\n- **Interpretasi skor**: Skor Anda 75/100 menunjukkan...\n\n### 2. IDENTIFIKASI KEKUATAN & KELEMAHAN\n- **Area yang dikuasai**: Listening Comprehension..."
}
```

#### Error Responses

- `401 Unauthorized`: Tidak terautentikasi
- `403 Forbidden`: Bukan role USER
- `404 Not Found`: Test attempt tidak ditemukan
- `500 Internal Server Error`: Gagal memproses evaluasi AI

---

## 8. Test Attempts Endpoints
````
