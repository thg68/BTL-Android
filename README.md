# BTL Android — POS Lẩu

Ứng dụng quản lý quán lẩu cho **bài tập lớn môn Lập trình Android**. Trọng tâm: UI/UX với Jetpack Compose + Material 3, real-time sync qua Firebase Firestore, thanh toán bằng VietQR.

---

## Tính năng nổi bật

### Khách hàng
- Đăng nhập theo **số bàn** (1–15)
- Trang chủ với banner khuyến mãi, lối tắt nhanh, danh sách món gợi ý có shimmer loading
- Duyệt menu, tìm kiếm món, thêm vào giỏ
- Xem giỏ hàng, gửi món xuống bếp
- Xem hoá đơn theo bàn, **quét VietQR chuẩn EMVCo/NAPAS** để chuyển khoản
- Hồ sơ cá nhân, lịch sử đơn, **toggle dark mode**

### Nhân viên
- Đăng nhập username/password (mặc định `admin / 123`)
- **Dashboard** doanh thu hôm nay, đơn đã đóng, bàn đang phục vụ, biểu đồ cột 7 ngày, top 5 món bán chạy (vẽ bằng Canvas)
- Sơ đồ **15 bàn** color-coded (Trống / Đang phục vụ / Đã đặt)
- **POS** theo bàn: tìm món, thêm vào giỏ, gửi bếp
- **KDS (Kitchen Display System)** 3 cột: Cần làm / Đang nấu / Hoàn tất, AnimatedVisibility khi đổi trạng thái
- Quản lý món ăn: bật/tắt availability bằng Switch
- Thu ngân: xác nhận thanh toán, đóng đơn

### UI / UX
- Dark mode đầy đủ, sync màu status bar
- Splash screen native (AndroidX Core SplashScreen)
- Animation slide + fade chuyển màn hình (280ms)
- Shimmer skeleton loading, AsyncImage tải ảnh món qua Coil
- Empty state có icon + mô tả ở mọi danh sách
- Snackbar feedback cho các action quan trọng

---

## Tech stack

| Lớp | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin 2.2 |
| UI | Jetpack Compose, Material 3, Navigation Compose |
| Kiến trúc | MVVM, single `PosViewModel` + StateFlow + Coroutines |
| Backend | Firebase Firestore (real-time listener) |
| Ảnh | Coil 2.7 (`AsyncImage`) |
| QR code | `qrcode-kotlin` 4.2 + custom VietQR EMV TLV + CRC16-CCITT |
| Splash | `androidx.core:core-splashscreen` 1.0 |
| Build | Gradle + Kotlin DSL, AGP 9 |

---

## Cách build & chạy

### Yêu cầu
- Android Studio **Ladybug** (2024.2) trở lên
- JDK 11+
- Thiết bị / emulator Android **API 24+** (Android 7.0)
- Tài khoản Firebase (đã có project — file `google-services.json` đặt trong `app/`)

### Các bước
1. Clone repo
   ```
   git clone https://github.com/thg68/BTL-Android.git
   cd BTL-Android
   ```
2. Mở project bằng Android Studio, đợi Gradle sync xong
3. Đảm bảo `app/google-services.json` đã có (file Firebase config)
4. Bấm **Run ▶** trên Android Studio (chọn emulator hoặc cắm máy thật bật USB debug)
5. App tự động seed 15 bàn + 10 món mẫu + 18 đơn closed (cho dashboard) trong lần chạy đầu

---

## Tài khoản test

| Vai trò | Tài khoản |
|---|---|
| Nhân viên | username `admin`, password `123` |
| Khách hàng | Chọn bàn 1–15 ở tab "Khách hàng" |

---

## Demo flow gợi ý

Xem chi tiết script thuyết trình trong [`DEMO_SCRIPT.md`](DEMO_SCRIPT.md).

Tóm tắt 5–7 phút:
1. Mở app → splash → LoginScreen
2. Customer flow: login bàn 5 → Home → POS → giỏ → gửi bếp → hoá đơn → QR
3. Staff flow: login `admin/123` → Dashboard → Bàn → KDS đổi trạng thái → Thu ngân
4. Code walkthrough: `Theme.kt` (dark mode), `VietQrGenerator.kt` (EMV + CRC16), `DashboardScreen.kt` (Canvas bar chart)

---

## Cấu trúc thư mục

```
app/src/main/java/com/example/androidbtl/
├── MainActivity.kt
├── data/
│   ├── VietQrGenerator.kt          # EMV TLV builder + CRC16-CCITT
│   └── models/Models.kt            # RestaurantTable, MenuItem, Order, OrderItem
├── ui/
│   ├── components/
│   │   ├── AppBottomNavBar.kt      # Bottom nav, role-based items
│   │   ├── CommonComponents.kt     # AsyncFoodImage, EmptyState, Shimmer skeleton
│   │   └── QrPaymentDialog.kt      # Dialog QR thanh toán
│   ├── navigation/AppNavigation.kt # NavHost, slide+fade transitions, snackbar host
│   ├── screens/
│   │   ├── LoginScreen.kt
│   │   ├── HomeScreen.kt           # Trang chủ khách hàng
│   │   ├── POSOrderScreen.kt       # POS chọn món
│   │   ├── BookingScreen.kt        # Giỏ hàng
│   │   ├── BillScreen.kt           # Hoá đơn + QR
│   │   ├── ProfileScreen.kt        # Tài khoản + dark mode toggle
│   │   ├── DashboardScreen.kt      # Dashboard staff (chart Canvas)
│   │   ├── TableManagementScreen.kt
│   │   ├── KitchenDisplayScreen.kt
│   │   ├── StaffMenuScreen.kt
│   │   └── BillingScreen.kt
│   └── theme/
│       ├── Theme.kt                # Light + Dark color scheme
│       ├── Color.kt
│       ├── Type.kt                 # Typography scale
│       └── ThemeState.kt           # CompositionLocal cho dark mode
└── viewmodel/PosViewModel.kt       # Single VM, Firestore listeners, seed data
```

---

## Thành viên

- Nguyễn Ngọc Hiệp — ()

---

## Ghi chú

- Dashboard có dữ liệu mẫu được seed tự động trong lần chạy đầu tiên (18 closed orders trải đều 7 ngày). Đoạn này nằm trong `PosViewModel.seedClosedOrdersIfEmpty()` — có thể xoá khi đưa lên production.
- VietQR config (số tài khoản, ngân hàng) nằm tại `data/VietQrGenerator.kt → VietQrConfig`. Đổi `BANK_ACCOUNT_NUMBER` / `BANK_ACCOUNT_NAME` để demo với tài khoản thật.
