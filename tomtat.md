# Tóm tắt và giải thích code SAKA System

## 1. Tổng quan đề tài

Repo này là ứng dụng Android quản lý gọi món cho nhà hàng lẩu **SAKA Hotpot / SAKA System**. App được viết bằng **Kotlin + Jetpack Compose**, dùng **Firebase Firestore** để đồng bộ dữ liệu bàn, món ăn, đơn hàng và thông báo theo thời gian thực.

Ứng dụng có 2 nhóm người dùng:

- **Khách hàng**: vào bàn bằng số bàn hoặc QR mở bàn, xem trang chủ, gọi món, xem giỏ hàng, gửi món xuống bếp, xem hóa đơn, tạo VietQR thanh toán và báo nhân viên kiểm tra thanh toán.
- **Nhân viên**: đăng nhập nội bộ, quản lý bàn, mở bàn bằng QR cho khách, gọi món thay khách, theo dõi bếp, quản lý thực đơn, xác nhận thanh toán, xem doanh thu trong ngày.

Tầng xử lý nghiệp vụ chính là `PosViewModel`. Các màn hình Compose đọc dữ liệu qua `StateFlow` và gọi các hàm ViewModel để cập nhật Firestore.

## 2. Công nghệ sử dụng

- **Kotlin**: ngôn ngữ chính.
- **Jetpack Compose + Material 3**: xây dựng giao diện.
- **Navigation Compose**: điều hướng giữa các màn hình.
- **Firebase Firestore**: lưu dữ liệu realtime bằng `addSnapshotListener`.
- **Firebase Messaging / FCM HTTP v1**: gửi thông báo đẩy cho khách và nhân viên.
- **CameraX + ML Kit Barcode Scanning**: quét QR mở bàn ở màn đăng nhập khách.
- **Coil Compose**: tải ảnh món ăn từ URL.
- **qrcode-kotlin**: render QR thanh toán và QR mở bàn.
- **MVVM + StateFlow / SharedFlow**: quản lý state và sự kiện UI.

## 3. Cấu trúc thư mục chính

```text
app/src/main/java/com/example/androidbtl
├── MainActivity.kt
├── MyFirebaseMessagingService.kt
├── data
│   ├── VietQrGenerator.kt
│   └── models/Models.kt
├── utils
│   ├── FcmSender.kt
│   └── NotificationHelper.kt
├── viewmodel/PosViewModel.kt
└── ui
    ├── navigation/AppNavigation.kt
    ├── components/
    ├── screens/
    └── theme/
```

Ý nghĩa:

- `MainActivity.kt`: điểm vào app, bật edge-to-edge, xin quyền notification và gọi theme/navigation.
- `Models.kt`: định nghĩa model bàn, món, order, thông báo.
- `VietQrGenerator.kt`: tạo payload VietQR theo TLV/EMVCo.
- `FcmSender.kt`: gửi FCM HTTP v1 bằng service account trong assets.
- `NotificationHelper.kt`: hiển thị local notification trên Android.
- `PosViewModel.kt`: trung tâm dữ liệu và nghiệp vụ.
- `AppNavigation.kt`: phân vai khách/nhân viên, route, deep link, bottom navigation.
- `ui/screens`: các màn hình nghiệp vụ.
- `ui/components`: component dùng lại như bottom bar, ảnh món, empty state, skeleton, chuông thông báo, dialog QR.
- `ui/theme`: màu sắc, typography, light/dark theme.

## 4. Model dữ liệu

File: `app/src/main/java/com/example/androidbtl/data/models/Models.kt`

### `RestaurantTable`

Đại diện cho một bàn:

```kotlin
data class RestaurantTable(
    val id: String = "",
    val name: String = "",
    val status: String = "Trống",
    val capacity: Int = 4,
    val fcmToken: String = "",
    val accessCode: String = ""
)
```

Trạng thái bàn:

- `Trống`: chưa có khách.
- `Đang phục vụ`: đang có order mở hoặc đã được nhân viên mở bàn.
- `Đã đặt`: bàn được giữ trước, khách không thể tự vào.

