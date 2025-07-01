# Authentication

### Forgot Password

1. User memasukkan email di halaman lupa password frontend dan mengirimkannya ke backend (POST /api/v1/auth/forgot-password).
2. Backend membuat token reset yang unik, menyimpannya, dan mengirim email ke user. Email ini berisi link ke backend.
3. User mengklik link di email. Link ini mengarah ke endpoint backend baru (GET /api/v1/auth/reset-password).
4. Endpoint GET backend ini akan memvalidasi token. Jika valid, ia akan mengarahkan (redirect) browser user ke halaman frontend http://localhost:5173/reset-password?token=....
5. Frontend di halaman /reset-password akan menampilkan form untuk memasukkan password baru. Form ini, saat disubmit, akan mengirim request ke backend (POST /api/v1/auth/reset-password) beserta token dan password baru.
6. Backend memvalidasi token sekali lagi, mengubah password user, dan mengembalikan respons sukses.
7. Frontend, setelah menerima respons sukses, akan mengarahkan user ke halaman login.

# Profile Management

## User

### Update Foto
1. User telah login (kredensial valid). FE mempunyai Dashboard User Management. User mengklik button untuk upload Foto.
2. Backend akan menerima foto dari FE kemudian mengirimkan ke Cloudinary.
3. Cloudinary akan menyimpan data tersebut kemudian menyimpannya sebagai url. Cloudinary mengirim response berupa url ke BE.
4. BE menerima url dan memasukkannya kedalam Entity user dalam kolom ImageUrl
5. FE akan mengambil imageURL dari Entity User untuk ditampilkan pada Dashboard User Management.
(Validasi : User hanya bisa mengubah profilenya sendiri/Tidak bisa mengubah profile User yang lain)

### Lihat Profile Diri Sendiri
1. FE menggunakan method GetMyProfile untuk menampilkan profile User di Dashboard User Management.

### Change Password
1. User telah login (kredensial valid). Dari Dashboard User Management, User akan mengklik button Change Password maka FE akan redirect ke page
Change Password.
2. User akan mengisi currentPassword, newPassword, dan confirmNewPassword. Kemudian FE akan mengirim ketiga hal tersebut sebagai request yang akan diterima oleh BE.
3. BE akan melakukan validasi apakah password sesuai kriteria (panjang password 8).
4. Jika sudah sesuai, maka BE akan mengembalikan response berupa message 'Password berhasil diubah'.
(Validasi : User hanya bisa mengubah password miliknya sendiri)

## Admin

### Get All User
1. Admin dari Dashboard Admin, Admin bisa mengklik tombol/page yang menampilkan seluruh User.
