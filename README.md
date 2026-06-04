# SaKa System

Ứng dụng Android quản lý nhà hàng lẩu theo mô hình **khách hàng tại bàn** và **nhân viên vận hành**. Dự án được xây dựng bằng Kotlin, Jetpack Compose Material 3 và Firebase Firestore realtime.

SaKa System hỗ trợ khách quét QR/nhập số bàn để gọi món, theo dõi trạng thái món, thanh toán bằng VietQR; đồng thời hỗ trợ nhân viên quản lý bàn, bếp, thực đơn, xác nhận thanh toán và xem doanh thu.

## Tính năng chính

### Khách hàng

- Đăng nhập theo số bàn hoặc QR mở bàn.
- Xem trang chủ theo bàn đang sử dụng.
- Gọi món theo danh mục, tìm kiếm món và thêm vào giỏ.
- Gửi món xuống bếp.
- Theo dõi trạng thái món: đã gửi, đang nấu, sẵn sàng.
- Xem hóa đơn theo bàn.
- Tạo QR VietQR để thanh toán.
- Báo nhân viên sau khi đã thanh toán.
- Nhận thông báo khi món đã hoàn tất.
- Đổi giao diện sáng/tối và đăng xuất khỏi bàn.

### Nhân viên

- Đăng nhập hệ thống nhân viên.
- Quản lý sơ đồ bàn: thêm, sửa, xóa, đặt bàn, hủy đặt bàn.
- Mở bàn cho khách bằng QR đăng nhập có access code.
- Gọi món/POS cho từng bàn.
- Điều phối bếp qua KDS: cần làm, đang nấu, hoàn tất.
- Quản lý thực đơn: thêm, sửa, xóa món, bật/tắt trạng thái đang bán.
- Nhận thông báo realtime: khách gọi phục vụ, có món mới, khách báo thanh toán.
- Xác nhận thanh toán và đóng hóa đơn.
- Xem doanh thu trong ngày.

## Công nghệ sử dụng

| Nhóm | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Điều hướng | Navigation Compose |
| State/Lifecycle | ViewModel, StateFlow, `collectAsStateWithLifecycle` |
| Database realtime | Firebase Firestore |
| Push notification | Firebase Cloud Messaging |
| Ảnh | Coil |
| QR thanh toán | qrcode-kotlin, VietQR payload |
| QR đăng nhập bàn | qrcode-kotlin |
| Quét QR | CameraX, ML Kit Barcode Scanning |
| Build | Gradle Kotlin DSL, Android Gradle Plugin |

## Kiến trúc tổng quan

Dự án là ứng dụng Android single-module, theo hướng **MVVM + component-based UI**.

- `MainActivity` khởi tạo theme và gọi `AppNavigation`.
- `AppNavigation` quản lý role khách/nhân viên, route, bottom navigation, snackbar và auto logout khi nhân viên đóng bàn.
- `PosViewModel` là lớp trung tâm quản lý dữ liệu, realtime listener và thao tác Firestore.
- Các màn hình trong `ui/screens` chỉ tập trung vào UI và gọi ViewModel qua callback.
- Các component dùng chung nằm trong `ui/components`.
- Firebase Firestore là nguồn dữ liệu chính cho bàn, món ăn, order và thông báo nhân viên.

## Cấu trúc thư mục

```text
app/src/main/java/com/example/androidbtl
├── MainActivity.kt
├── MyFirebaseMessagingService.kt
├── data
│   ├── VietQrGenerator.kt
│   └── models/Models.kt
├── ui
│   ├── components
│   │   ├── BottomNavBar.kt
│   │   ├── CommonComponents.kt
│   │   └── QrPaymentDialog.kt
│   ├── navigation/AppNavigation.kt
│   ├── screens
│   │   ├── BillingScreen.kt
│   │   ├── BillScreen.kt
│   │   ├── BookingScreen.kt
│   │   ├── HomeScreen.kt
│   │   ├── KitchenDisplayScreen.kt
│   │   ├── LoginScreen.kt
│   │   ├── MenuScreen.kt
│   │   ├── OffersScreen.kt
│   │   ├── POSOrderScreen.kt
│   │   ├── ProfileScreen.kt
│   │   ├── RevenueScreen.kt
│   │   ├── StaffMenuScreen.kt
│   │   └── TableManagementScreen.kt
│   └── theme
├── utils
│   ├── FcmSender.kt
│   └── NotificationHelper.kt
└── viewmodel/PosViewModel.kt
```

Tài liệu phân tích chi tiết từng UI nằm trong thư mục `docs/`.

## Luồng sử dụng

### Luồng khách hàng

1. Khách nhập số bàn hoặc quét QR bàn.
2. App kiểm tra trạng thái bàn trong Firestore.
3. Nếu hợp lệ, app mở order cho bàn và chuyển tới trang chủ khách.
4. Khách chọn món ở tab thực đơn.
5. Món được thêm vào order với trạng thái `Cart`.
6. Khi gửi bếp, món chuyển sang `Pending`.
7. Bếp chuyển món qua `Cooking` rồi `Done`.
8. Khách xem hóa đơn, tạo QR VietQR và bấm báo đã thanh toán.
9. Nhân viên xác nhận thanh toán, order chuyển sang `Closed`.
10. Khi phục vụ xong, nhân viên bấm `Đóng bàn`; bàn về `Trống` và khách đang đăng nhập ở bàn đó tự động đăng xuất.