`fcmToken` dùng để gửi thông báo cho thiết bị khách đang ngồi ở bàn đó. `accessCode` là mã ngẫu nhiên dùng trong QR mở bàn.

### `MenuItem`

Đại diện món ăn trong thực đơn:

- `id`: id document Firestore.
- `name`, `category`, `price`, `description`, `imageUrl`: thông tin hiển thị.
- `isAvailable`: còn bán hay hết hàng. Field này map với Firestore key `available`.

### `OrderItem`

Đại diện một món trong đơn hàng:

- `menuItemId`, `name`, `quantity`, `price`.
- `status`: trạng thái món.

Pipeline trạng thái món:

```text
Cart -> Pending -> Cooking -> Done
```

Ý nghĩa:

- `Cart`: mới thêm vào giỏ, chưa gửi bếp.
- `Pending`: đã gửi bếp, chờ xử lý.
- `Cooking`: bếp đang nấu.
- `Done`: món đã xong.

### `Order`

Đại diện một đơn hàng của một bàn:

- `tableId`: bàn sở hữu đơn.
- `items`: danh sách món.
- `totalAmount`: tổng tiền.
- `status`: `Open` hoặc `Closed`.
- `timestamp`: thời điểm tạo/cập nhật đóng đơn.

### `NotificationItem`

Dùng cho thông báo nội bộ của nhân viên:

- `message`: nội dung.
- `isRead`: đã đọc hay chưa.
- `targetRoute`: route cần mở khi bấm thông báo, ví dụ `kds`, `billing`, `tables`.

## 5. Firestore

Các collection chính đang được code sử dụng:

### `tables`

Lưu danh sách bàn:

```json
{
  "id": "1",
  "name": "Bàn 1",
  "status": "Trống",
  "capacity": 4,
  "fcmToken": "",
  "accessCode": ""
}
```

### `menu_items`

Lưu thực đơn:

```json
{
  "name": "Bò Wagyu A5",
  "category": "Thịt bò",
  "price": 299000,
  "available": true,
  "description": "Thịt bò Wagyu Nhật Bản thượng hạng",
  "imageUrl": "https://..."
}
```

### `orders`

Lưu đơn hàng:

```json
{
  "tableId": "1",
  "items": [
    {
      "menuItemId": "...",
      "name": "Bò Wagyu A5",
      "quantity": 2,
      "price": 299000,
      "status": "Pending"
    }
  ],
  "totalAmount": 598000,
  "status": "Open",
  "timestamp": 1710000000000
}
```

### `staff_notifications`

Lưu thông báo cho nhân viên, ví dụ khách gọi phục vụ hoặc khách báo đã thanh toán.

### `staff_devices`

Lưu token FCM của thiết bị nhân viên để gửi push notification.

## 6. `PosViewModel` và nghiệp vụ chính

File: `app/src/main/java/com/example/androidbtl/viewmodel/PosViewModel.kt`

Các state chính:

- `tables`: danh sách bàn.
- `menuItems`: danh sách món.
- `activeOrders`: các order `Open`.
- `closedOrders`: các order `Closed`.
- `notifications`: thông báo nhân viên.
- `pendingItemCount`: tổng số món `Pending`.
- `topSellingItems`: món gợi ý, tính từ order đã đóng; nếu chưa có doanh thu thì lấy món còn bán.
- `unreadCount`: số thông báo chưa đọc.
- `isLoadingMenu`, `isLoadingTables`: trạng thái tải dữ liệu.

Khi khởi tạo, ViewModel gọi:

- `listenToTables()`
- `listenToMenuItems()`
- `listenToActiveOrders()`
- `listenToClosedOrders()`
- `listenToStaffNotifications()`
- `seedDatabaseIfEmpty()`
- `backfillImageUrls()`

### Seed dữ liệu

Nếu Firestore chưa có đủ dữ liệu, app tự thêm:

