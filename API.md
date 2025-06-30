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
| username   | String | Username pengguna yang terdaftar            | Yes      |
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

**Endpoint**: `POST auth/change-password`
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

---

## 3. Test Package Endpoints

Endpoint untuk mengelola paket-paket soal ujian.

### 3.1. Get Test Packages

**Endpoint**: `GET /test-packages`
**Authorization**: Public
**Description**: Endpoint ini digunakan untuk mengambil daftar semua `test_packages` yang dapat diakses oleh publik

#### Request Body

Tidak memerlukan body.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Test Packages successfully retrieved.",
  "data": [
    {
      "id": 1,
      "name": "Paket Simulasi Reading TOPIK I",
      "description": "Latihan intensif untuk bagian membaca.",
      "price": "50000.00",
      "discountPrice": "49999.00",
      "is_trial": false
    },
    {
      "id": 2,
      "name": "Free Trial Listening Section",
      "description": "Coba gratis 10 soal listening.",
      "price": "0.00",
      "discountPrice": "49999.00",
      "is_trial": true
    }
  ]
}
```

#### Error Responses

* `400 Bad Request`: Tidak ada file yang diunggah, format file salah, atau ukuran file terlalu besar.

### 3.2. Create New Test Package

**Endpoint**: `POST /test-packages`
**Authorization**: Admin
**Content-Type**: multipart/form-data
**Description**: Membuat test_package baru.

#### Request Form

Request (Form-Data)
Endpoint ini menerima data dalam format multipart/form-data, bukan JSON. Request harus terdiri dari beberapa parts:
- name (Text): Nama untuk paket ujian.
- description (Text): Deskripsi paket ujian.
- price (Text): Harga normal dari paket tersebut.
- discount_price (Text, Opsional): Harga setelah diskon. Jika diisi, harga ini yang akan digunakan sebagai harga jual aktif.
- questions_file (File): File dengan format .xlsx yang berisi daftar soal.

#### Success Response `201 CREATED`

```json
{
  "status": "success",
  "message": "Test Package and 50 questions created successfully from CSV.",
  "data": {}
}
```

#### Error Responses

* `400 Bad Request`:
"Required part 'questions_file' is not present." (File tidak diunggah).
"Invalid CSV format: Header is missing or does not match." (Struktur CSV salah).
"Error in CSV on row 15: Invalid question_type 'TESTING'." (Data dalam baris tertentu tidak valid).
* `401 Unauthorized`: Tidak terautentikasi.
* `403 Forbidden`: Pengguna yang login bukan Admin.

### 3.3. Update Test Package

**Endpoint**: `PATCH /test-packages//{packageId}`
**Authorization**: Admin
**Content-Type**: JSON
**Description**: Memperbarui sebagian atau seluruh data test_package.

#### Request Body

```json
{
  "price": "65000.00",
  "discount_price": "3500.00",
  "description": "Deskripsi diperbarui dengan info terbaru."
}
```
#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Test Package updated successfully.",
  "data": {
    "id": 3,
    "name": "Paket Ujian Akhir Pekan",
    "description": "Deskripsi diperbarui dengan info terbaru.",
    "price": "65000.00",
    "discount_price": "35000.00",
    "is_trial": false
  }
}
```

#### Error Response

* `400 Bad Request`: Data yang dikirim tidak valid.
* `401 Unauthorized`: Tidak terautentikasi.
* `403 Forbidden`: Pengguna bukan Admin.
* `404 Not Found`: Jika packageId tidak ditemukan.

### 3.4 Delete Test Package

**Endpoint**: `DELETE /test-packages/{packageId}`
**Authorization**: Admin
**Content-Type**: JSON
**Description**: Menghapus (Soft Delete) test_package berdasarkan ID-nya.

#### Request Body

Tidak memerlukan request body.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Test Package successfully deleted."
}
```

#### Error Response

* `401 Unauthorized`: Tidak terautentikasi.
* `403 Forbidden`: Pengguna bukan Admin.
* `404 Not Found`: Jika packageId tidak ditemukan.