### Luồng nhân viên

1. Nhân viên đăng nhập bằng tài khoản hệ thống.
2. Màn sơ đồ bàn hiển thị toàn bộ trạng thái bàn realtime.
3. Nhân viên có thể mở bàn, đặt bàn, tạo QR đăng nhập hoặc QR thanh toán.
4. Màn bếp xử lý món theo trạng thái.
5. Màn xác nhận thanh toán hiển thị các hóa đơn đang chờ xử lý.
6. Setting bàn cho phép đóng bàn đang mở để kết thúc phiên khách.
7. Màn quản lý món cho phép cập nhật thực đơn.
8. Màn doanh thu thống kê các order đã đóng trong ngày.

## Firebase Firestore

Dự án đang sử dụng các collection chính:

| Collection | Vai trò |
|---|---|
| `tables` | Danh sách bàn, trạng thái bàn, capacity, FCM token của bàn, access code QR |
| `menu_items` | Danh sách món ăn, danh mục, giá, mô tả, ảnh, trạng thái đang bán |
| `orders` | Order đang mở hoặc đã đóng, danh sách item và tổng tiền |
| `staff_notifications` | Thông báo realtime cho nhân viên |
| `staff_devices` | FCM token thiết bị nhân viên |

Khi Firestore trống, `PosViewModel` có logic seed dữ liệu mẫu cho bàn và thực đơn.

## Cài đặt và chạy dự án

### Yêu cầu

- Android Studio bản mới.
- JDK 11 hoặc mới hơn.
- Thiết bị/emulator Android API 24 trở lên.
- Firebase project đã bật Firestore và Cloud Messaging.

### Cấu hình Firebase

1. Tạo Firebase project.
2. Tạo Android app trong Firebase Console với package:

```text
com.example.androidbtl
```

3. Tải file `google-services.json`.
4. Đặt file vào:

```text
app/google-services.json
```

5. Bật Firestore Database.
6. Bật Firebase Cloud Messaging.

### Cấu hình gửi FCM hiện tại

Trong code hiện tại, `FcmSender.kt` đọc file service account từ:

```text
app/src/main/assets/service_account.json
```

Đây là cách phù hợp cho bài tập/demo nội bộ, nhưng **không an toàn cho production** vì service account không nên nằm trong app client. Nếu triển khai thật, nên chuyển logic gửi FCM sang backend hoặc Cloud Functions.

### Chạy bằng Android Studio

1. Mở project bằng Android Studio.
2. Sync Gradle.
3. Chọn emulator hoặc thiết bị thật.
4. Run app.

### Chạy bằng command line

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

macOS/Linux:

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
```

## Tài khoản demo

Tài khoản nhân viên hiện đang được kiểm tra trực tiếp trong `LoginScreen.kt`:

```text
Username: admin
Password: 123
```

Khách hàng đăng nhập bằng số bàn đang có trong Firestore, ví dụ các bàn được seed tự động khi database trống.

## Quyền Android

Ứng dụng khai báo các quyền:

| Quyền | Mục đích |
|---|---|
| `INTERNET` | Kết nối Firebase, tải ảnh, gửi/nhận dữ liệu |
| `POST_NOTIFICATIONS` | Hiển thị thông báo trên Android 13 trở lên |
| `CAMERA` | Quét QR đăng nhập bàn |

## Tài liệu UI

Thư mục `docs/` chứa 17 file Markdown phân tích từng UI chính:

- Navigation và bottom navigation.
- Component dùng chung.
- Dialog QR thanh toán.
- Các màn khách hàng.
- Các màn nhân viên.
- Phân tích chức năng, luồng hoạt động và từng cụm dòng code.

## Lưu ý kỹ thuật

- App name hiện tại là `SaKa System`.
- Deep link QR bàn hiện vẫn dùng scheme `androidbtl://table/...`.
- `MenuScreen.kt` là màn thực đơn tĩnh, hiện không được dùng trong `AppNavigation`; tab thực đơn thực tế dùng `POSOrderScreen.kt`.
- Tài khoản nhân viên đang hard-code, chưa có hệ thống authentication thật.
- Service account FCM không nên đóng gói trong app nếu đưa vào production.
- Một số màu UI vẫn hard-code ở vài màn, nên kiểm tra thêm nếu tối ưu dark mode.

## Kiểm thử nhanh

Sau khi cấu hình Firebase, nên kiểm tra các flow sau:

1. Khách đăng nhập bàn trống.
2. Khách gọi món và gửi bếp.
3. Nhân viên thấy món mới ở KDS.
4. Nhân viên chuyển món sang hoàn tất.
5. Khách nhận thông báo món sẵn sàng.
6. Khách tạo QR và báo đã thanh toán.
7. Nhân viên thấy hóa đơn ở tab xác nhận thanh toán.
8. Nhân viên xác nhận thanh toán, order chuyển sang đã đóng.
9. Nhân viên đóng bàn ở setting bàn.
10. Khách đang đăng nhập ở bàn đó tự động đăng xuất.
