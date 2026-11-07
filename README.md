# Restaurant Ordering System

Bài tập lớn môn Lập trình hướng đối tượng (INT1332_CLC, PTIT), **chủ đề 8: hệ thống gọi món nhà hàng**.
Ứng dụng Java có hai giao diện dùng chung một tầng nghiệp vụ: console và Swing. Chức năng: mở bàn, gọi món, sửa món, thanh toán có giảm giá, hủy đơn, quản lý thực đơn và thống kê món bán chạy.

Thiết kế chi tiết: [`docs/superpowers/specs/2026-11-06-restaurant-ordering-design.md`](docs/superpowers/specs/2026-11-06-restaurant-ordering-design.md).

## Yêu cầu

- JDK 17 trở lên
- Maven 3.9 (NetBeans, IntelliJ, Eclipse đều có sẵn Maven)
- Lần build đầu cần có mạng để Maven tải JUnit và các plugin
- Nên chạy `mvn verify` một lần trên đúng máy và đúng JDK sẽ dùng khi demo

## Chạy bằng VS Code (khuyên dùng khi demo)

**Cách nhanh nhất:** trong khung Explorer bên trái, mở một trong 3 file sau ở `src/main/java/restaurant/`, rồi bấm nút **Run** (▶) ở góc trên bên phải hoặc chữ **Run** phía trên hàm `main`:

| File | Chạy gì |
|---|---|
| `ChayGiaoDien.java` | Giao diện cửa sổ (Swing) |
| `ChayConsole.java` | Giao diện dòng lệnh |
| `ChayDemo.java` | Console tự chạy kịch bản demo |

Ngoài VS Code (Ubuntu): chuột phải `chay-giao-dien.sh`, `chay-console.sh` hoặc `chay-demo.sh` → **Run as a Program**.

Hoặc dùng cấu hình có sẵn:

1. Cài JDK 17 trở lên và extension **Extension Pack for Java** (VS Code sẽ tự gợi ý khi mở thư mục).
2. **File → Open Folder** → chọn thư mục `restaurant-ordering`. Đợi góc dưới báo Java đã tải xong dự án (lần đầu cần mạng để tải JUnit).
3. Bấm biểu tượng **Run and Debug** (Ctrl+Shift+D) ở thanh bên trái, chọn một cấu hình rồi bấm nút ▶ (hoặc F5):
   - **1. Giao diện Swing**: mở cửa sổ quản lý
   - **2. Console**: chạy menu chữ trong terminal của VS Code
   - **3. Demo tự động (console)**: app tự chạy hết `scripts/demo-input.txt`
4. Chạy test: bấm biểu tượng ống nghiệm (Testing) ở thanh bên trái → Run All Tests.

Nếu terminal trên Windows hiện sai dấu tiếng Việt, gõ `chcp 65001` trong terminal rồi chạy lại.

## Chạy

**Trong IDE:** mở thư mục dự án như một dự án Maven, chạy lớp `restaurant.Main`.

**Dòng lệnh:**

```bash
mvn verify                                      # biên dịch, chạy toàn bộ test, đo độ phủ
java -jar target/restaurant-ordering.jar        # chạy giao diện console
java -jar target/restaurant-ordering.jar --gui  # chạy giao diện Swing
java -jar target/restaurant-ordering.jar --demo # console tự chạy kịch bản demo
```

Trong IDE, muốn mở giao diện Swing thì thêm tham số `--gui` vào cấu hình chạy của `restaurant.Main`.

Giao diện Swing: chọn bàn ở bảng bên trái, chọn món ở bảng giữa, rồi bấm các nút ở dưới.

![Giao diện Swing](docs/images/swing-don-ban-3.png)

Báo cáo độ phủ test nằm ở `target/site/jacoco/index.html` sau khi chạy `mvn verify`.

**Chạy kịch bản demo tự động** (dùng khi thuyết trình hoặc để kiểm tra nhanh):

```bash
java -jar target/restaurant-ordering.jar < scripts/demo-input.txt
```

Xóa thư mục `data/` trước khi chạy nếu muốn bắt đầu lại từ thực đơn mẫu.

## Dữ liệu

| File | Nội dung |
|---|---|
| `data/menu.csv` | Thực đơn. Tự tạo từ thực đơn mẫu nếu chưa có. Sửa tay được, nhưng phải lưu dạng UTF-8 (trong Excel chọn "CSV UTF-8"), phân cách bằng `;`. Dòng sai bị bỏ qua khi đọc nhưng vẫn giữ nguyên trong file |
| `data/hoa-don.txt` | Mọi hóa đơn đã thanh toán, ghi nối tiếp |

Đơn đang mở và trạng thái bàn không được lưu khi tắt app.

## Tiếng Việt trên cửa sổ cmd của Windows

App in ra UTF-8. Nếu cmd hiện sai dấu, chạy `chcp 65001` trước khi chạy app. Trong IntelliJ và NetBeans thường hiện đúng sẵn.

## Cấu trúc mã nguồn

| Gói | Vai trò |
|---|---|
| `restaurant.model` | Các lớp dữ liệu: `MenuItem` và lớp con (`Food`, `Drink`, `Combo`), `MenuItemFactory`, `Table`, `Order`, `OrderItem`, `Bill` |
| `restaurant.pricing` | Tính hóa đơn; các cách giảm giá theo pattern Strategy |
| `restaurant.repository` | Lưu trữ: interface `Repository<T, ID>` và `BillArchive`, bản bộ nhớ, bản file |
| `restaurant.service` | Nghiệp vụ: thực đơn, bàn, đơn |
| `restaurant.exception` | Lỗi nghiệp vụ, đều kế thừa `RestaurantException` |
| `restaurant.ui` | Giao diện console (`ConsoleMenu`, `InputReader`) và Swing (`ui.swing.MainWindow`) |
| `restaurant.common` | Dùng chung: định dạng tiền, định dạng hóa đơn |

## Các mốc trong git

| Tag | Nội dung |
|---|---|
| `milestone-4` | Bản cài đặt lõi (Milestone 4) |
| `milestone-5` | Refactor theo SOLID: Strategy, Factory, BillArchive, InputReader; thêm Combo (Composite) |

| `final` | Thêm giao diện Swing dùng chung service với console |
| `v1.0` | Bản nộp: bản `final` cộng các bản sửa lỗi sau khi review code |
| `v1.1` | Thêm cấu hình chạy cho VS Code và tham số `--demo` |
| `v1.2` | Thêm file bấm là chạy: `ChayGiaoDien`, `ChayConsole`, `ChayDemo` và các file `.sh` |

Xem các thay đổi khi refactor: `git diff milestone-4 milestone-5 -- src/main`.

Chứng minh thêm giao diện Swing không phải sửa tầng nghiệp vụ (lệnh này in ra rỗng):

```bash
git diff milestone-5 final --stat -- src/main/java/restaurant/model src/main/java/restaurant/pricing src/main/java/restaurant/repository src/main/java/restaurant/service
```