## 4. Bundles Endpoints

Endpoint untuk mengelola bundles.

### 4.1 Get All Bundles

**Endpoint**: `GET /bundles`
**Authorization**: Public
**Content-Type**: JSON
**Description**: Mengambil daftar semua bundles yang ditawarkan.

#### Request Body

Tidak memerlukan body.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Bundles successfully retrieved.",
  "data": [
    {
      "id": 1,
      "name": "Bundle Hemat 3-in-1",
      "description": "Dapatkan 3 paket simulasi dengan harga diskon.",
      "price": "120000.00",
      "discount_price": "90000.00"
    }
  ]
}
```

### 4.2 Get Bundle by ID

**Endpoint**: `GET /bundles/{bundleId}`
**Authorization**: Public
**Content-Type**: JSON
**Description**: Mengambil detail satu bundle spesifik, termasuk daftar paket di dalamnya.

#### Request Body

Tidak memerlukan body.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Bundle successfully retrieved.",
  "data": {
    "id": 1,
    "name": "Bundle Hemat 3-in-1",
    "description": "Dapatkan 3 paket simulasi dengan harga diskon.",
    "price": "120000.00",
    "discount_price": "900000.00",
    "packages": [
      {
        "id": 1,
        "name": "Paket Simulasi Reading TOPIK I"
      },
      {
        "id": 2,
        "name": "Free Trial Listening Section"
      }
    ]
  }
}
```

#### Error Response

`404 Not Found`: Jika bundleId tidak ditemukan.

#### 4.3 Create New Bundle

**Endpoint**: `POST /bundles`
**Authorization**: Admin
**Content-Type**: JSON
**Description**: Membuat bundle baru dan menautkannya dengan beberapa test_packages.

#### Request Body

```json
{
  "name": "Bundle Intensif TOPIK",
  "description": "Semua paket TOPIK dalam satu bundel.",
  "price": "150000.00",
  "discount_price": "90000.00",
  "package_ids": [1, 3]
}
```

#### Response `201 CREATED`

```json
{
  "status": "success",
  "message": "Bundle created successfully.",
  "data": {
    "id": 2,
    "name": "Bundle Intensif TOPIK",
    "description": "Semua paket TOPIK dalam satu bundel.",
    "price": "150000.00",
    "discount_price": "90000.00"
  }
}
```

#### Error Response
* `400 Bad Request`: Data tidak valid atau salah satu package_ids tidak ada.
* `401 Unauthorized`: Tidak terautentikasi.
* `403 Forbidden`: Pengguna bukan Admin.

## 5. Transaction Endpoints

Endpoint untuk mengelola transaksi

### 5.1 Create Transaction

**Endpoint**: POST /api/v1/transactions
**Authorization**: Bearer Token (User)
**Description**: Endpoint ini dipanggil saat pengguna akan memulai proses pembayaran. Server akan membuat catatan transaksi lokal dengan status PENDING dan meminta token pembayaran dari Midtrans.

**Logika Bisnis**:
Harus ada validasi: hanya salah satu dari package_id atau bundle_id yang boleh ada dalam satu request.
Server harus mengambil harga dari tabel test_packages atau bundles (memeriksa discount_price terlebih dahulu) untuk menentukan amount.

#### Request Body

```json
// Opsi 1: Membeli satu paket
{
  "package_id": 1
}

// Opsi 2: Membeli satu bundle
{
  "bundle_id": 1
}
```

#### Success Response `201 CREATED`

Mengembalikan snap_token dari Midtrans yang akan digunakan oleh frontend untuk menampilkan jendela pembayaran.

```json
{
  "status": "success",
  "message": "Transaction created. Please proceed with the payment.",
  "data": {
    "transaction_id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d", // ID dari database Anda
    "midtrans_order_id": "ORDER-1719731400-XYZ", // ID order yang dikirim ke Midtrans
    "snap_token": "abcdef-1234-ghijkl-5678-mnopqr" // Token dari Midtrans untuk frontend
  }
}
```