- 15 bàn mặc định.
- Một số món mẫu theo danh mục.

Sau đó `backfillImageUrls()` cập nhật URL ảnh cho các món nếu thiếu hoặc không khớp mapping trong ViewModel.

### Mở bàn

Các hàm chính:

- `ensureOrderForTable(tableId)`: tạo order `Open` nếu bàn chưa có order mở.
- `createOrderForTable(tableId)`: tạo order mới và chuyển bàn sang `Đang phục vụ`.
- `openTableForCustomer(tableId)`: nhân viên mở bàn, sinh `accessCode`, cập nhật bàn và tạo order.

### Gọi món

`addMenuItemToOrder(orderId, menuItem)`:

- Chỉ thêm nếu món còn bán.
- Nếu món đã có trong giỏ với trạng thái `Cart`, tăng `quantity`.
- Nếu chưa có, thêm `OrderItem(status = "Cart")`.
- Cập nhật Firestore bằng transaction.

`removeOrderItem(orderId, menuItemId)`:

- Chỉ xóa món đang ở trạng thái `Cart`.

### Gửi bếp

`sendOrderToKitchen(orderId)`:

- Tìm các món `Cart`.
- Chuyển toàn bộ `Cart` sang `Pending`.
- Cập nhật Firestore.

`listenToActiveOrders()` theo dõi số món `Pending`; khi tăng, ViewModel tạo thông báo và emit `newOrderEvent` để nhân viên/bếp nhận được.

### Bếp xử lý món

`updateOrderItemStatus(orderId, itemIndex, newStatus)`:

- Chuyển món qua `Pending -> Cooking -> Done`.
- Khi món chuyển sang `Done`, app gửi FCM cho bàn khách nếu bàn có `fcmToken`.
- Đồng thời `dishReadyEvent` được emit để màn khách hiển thị snackbar/local notification.

### Thanh toán

`notifyPaymentSuccess(tableId, amount)`:

- Khách bấm “Tôi đã thanh toán”.
- ViewModel tạo thông báo trong `staff_notifications`.
- Gửi FCM cho các thiết bị nhân viên trong `staff_devices`.

`closeOrder(orderId, tableId)`:

- Nhân viên xác nhận thanh toán.
- Order chuyển sang `Closed`.
- Tính lại `totalAmount`.
- Bàn về `Trống`, xóa `accessCode` và `fcmToken`.

## 7. Điều hướng

File: `app/src/main/java/com/example/androidbtl/ui/navigation/AppNavigation.kt`

App bắt đầu tại `login`.

### Đăng nhập khách

Khách có thể:

- Nhập số bàn thủ công.
- Quét QR chứa deep link dạng:

```text
androidbtl://table/{tableId}?code={accessCode}
```

Nếu bàn `Đã đặt`, app từ chối. Nếu bàn `Đang phục vụ`, khách chỉ được vào lại khi có QR access code. Sau khi vào bàn, app lấy FCM token của thiết bị và lưu vào document bàn.

### Đăng nhập nhân viên

Nhân viên đăng nhập bằng tài khoản hardcode:

```text
admin / 123
```

Sau khi đăng nhập, app đăng ký FCM token vào `staff_devices` và vào khu vực nhân viên.

### Route chính

Nhân viên:

- `tables`: sơ đồ bàn.
- `staff_pos/{tableId}`: gọi món cho bàn.
- `kds`: màn hình bếp.
- `staff_menu`: quản lý thực đơn.
- `billing`: xác nhận thanh toán.
- `revenue`: doanh thu hôm nay.

Khách hàng:

- `cus_home`: trang chủ.
- `cus_menu`: gọi món.
- `cus_booking/{tableId}`: giỏ hàng.
- `cus_bill/{tableId}`: hóa đơn/thanh toán.
- `cus_profile`: tài khoản.
- `cus_offers`: ưu đãi hiện tại.

`AppBottomNavBar` chọn tab theo vai trò. Với nhân viên, các tab nghiệp vụ được render thông qua route `tables` và biến `staffTabRoute`.

