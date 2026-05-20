# SAKA System – Ứng dụng quản lý nhà hàng

Ứng dụng Android quản lý nhà hàng theo mô hình **Staff / Customer**, xây dựng bằng Jetpack Compose + Firebase Firestore.

## Tính năng

| Vai trò | Màn hình |
|---|---|
| Staff | Quản lý bàn, POS gọi món, Màn hình bếp (KDS), Quản lý thực đơn, Thanh toán, Doanh thu |
| Customer | Trang chủ, Gọi món, Giỏ hàng, Hóa đơn + QR VietQR, Hồ sơ |

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Firebase Firestore** – realtime database
- **MVVM** – `PosViewModel` dùng chung toàn app
- **Coil** – tải ảnh món ăn
- **qrcode-kotlin** – tạo mã QR VietQR thanh toán

## Cài đặt

1. Clone repo, mở bằng **Android Studio**
2. Thêm file `google-services.json` vào `app/` (lấy từ Firebase Console)
3. Build & chạy (min SDK 24)

> Lần đầu khởi chạy sẽ tự động seed dữ liệu mẫu (bàn + thực đơn) vào Firestore.