### 5.2 Handle Notification From Midtrans (Webhook)

**Endpoint**: `POST /transactions/notifications`
**Authorization**: Midtrans Signature Key (Bukan JWT Pengguna)
**Description**: Endpoint ini HANYA untuk dipanggil oleh server Midtrans, bukan oleh pengguna atau aplikasi frontend Anda. Endpoint ini menerima pembaruan status pembayaran (misalnya, berhasil, gagal, kadaluwarsa).

Keamanan (Sangat Penting):
- Endpoint ini tidak boleh dilindungi oleh otentikasi pengguna biasa (JWT).
- Keamanannya dijamin dengan memvalidasi signature_key yang dikirim oleh Midtrans. signature_key adalah hash SHA512 dari order_id + status_code + gross_amount + server_key Anda. Jika hash tidak cocok, abaikan request tersebut.

#### Request Body (Contoh dari Midtrans)

```json
{
  "transaction_time": "2025-06-30 14:10:05",
  "transaction_status": "capture", // atau "settlement"
  "transaction_id": "...",
  "status_message": "...",
  "status_code": "200",
  "signature_key": "...",
  "order_id": "ORDER-1719731400-XYZ",
  "payment_type": "gopay",
  "gross_amount": "59900.00"
}
```

#### Success Response 200 OK
Midtrans hanya mengharapkan respons status 200 OK tanpa body untuk menandakan bahwa notifikasi telah diterima.

```json
// Cukup kembalikan HTTP Status 200 OK
```

Logika Backend (Wajib)
VERIFIKASI SIGNATURE KEY: Langkah pertama dan terpenting. Jika tidak valid, hentikan proses.
Cari transaksi di database Anda menggunakan order_id. Jika tidak ditemukan, abaikan.
Pastikan gross_amount dari notifikasi cocok dengan amount yang tersimpan di database Anda.
Periksa status transaksi saat ini. Jika sudah SUCCESS, jangan proses lagi (untuk menangani idempotency).
Update status transaksi di database Anda menjadi SUCCESS atau FAILED berdasarkan transaction_status.
Jika SUCCESS, berikan hak akses kepada pengguna untuk konten yang dibeli (misalnya, dengan membuat entri di tabel user_access atau sejenisnya).

### 5.3 Get User Transaction History

**Endpoint**: GET `/transactions`
**Authorization**: Authenticated User
**Description**: Mengambil daftar semua riwayat transaksi yang pernah dilakukan oleh pengguna yang sedang login.

#### Success Response `200 OK`
```json
{
  "status": "success",
  "message": "User transactions history retrieved successfully.",
  "data": [
    {
      "transaction_id": "a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
      "item_name": "Paket Ujian Akhir Pekan",
      "item_type": "PACKAGE",
      "amount": "59900.00",
      "status": "SUCCESS",
      "transaction_date": "2025-06-30T14:10:05Z"
    },
    {
      "transaction_id": "f9e8d7c6-b5a4-4f3e-2d1c-0b9a8f7e6d5c",
      "item_name": "Bundle Hemat 3-in-1",
      "item_type": "BUNDLE",
      "amount": "120000.00",
      "status": "FAILED",
      "transaction_date": "2025-06-29T11:00:00Z"
    }
  ]
}
```

## 6. Test Attempts Endpoints

Endpoint untuk mengatur test attempt yang dilakukan si User.

### 6.1 Start Session

**Endpoint**: POST /api/v1/test-attempts
**Authorization**: Authenticated User
**Description**: Membuat sebuah entri sesi ujian (test_attempts) baru untuk pengguna yang sedang login dan paket ujian yang dipilih. Endpoint ini adalah langkah pertama dalam alur pengerjaan ujian.

**Logika Bisnis Penting**: Sebelum membuat sesi, sistem harus memvalidasi apakah pengguna berhak mengakses paket ini (misalnya, paket tersebut berstatus is_trial=true atau pengguna telah melakukan transaksi pembelian yang valid untuk package_id tersebut).