## 8. Các màn hình chính

### `LoginScreen.kt`

- Có 2 tab: khách hàng và nhân viên.
- Khách nhập số bàn hoặc bấm quét QR.
- QR được đọc bằng CameraX + ML Kit.
- Nhân viên dùng `admin / 123`.
- Chưa dùng Firebase Auth.

### `HomeScreen.kt`

Trang chủ khách:

- Hiển thị lời chào theo giờ và số bàn.
- Hiển thị tiến độ món: đã gửi, đang nấu, sẵn sàng.
- Có banner `hotpot_banner.jpg`.
- Có lối tắt: thực đơn, thanh toán, gọi phục vụ.
- Hiển thị món xu hướng từ `topSellingItems`.

### `POSOrderScreen.kt`

Màn gọi món dùng cho cả khách và nhân viên:

- Lấy món realtime từ Firestore.
- Lọc theo danh mục.
- Tìm kiếm theo tên, danh mục, mô tả.
- Chỉ thêm được món còn bán.
- Món mới vào giỏ với trạng thái `Cart`.
- Thanh dưới hiển thị số món trong giỏ và nút `GỬI BẾP`.

### `BookingScreen.kt`

Giỏ hàng của khách:

- Chỉ hiển thị món `Cart`.
- Cho xóa món khỏi giỏ.
- Tính tổng tạm thời.
- Gửi toàn bộ món trong giỏ xuống bếp.

### `BillScreen.kt`

Hóa đơn khách:

- Chỉ tính các món `Pending` hoặc `Done`, không tính món còn trong `Cart`.
- Gộp món giống nhau theo `menuItemId`.
- Tạo QR VietQR để chuyển khoản.
- Có nút “Tôi đã thanh toán” để báo nhân viên kiểm tra.

### `OffersScreen.kt`

Màn ưu đãi khách hàng:

- Hiển thị danh sách ưu đãi tĩnh trong code.
- Mỗi ưu đãi có tiêu đề, mô tả, mã khuyến mãi và hạn dùng.
- Hiện đã được nối vào bottom navigation của khách.

### `KitchenDisplayScreen.kt`

Màn bếp:

- Chia món theo 3 nhóm: `Pending`, `Cooking`, `Done`.
- Bếp bấm để chuyển trạng thái món.
- Có chuông thông báo nhân viên.
- Nhận snackbar/local notification khi có món mới.

### `TableManagementScreen.kt`

Sơ đồ bàn cho nhân viên:

- Hiển thị bàn dạng grid.
- Thêm, sửa, xóa bàn.
- Chỉ xóa khi bàn `Trống`.
- Đặt/hủy đặt bàn.
- Mở bàn cho khách và tạo QR truy cập bàn.
- Tạo QR thanh toán cho bàn đang có order.
- Có chuông thông báo và đăng xuất.

### `StaffMenuScreen.kt`

Quản lý thực đơn:

- Xem danh sách món.
- Thêm, sửa, xóa món.
- Bật/tắt `available`.
- Nếu URL ảnh trống khi thêm món, ViewModel tự chọn ảnh theo tên hoặc danh mục.

Danh mục đang dùng:

- `Thịt bò`
- `Thịt lợn`
- `Hải sản`
- `Rau nấm`
- `Ăn kèm`
- `Tráng miệng`
- `Nước lẩu`

### `BillingScreen.kt`

Màn xác nhận thanh toán:

- Hiển thị các order `Open` có món và có tổng tiền.
- Hiển thị tổng tiền đang chờ, số hóa đơn, số món.
- Nhân viên kiểm tra chuyển khoản bên ngoài app rồi bấm xác nhận.
- Khi xác nhận, gọi `closeOrder()`.

### `RevenueScreen.kt`

Báo cáo doanh thu:

- Nguồn dữ liệu là `closedOrders`.
- Code hiện tại chỉ lọc và hiển thị **doanh thu hôm nay**.
- Hiển thị tổng doanh thu hôm nay, số đơn đã hoàn tất và danh sách giao dịch hôm nay.

