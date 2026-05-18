# Script demo BTL Android — POS Lẩu

**Thời lượng**: 5–7 phút thuyết trình + 2 phút Q&A.

**Chuẩn bị trước khi demo**
- Cài app trên 1 thiết bị (hoặc emulator có wifi để Firebase sync)
- Đảm bảo đã chạy app ít nhất 1 lần để seed data (15 bàn + 10 món + 18 đơn mẫu cho dashboard)
- Mở sẵn Android Studio bên cạnh để show code khi cần

---

## 0:00 – 0:30 — Mở đầu

> "Em xin trình bày bài tập lớn: **ứng dụng POS quản lý quán lẩu**, viết bằng Kotlin với Jetpack Compose và Material 3. App có 2 vai trò: khách hàng và nhân viên, dữ liệu sync real-time qua Firebase."

**Bấm icon app trên launcher** → splash screen logo trên nền tối hiện 600ms → vào màn hình đăng nhập.

> "Splash screen được làm bằng AndroidX Core SplashScreen API chuẩn của Android 12+."

---

## 0:30 – 2:30 — Customer flow

**Tab "Khách hàng"** → nhập bàn `5` → bấm **TIẾP TỤC**.

- HomeScreen mở ra, **shimmer loading** chạy trên carousel "Món gợi ý" → fade vào danh sách thật.
- > "Đây là shimmer skeleton dùng Compose `rememberInfiniteTransition`."

**Bấm lối tắt "Thực Đơn"** → POS Order Screen.

- Gõ "bò" vào ô tìm kiếm → list filter live.
- Bấm + vào "Bò Wagyu A5" (2 lần) → cart counter nảy bằng spring animation.
- > "Animation cart icon dùng `animateFloatAsState` với spring stiffness."

**Bấm icon giỏ** → BookingScreen → bấm "Gửi món xuống bếp" → snackbar top hiện "Đã gửi món".

**Bấm tab Hoá đơn** → BillScreen → bấm **TẠO QR THANH TOÁN**.

- Dialog QR mở ra với mã VietQR chuẩn EMVCo.
- > "QR này tuân thủ chuẩn NAPAS, có TLV và checksum CRC16-CCITT. Khách dùng app MBBank/Vietcombank quét → tự fill số tiền + nội dung 'BAN5'."

**Bấm tab Tài khoản** → ProfileScreen → toggle "Chế độ tối" → toàn app đổi sang dark mode mượt → toggle lại.

> "Dark mode dùng `CompositionLocal` để chia sẻ state, theme tự đổi `colorScheme` và `statusBarColor`."

---

## 2:30 – 4:30 — Staff flow

**Bấm "Đăng xuất"** → quay lại Login → tab **"Nhân viên"** → nhập `admin / 123`.

**Tab "Báo cáo"** (Dashboard).

- 3 stat card: Doanh thu hôm nay / Đơn đã đóng / Bàn đang phục vụ.
- **Bar chart 7 ngày** vẽ bằng Compose `Canvas` (không dùng thư viện ngoài).
- Top 5 món bán chạy, mỗi món có progress bar so sánh.
- > "Toàn bộ chart này em vẽ tay bằng Canvas API: `drawRoundRect`, `drawLine`. Không thêm dependency."

**Tab "Bàn"** → grid 15 bàn, bàn 5 đang xanh (Đang phục vụ).

- Bấm bàn 5 → POS screen của nhân viên cho bàn 5.

**Tab "Bếp"** (KDS) — 3 cột Cần làm / Đang nấu / Hoàn tất.

- Bấm "Bắt đầu nấu" → item slide+scale sang cột Đang nấu (`AnimatedVisibility`).
- Bấm "Hoàn tất" → slide sang cột Hoàn tất.

**Tab "Món"** (Staff Menu) — bấm Switch tắt 1 món → snackbar "Đã ẩn món...".

**Tab "Thu ngân"** → bàn 5 hiện trong list → bấm "Xác nhận thanh toán" → snackbar "Đã đóng bàn 5".

---

## 4:30 – 5:30 — Code walkthrough nhanh

Chuyển sang Android Studio.

1. **`ui/theme/Theme.kt`** — show `AndroidBTLTheme` với `LocalThemeIsDark` để cả app phản ứng đồng bộ.
2. **`data/VietQrGenerator.kt`** — show hàm `build()` với chuỗi TLV và `crc16Ccitt()`.
3. **`ui/screens/DashboardScreen.kt`** — show hàm `BarChart` Canvas.
4. **`viewmodel/PosViewModel.kt`** — show 4 listener Firestore real-time + `ensureOrderForTable` xử lý case khách quay lại app.

---

## 5:30 – 6:30 — Kết bài

> "Tóm lại, app gồm 11 màn hình, dùng 1 ViewModel duy nhất theo MVVM, Firestore làm backend, có dark mode, splash screen, animation, shimmer loading, empty state, snackbar feedback, và thanh toán VietQR chuẩn ngân hàng Việt Nam. Em xin cảm ơn thầy/cô."

---

## Q&A — câu hỏi thường gặp

| Hỏi | Trả lời ngắn |
|---|---|
| Sao chọn Compose mà không phải XML? | Compose declarative, ít boilerplate, dark mode/animation dễ hơn nhiều. |
| Firebase free tier có đủ không? | Spark free, 50k reads + 20k writes / ngày. Quá đủ demo trong lớp. |
| VietQR có chạy thật không? | Có. Quét bằng MBBank app → tự fill số tiền + nội dung. |
| Sao 1 ViewModel cho cả app? | Đơn giản hoá scope, vì tất cả screen đều share dữ liệu menu/tables/orders. Production sẽ tách theo feature. |
| Bar chart không dùng thư viện? | Đúng. Vẽ tay bằng `Canvas` 30 dòng — đủ đẹp, không phụ thuộc lib bên ngoài. |
| Demo data từ đâu? | `PosViewModel.seedDatabaseIfEmpty()` + `seedClosedOrdersIfEmpty()` chạy 1 lần lúc init. Production xoá đi. |
