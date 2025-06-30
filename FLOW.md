# Forgot Password

1. User memasukkan email di halaman lupa password frontend dan mengirimkannya ke backend (POST /api/v1/auth/forgot-password).
2. Backend membuat token reset yang unik, menyimpannya, dan mengirim email ke user. Email ini berisi link ke backend.
3. User mengklik link di email. Link ini mengarah ke endpoint backend baru (GET /api/v1/auth/reset-password).
4. Endpoint GET backend ini akan memvalidasi token. Jika valid, ia akan mengarahkan (redirect) browser user ke halaman frontend http://localhost:5173/reset-password?token=....
5. Frontend di halaman /reset-password akan menampilkan form untuk memasukkan password baru. Form ini, saat disubmit, akan mengirim request ke backend (POST /api/v1/auth/reset-password) beserta token dan password baru.
6. Backend memvalidasi token sekali lagi, mengubah password user, dan mengembalikan respons sukses.
7. Frontend, setelah menerima respons sukses, akan mengarahkan user ke halaman login.