#### Request Body

```json
{
  "package_id": 1
}
```

#### Success Response `201 CREATED`

Mengembalikan ID sesi ujian yang baru dibuat (attemptId), yang akan digunakan untuk semua interaksi selanjutnya dalam sesi ini.

```json
{
  "status": "success",
  "message": "Test attempt started successfully.",
  "data": {
    "test_attempt_id": 101,
    "package_id": 1,
    "user_id": 42,
    "start_time": "2025-06-30T14:05:00Z",
    "status": "IN_PROGRESS"
  }
}
```

#### Error Response

`403 Forbidden`: Pengguna tidak memiliki akses ke package_id ini.
`404 Not Found`: package_id tidak valid.

### 6.2 Get All Question For Test Session

**Endpoint**: GET `/test-attempts/{attemptId}/questions`
**Authorization**: Authenticated User
**Description**: Setelah sesi ujian dimulai, frontend memanggil endpoint ini untuk mendapatkan semua soal (questions) dan pilihan jawaban (options) yang terkait dengan sesi tersebut.

**Keamanan (Best Practice)**: Respons dari endpoint ini TIDAK BOLEH menyertakan informasi kunci jawaban (is_correct). Informasi ini hanya boleh ada di backend untuk mencegah kecurangan.

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Questions for the attempt retrieved successfully.",
  "data": {
    "test_attempt_id": 101,
    "package_name": "Paket Simulasi Reading TOPIK I",
    "questions": [
      {
        "id": 25,
        "question_text": "다음 밑줄 친 부분과 의미가 비슷한 것을 고르십시오.",
        "question_type": "READING",
        "image_url": null,
        "audio_url": null,
        "options": [
          { "id": 101, "option_text": "보기 1" },
          { "id": 102, "option_text": "보기 2" },
          { "id": 103, "option_text": "보기 3" },
          { "id": 104, "option_text": "보기 4" }
        ]
      },
      // ...soal-soal lainnya
    ]
  }
}
```


### 6.3 Save User Answer

**Endpoint**: `POST /test-attempts/{attemptId}/answers`
**Authorization**: Authenticated User
**Description**: Selama ujian berlangsung, setiap kali pengguna memilih sebuah jawaban, frontend mengirimkannya ke backend untuk disimpan di tabel user_answers. Ini memastikan progres pengguna tersimpan secara real-time.

#### Request - Path Parameters
{attemptId} (Long): ID unik dari sesi ujian yang sedang berlangsung.

#### Request Body
```json
{
  "question_id": 25,
  "selected_option_id": 103
}
```

#### Success Response `200 OK`

```json
{
  "status": "success",
  "message": "Answer saved successfully."
}
```

### 6.4 End Test Session

**Endpoint**: POST /api/v1/test-attempts/{attemptId}/finish
**Authorization**: Authenticated User
**Description**: Dipanggil ketika pengguna menekan tombol "Selesai" atau ketika waktu ujian habis. Backend akan melakukan kalkulasi skor, mengubah status menjadi COMPLETED, mengisi end_time, dan memicu proses evaluasi AI di background.

**Best Practice**: Proses kalkulasi skor dan evaluasi AI Gemini harus dijalankan sebagai background job (misalnya menggunakan @Async di Spring atau message queue) untuk mencegah request timeout dan memberikan respons cepat kepada pengguna.

#### Request - Path Parameters
{attemptId} (Long): ID unik dari sesi ujian yang akan diselesaikan.

#### Success Response `200 OK`
```json
{
  "status": "success",
  "message": "Test attempt finished. Score is being calculated.",
  "data": {
    "test_attempt_id": 101,
    "status": "COMPLETED",
    "score": 85.5, // Skor bisa langsung dihitung jika prosesnya cepat
    "ai_evaluation_status": "PROCESSING" // Memberi tahu frontend bahwa AI sedang bekerja
  }
}
```

### 6.5 Get User History Test

**Endpoint**: `GET /test-attempts`
**Authorization**: Authenticated User
**Description**: Mengambil daftar ringkasan dari semua riwayat pengerjaan ujian yang pernah dilakukan oleh pengguna yang sedang login.

#### Success Response `200 OK`
```json
{
  "status": "success",
  "message": "User test attempts history retrieved successfully.",
  "data": [
    {
      "test_attempt_id": 101,
      "package_name": "Paket Simulasi Reading TOPIK I",
      "attempt_date": "2025-06-30T14:05:00Z",
      "score": 85.5,
      "status": "VERIFIED"
    },
    {
      "test_attempt_id": 98,
      "package_name": "Free Trial Listening Section",
      "attempt_date": "2025-06-28T10:00:00Z",
      "score": 70.0,
      "status": "COMPLETED"
    }
  ]
}
```

### 6.6 Get Detail Test Result History

**Endpoint**: `GET /api/v1/test-attempts/{attemptId}`
**Authorization**: Authenticated User
**Description**: Mengambil laporan hasil lengkap dari satu sesi pengerjaan ujian yang telah selesai, termasuk skor, evaluasi AI (jika sudah ada), serta perbandingan jawaban pengguna dengan kunci jawaban.

#### Request - Path Parameters
{attemptId} (Long): ID unik dari sesi ujian yang telah selesai.

#### Success Response `200 OK`
```json
{
  "status": "success",
  "message": "Test attempt details retrieved successfully.",
  "data": {
    "test_attempt_id": 101,
    "package_name": "Paket Simulasi Reading TOPIK I",
    "score": 85.5,
    "status": "VERIFIED", // Status menjadi VERIFIED setelah AI selesai
    "ai_evaluation_result": "Analisis pengucapan Anda menunjukkan akurasi 90%. Namun, ada beberapa kata benda yang perlu diperbaiki...",
    "user_answers_review": [
      {
        "question_id": 25,
        "question_text": "...",
        "your_answer": {
          "option_id": 103,
          "option_text": "보기 3"
        },
        "correct_answer": {
          "option_id": 103,
          "option_text": "보기 3"
        },
        "is_correct": true
      },
      {
        "question_id": 26,
        "question_text": "...",
        "your_answer": {
          "option_id": 105,
          "option_text": "보기 1"
        },
        "correct_answer": {
          "option_id": 106,
          "option_text": "보기 2"
        },
        "is_correct": false
      }
    ]
  }
}
```

## 7. Vocabulary Gamification

Endpoint-endpoint berikut digunakan untuk mengakses data kosakata sebagai bagian dari fitur gamifikasi untuk pengguna.

### 7.1 Mendapatkan Daftar Kosakata
**Endpoint**: `GET /vocabularies`
**Authorization**: Bearer Token (User)
**Description**: Mengambil daftar semua kosakata yang tersedia. Endpoint ini mendukung filtering berdasarkan kategori, searching berdasarkan kata, dan pagination untuk mengelola jumlah data yang besar.

#### Query Parameters
GET /vocabularies?category=NOUN&page=1&limit=5

#### Success Response `200 OK`
```json
{
  "status": "success",
  "message": "Vocabularies fetched successfully.",
  "data": {
    "vocabularies": [
      {
        "id": 15,
        "koreanWord": "학교",
        "romanization": "hakgyo",
        "translation": "Sekolah",
        "category": "NOUN"
      },
      {
        "id": 21,
        "koreanWord": "집",
        "romanization": "jib",
        "translation": "Rumah",
        "category": "NOUN"
      },
      {
        "id": 34,
        "koreanWord": "책",
        "romanization": "chaek",
        "translation": "Buku",
        "category": "NOUN"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 4,
      "totalItems": 20
    }
  }
}
```

#### Error Responses
`400 Bad Request`: Parameter category tidak valid.
`401 Unauthorized`: Pengguna belum login.
```
---
```
