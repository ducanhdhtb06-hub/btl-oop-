# Restaurant Ordering System

Bài tập lớn môn Lập trình hướng đối tượng (INT1332_CLC, PTIT), **chủ đề 8: hệ thống gọi món nhà hàng**.
Ứng dụng Java chạy trên console: mở bàn, gọi món, sửa món, thanh toán có giảm giá, hủy đơn, quản lý thực đơn và thống kê món bán chạy.

Thiết kế chi tiết: [`docs/superpowers/specs/2026-11-06-restaurant-ordering-design.md`](docs/superpowers/specs/2026-11-06-restaurant-ordering-design.md).

## Yêu cầu

- JDK 17 trở lên
- Maven 3.9 (NetBeans, IntelliJ, Eclipse đều có sẵn Maven)
- Lần build đầu cần có mạng để Maven tải JUnit và các plugin

## Chạy

**Trong IDE:** mở thư mục dự án như một dự án Maven, chạy lớp `restaurant.Main`.

**Dòng lệnh:**

```bash
mvn verify                                # biên dịch, chạy toàn bộ test, đo độ phủ
java -jar target/restaurant-ordering.jar  # chạy app
```

Báo cáo độ phủ test nằm ở `target/site/jacoco/index.html` sau khi chạy `mvn verify`.

**Chạy kịch bản demo tự động** (dùng khi thuyết trình hoặc để kiểm tra nhanh):

```bash
java -jar target/restaurant-ordering.jar < scripts/demo-input.txt
```

Xóa thư mục `data/` trước khi chạy nếu muốn bắt đầu lại từ thực đơn mẫu.

## Dữ liệu

| File | Nội dung |
|---|---|
| `data/menu.csv` | Thực đơn. Tự tạo từ thực đơn mẫu nếu chưa có. Sửa tay được (UTF-8, phân cách bằng `;`) |
| `data/hoa-don.txt` | Mọi hóa đơn đã thanh toán, ghi nối tiếp |

Đơn đang mở và trạng thái bàn không được lưu khi tắt app.

## Tiếng Việt trên cửa sổ cmd của Windows

App in ra UTF-8. Nếu cmd hiện sai dấu, chạy `chcp 65001` trước khi chạy app. Trong IntelliJ và NetBeans thường hiện đúng sẵn.

## Cấu trúc mã nguồn

| Gói | Vai trò |
|---|---|
| `restaurant.model` | Các lớp dữ liệu: `MenuItem` và lớp con, `Table`, `Order`, `OrderItem`, `Bill` |
| `restaurant.pricing` | Tính hóa đơn và giảm giá |
| `restaurant.repository` | Lưu trữ: interface `Repository<T, ID>`, bản bộ nhớ, bản file CSV |
| `restaurant.service` | Nghiệp vụ: thực đơn, bàn, đơn |
| `restaurant.exception` | Lỗi nghiệp vụ, đều kế thừa `RestaurantException` |
| `restaurant.ui` | Giao diện console |
| `restaurant.common` | Tiện ích dùng chung (định dạng tiền) |

## Các mốc trong git

| Tag | Nội dung |
|---|---|
| `milestone-4` | Bản cài đặt lõi (Milestone 4) |