### `ProfileScreen.kt`

Màn tài khoản khách:

- Hiển thị khách hàng theo số bàn.
- Cho bật/tắt chế độ sáng/tối.
- Cho đăng xuất khỏi bàn.

### `MenuScreen.kt`

File này chứa thực đơn tĩnh `menuData` và giao diện menu riêng, nhưng navigation hiện tại không dùng màn này cho luồng chính. Luồng gọi món thực tế đang dùng `POSOrderScreen` với dữ liệu Firestore.

## 9. Component dùng lại

### `BottomNavBar.kt`

Định nghĩa sealed class `Screen` và `AppBottomNavBar`.

Tab khách:

- Trang chủ
- Thực đơn
- Ưu đãi
- Hóa đơn
- Tài khoản

Tab nhân viên:

- Bàn
- Bếp
- Món ăn
- Thu ngân
- Doanh thu

Tab bếp có badge số món `Pending`.

### `CommonComponents.kt`

Chứa:

- `AsyncFoodImage`
- `EmptyState`
- `ShimmerBox`
- `MenuItemSkeleton`
- `DishCardSkeleton`
- `StaffNotificationBell`

### `QrPaymentDialog.kt`

Hiển thị QR thanh toán:

- Gọi `VietQrGenerator.build(...)`.
- Render QR thành bitmap.
- Hiển thị ngân hàng, số tài khoản, chủ tài khoản, số tiền và nội dung chuyển khoản.

## 10. VietQR

File: `app/src/main/java/com/example/androidbtl/data/VietQrGenerator.kt`

Cấu hình hiện tại:

```kotlin
object VietQrConfig {
    const val MBBANK_BIN = "970422"
    const val BANK_ACCOUNT_NUMBER = "0823468986"
    const val BANK_ACCOUNT_NAME = "NGUYEN NGOC HIEP"
    const val BANK_DISPLAY_NAME = "MB Bank"
}
```

`VietQrGenerator.build(...)` tạo payload theo cấu trúc TLV:

- `00`: payload format.
- `01`: QR động.
- `38`: thông tin ngân hàng và tài khoản.
- `52`: mã ngành nhà hàng `5812`.
- `53`: tiền tệ VND `704`.
- `54`: số tiền.
- `58`: quốc gia `VN`.
- `59`: chủ tài khoản.
- `62`: nội dung chuyển khoản, ví dụ `BAN1`.
- `63`: CRC checksum.

`sanitize()` bỏ dấu tiếng Việt và chuẩn hóa chữ hoa. `crc16Ccitt()` tính checksum cuối payload.

## 11. Thông báo

App có 2 lớp thông báo:

- **Local notification** qua `NotificationHelper`, dùng khi app nhận event trong phiên hiện tại hoặc khi FCM đến lúc app đang mở.
- **Push notification** qua `FcmSender`, dùng FCM HTTP v1.

Các trường hợp chính:

- Khách gọi phục vụ: `callStaff()` tạo thông báo cho nhân viên và gửi FCM cho staff.
- Khách báo đã thanh toán: `notifyPaymentSuccess()` tạo thông báo target `billing` và gửi FCM cho staff.
- Bếp hoàn tất món: `updateOrderItemStatus(..., "Done")` gửi FCM cho bàn khách.
- Có món mới xuống bếp: `listenToActiveOrders()` tạo notification local trong state và emit `newOrderEvent`.

Lưu ý: repo có `app/src/main/assets/service_account.json`; đây là file nhạy cảm nếu triển khai thật và không nên đưa lên public repository.

## 12. Theme và giao diện

Các file theme:

- `Color.kt`: màu thương hiệu và màu hành động.
- `Theme.kt`: light/dark color scheme.
- `ThemeState.kt`: `LocalThemeIsDark`.
- `Type.kt`: typography.

Màu chính:

- `BrandYellow`: màu thương hiệu.
- `ActionRed`: xóa/cảnh báo.
- `ActionGreen`: xác nhận.

