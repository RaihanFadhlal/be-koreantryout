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


# Transaction

## User

### Create New Transaction (Detailed Flow)

1.  **User Initiates Purchase**: The user, while logged in, clicks a "buy" button on the frontend for a specific `TestPackage` or `Bundle`.
2.  **Frontend Sends Request**: The frontend sends a request to the backend endpoint (e.g., `POST /api/transactions/create`) containing the ID of the package/bundle the user wants to purchase.
3.  **Backend Creates Transaction Record**: The backend receives the request. It creates a new `Transaction` entity in the database with an initial status (e.g., `PENDING`) and a unique `order_id`. This record links the user to the item being purchased.
4.  **Backend Requests Payment from Midtrans**: The backend service communicates with the Midtrans API, sending details of the transaction (like `order_id`, amount, item details) to create a new payment session.
5.  **Midtrans Returns Payment Redirect URL**: Midtrans processes the request and returns a `redirect_url` to the backend. This URL is for the Midtrans payment page (Snap.js).
6.  **Backend Responds to Frontend**: The backend sends a response to the frontend, containing the `redirect_url` from Midtrans.
7.  **Frontend Redirects to Midtrans**: The frontend receives the `redirect_url` and redirects the user's browser to the Midtrans payment page.
8.  **User Completes Payment**: The user completes the payment process on the Midtrans page (e.g., using a credit card, bank transfer, etc.).
9.  **Midtrans Sends Webhook Notification**: After the payment is completed (or fails), Midtrans sends an asynchronous HTTP notification (webhook) to a dedicated endpoint on the backend (e.g., `POST /api/transactions/midtrans/webhook`). This notification contains the final status of the transaction (`settlement`, `expire`, `cancel`, etc.).
10. **Backend Processes Webhook**: The backend's webhook handler receives the notification. It verifies the authenticity of the notification (using the signature key from Midtrans).
11. **Backend Updates Transaction Status**: Based on the status in the webhook payload:
    *   If the payment is successful (`transaction_status` is `settlement`), the backend updates the corresponding `Transaction` entity's status to `SUCCESS`.
    *   If the payment fails or expires, the backend updates the status to `FAILED` or `EXPIRED`.
12. **Backend Grants Access (on Success)**: If the transaction was successful, the backend now grants the user access to the purchased content. This involves:
    *   Creating a `TestAttempt` record for the user and the purchased `TestPackage`. This record signifies that the user is now eligible to take the test.
    *   If a `Bundle` was purchased, it creates `TestAttempt` records for *all* `TestPackage`s within that bundle.
13. **User Can Access Content**: The user can now see the purchased test package(s) as available on their dashboard on the frontend and can start the exam. The frontend will periodically check the transaction status or get updated user permissions from the backend.