App hỗ trợ dark mode qua màn Profile.

## 13. Luồng nghiệp vụ tổng thể

### Luồng khách gọi món

1. Khách vào app.
2. Nhập số bàn hoặc quét QR mở bàn.
3. App tạo/lấy order `Open` cho bàn.
4. App lưu FCM token của thiết bị vào document bàn.
5. Khách xem trang chủ và vào thực đơn.
6. Khách thêm món vào giỏ, món ở trạng thái `Cart`.
7. Khách gửi bếp, món chuyển sang `Pending`.
8. Bếp nhận món realtime.
9. Bếp chuyển món `Pending -> Cooking -> Done`.
10. Khách nhận thông báo khi món xong.
11. Khách xem hóa đơn, tạo VietQR và chuyển khoản.
12. Khách bấm “Tôi đã thanh toán”.
13. Nhân viên kiểm tra và xác nhận.
14. Order chuyển `Closed`, bàn về `Trống`.
15. Nếu khách đã báo thanh toán và bàn được dọn, app tự đăng xuất khách sau 30 giây.

### Luồng nhân viên

1. Nhân viên đăng nhập `admin / 123`.
2. App đăng ký FCM token vào `staff_devices`.
3. Nhân viên quản lý sơ đồ bàn.
4. Nhân viên có thể mở bàn, đặt bàn, tạo QR mở bàn.
5. Nhân viên gọi món thay khách nếu cần.
6. Bếp xử lý món trên KDS.
7. Thu ngân xác nhận thanh toán.
8. Nhân viên xem doanh thu hôm nay.

## 14. Điểm nổi bật khi báo cáo

- Phân vai khách hàng/nhân viên rõ ràng.
- Firestore realtime cho bàn, món, order và thông báo.
- Kiến trúc MVVM, UI Compose đọc `StateFlow`.
- Pipeline món rõ ràng: `Cart -> Pending -> Cooking -> Done`.
- Có QR mở bàn bằng deep link và access code.
- Có VietQR thanh toán tự tạo bằng TLV + CRC16.
- Có FCM cho thông báo khách gọi nhân viên, báo thanh toán và món hoàn tất.
- Có quản lý bàn, quản lý món, KDS, thu ngân và doanh thu.
- Có seed dữ liệu mẫu để chạy lần đầu.
- Có dark mode.

## 15. Hạn chế hiện tại

- Đăng nhập nhân viên hardcode `admin / 123`, chưa dùng Firebase Auth.
- Khách không có tài khoản riêng; định danh theo bàn và QR/access code.
- Xác nhận thanh toán vẫn thủ công, app chưa kiểm tra giao dịch ngân hàng tự động.
- `RevenueScreen` hiện chỉ có doanh thu hôm nay, chưa có bộ lọc 7 ngày/30 ngày.
- `MenuScreen.kt` là màn thực đơn tĩnh cũ/chưa nối vào luồng chính.
- Chưa thấy Firestore Security Rules trong repo.
- `service_account.json` nằm trong assets là rủi ro bảo mật nếu dùng môi trường thật.

## 16. Gợi ý trình bày báo cáo

Thứ tự trình bày hợp lý:

1. Mục tiêu: số hóa gọi món và vận hành nhà hàng lẩu.
2. Công nghệ: Kotlin, Compose, Firestore, FCM, CameraX/ML Kit, VietQR.
3. Kiến trúc: UI Compose -> Navigation -> PosViewModel -> Firestore/FCM.
4. Database: `tables`, `menu_items`, `orders`, `staff_notifications`, `staff_devices`.
5. Demo khách: mở bàn bằng QR, gọi món, gửi bếp, nhận thông báo, tạo QR thanh toán.
6. Demo nhân viên: quản lý bàn, bếp, thực đơn, thu ngân, doanh thu.
7. Điểm nổi bật: realtime, QR mở bàn, VietQR, FCM, pipeline trạng thái món.
8. Hạn chế và hướng phát triển.
