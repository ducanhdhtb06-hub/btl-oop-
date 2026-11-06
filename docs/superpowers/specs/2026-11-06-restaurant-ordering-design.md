# Thiết kế: Restaurant Ordering System (Chủ đề 8)

Ngày: 2026-11-06 · Môn: Lập trình hướng đối tượng (INT1332_CLC, PTIT) · Trạng thái: chờ duyệt

## 1. Mục tiêu và tiêu chí thành công

App Java chạy trên console để nhân viên nhà hàng mở bàn, gọi món, thanh toán và quản lý thực đơn. App là bài tập lớn của nhóm 3–4 người, được chấm theo 10 tiêu chí (mỗi tiêu chí 10 điểm) trong `Introduction.pdf`.

Coi là xong khi:

- Chạy trọn luồng: mở bàn → gọi món → thanh toán có giảm giá → in và lưu hóa đơn.
- Mỗi tiêu chí chấm có ít nhất một chỗ trong code chỉ ra được (bảng ở mục 9).
- Toàn bộ test JUnit qua (`mvn test`), kịch bản demo chạy thành công.
- Có ba mốc code thật trong git: bản Milestone 4 (đơn giản), bản Milestone 5 (đã refactor, có ví dụ trước và sau cho báo cáo) và bản Final (thêm Swing dùng chung service).

## 2. Phạm vi

Trong phạm vi: các chức năng ở mục 4, lưu thực đơn và hóa đơn ra file. Ở bản Final có thêm giao diện Swing chạy chung tầng service với console (mục 8). Nếu hết thời gian thì bỏ phần Swing, app console vẫn hoàn chỉnh.

Ngoài phạm vi (cố ý không làm): đăng nhập và phân quyền, cơ sở dữ liệu, lưu đơn đang mở khi tắt app, xóa món khỏi thực đơn, VAT và phí phục vụ, đa luồng. Báo cáo có mục giải thích vì sao không dùng Singleton, Observer và thread.

Ràng buộc kỹ thuật: Java 17 (biên dịch với `--release 17`), Maven, JUnit 5, JaCoCo. Không dùng thư viện ngoài cho code chính. Đọc ghi file và console dùng UTF-8.

## 3. Tác nhân

Không có đăng nhập, tác nhân chỉ dùng để phân tích use case.

- **Nhân viên phục vụ:** xem thực đơn, xem bàn, mở bàn, gọi món, sửa món, xem đơn.
- **Thu ngân:** thanh toán, hủy đơn.
- **Quản lý:** quản lý thực đơn, xem thống kê.

## 4. Menu console

| Số | Chức năng | Tác nhân |
|---|---|---|
| 1 | Xem thực đơn (tất cả / món ăn / đồ uống / combo / món chay) | Phục vụ |
| 2 | Xem tình trạng bàn | Phục vụ |
| 3 | Mở bàn | Phục vụ |
| 4 | Gọi món | Phục vụ |
| 5 | Sửa số lượng hoặc bớt món | Phục vụ |
| 6 | Xem đơn của bàn (có tạm tính) | Phục vụ |
| 7 | Thanh toán (chọn cách giảm giá) | Thu ngân |
| 8 | Hủy đơn (có hỏi xác nhận) | Thu ngân |
| 9 | Quản lý thực đơn: thêm món, sửa giá, đánh dấu hết hoặc còn món | Quản lý |
| 10 | Thống kê món bán chạy | Quản lý |
| 0 | Thoát | |

## 5. Các lớp

Gói gốc: `restaurant`. `Main` nằm ở gói gốc, tự tạo các đối tượng và truyền phụ thuộc vào (giống mục "Wiring in main()" ở Chapter 10).

### 5.1 `restaurant.model`

- `Identifiable<ID>` (interface): `ID getId()`.
- `MenuItem` (lớp trừu tượng, implements `Identifiable<String>`): `id`, `name`, `available`. Phương thức trừu tượng: `long getPrice()`, `ItemType getType()`, `String describe()`, `String toCsvRow()`. `equals`/`hashCode` theo `id`.
- `SimpleMenuItem` (lớp trừu tượng, extends `MenuItem`): thêm `price` và `changePrice(long)`. Là lớp cha của món đơn.
- `Food` (extends `SimpleMenuItem`): thêm `vegetarian`.
- `Drink` (extends `SimpleMenuItem`): thêm `volumeMl`.
- `Combo` (extends `MenuItem`, pattern **Composite**): giữ `List<MenuItem> components` được truyền vào từ bên ngoài (aggregation, ít nhất 1 món) và `discountPercent` (0–100). `getPrice()` = tổng giá các món thành phần, trừ % combo, làm tròn đến đồng. `isAvailable()` chỉ đúng khi bản thân combo và mọi món thành phần đều còn. Combo có thể chứa combo khác.
- `MenuItemFactory` (pattern **Factory**): `create(ItemType, ...)` tạo đúng lớp con. Có ở bản Milestone 5.
- `ItemType` (enum): `FOOD`, `DRINK`, `COMBO`.
- `Table` (implements `Identifiable<Integer>`): `number`, `seats`, `TableStatus status`. Có `occupy()` và `release()`.
- `TableStatus` (enum): `FREE`, `OCCUPIED`.
- `Order` (implements `Identifiable<Integer>`): `id`, `Table table`, `createdAt`, `OrderStatus status`, `List<OrderItem> items` (private final). Chỉ `Order` tạo `OrderItem` (composition). Phương thức:
  - `addItem(MenuItem)` và `addItem(MenuItem, int)` (overloading)
  - `updateQuantity(String itemId, int)`, `removeItem(String itemId)`
  - `getItems()` trả về danh sách chỉ đọc
  - `subtotal()`, `isEmpty()`, `markPaid()`, `cancel()`
- `OrderItem`: `MenuItem item`, `unitPrice` (chốt lúc gọi món), `quantity`, `lineTotal()`. Constructor và hàm đổi số lượng chỉ dùng được trong gói `model`.
- `OrderStatus` (enum): `OPEN`, `PAID`, `CANCELLED`.
- `Bill` (record): `orderId`, `tableNumber`, `issuedAt`, `List<BillLine> lines`, `subtotal`, `discountLabel`, `discount`, `total`.
- `BillLine` (record): `itemName`, `quantity`, `unitPrice`, `lineTotal`.

### 5.2 `restaurant.pricing`

- `DiscountStrategy` (functional interface, pattern **Strategy**): `long discountFor(long subtotal)`, `default String label()`. Dùng được cả lambda, ví dụ `s -> Math.min(s, 50_000)`.
- `NoDiscount`, `PercentageDiscount(percent 0–100)`, `FixedAmountDiscount(amount ≥ 0)`. Tiền giảm không bao giờ vượt tạm tính. Tham số sai thì báo `IllegalArgumentException`.
- `BillCalculator`: `Bill calculate(Order, DiscountStrategy, LocalDateTime)`. Chỉ tính tiền, không làm việc khác.

### 5.3 `restaurant.repository`

- `Repository<T extends Identifiable<ID>, ID>` (interface dùng generic có giới hạn kiểu): `List<T> findAll()`, `Optional<T> findById(ID)`, `void save(T)`.
- `InMemoryRepository<T, ID>`: lưu bằng `LinkedHashMap`, dùng cho bàn, đơn và trong test.
- `FileMenuRepository` (implements `Repository<MenuItem, String>`): đọc `data/menu.csv` khi khởi tạo, ghi lại cả file mỗi lần `save`.
- `BillArchive` (interface): `void archive(Bill) throws IOException`.
- `FileBillArchive`: ghi thêm hóa đơn vào cuối `data/hoa-don.txt`.

Định dạng `menu.csv` (UTF-8, phân cách bằng `;`, dòng đầu là tiêu đề):

```
type;id;name;value;available;detail
FOOD;F01;Phở bò;55000;true;false        <- value = giá, detail = món chay
DRINK;D01;Trà đá;5000;true;300          <- value = giá, detail = dung tích ml
COMBO;C01;Combo trưa;10;true;F02|D01    <- value = % giảm, detail = mã món thành phần
```

Các món thành phần của combo phải nằm ở dòng phía trên combo đó. Thiếu file thì app tự tạo thực đơn mẫu khoảng 10 món. Dòng sai định dạng thì bỏ qua và báo số dòng.

### 5.4 `restaurant.service`

- `MenuService`: liệt kê và lọc thực đơn bằng stream, tìm món, thêm món ăn hoặc đồ uống (tự sinh mã theo loại, ví dụ `F07`), sửa giá (chỉ cho món đơn), bật tắt còn món. Combo chỉ khai báo trong `menu.csv`, không thêm qua menu console.
- `TableService`: liệt kê bàn, tìm bàn. Lúc khởi động có 10 bàn khai báo sẵn trong code.
- `OrderService`: phụ thuộc `Repository<Order,Integer>`, `TableService`, `MenuService`, `BillCalculator`, `BillArchive`, `Clock`. Mã đơn do service tự đếm, không dùng biến static. Phương thức:
  - `openOrder(tableNo)`
  - `addItem(tableNo, itemId, qty)`, `updateQuantity(...)`, `removeItem(...)`
  - `currentOrder(tableNo)`
  - `checkout(tableNo, DiscountStrategy)` trả về `CheckoutResult(Bill bill, boolean archived)`
  - `cancel(tableNo)`
  - `bestSellers(int n)` trả về `List<ItemSales>`
- `CheckoutResult` và `ItemSales(itemName, quantity, revenue)` là record nằm trong gói `service`.

### 5.5 `restaurant.exception`

`RestaurantException` (checked, lớp gốc) và các lớp con: `MenuItemNotFoundException`, `ItemUnavailableException`, `InvalidQuantityException`, `TableNotFoundException`, `TableNotAvailableException`, `NoOpenOrderException`, `ItemNotInOrderException`, `EmptyOrderException`, `OrderClosedException`.

Lỗi do lập trình sai (tham số null, % giảm ngoài khoảng) dùng `IllegalArgumentException`.

### 5.6 `restaurant.ui`

- `ConsoleMenu`: vòng lặp menu, gọi service, bắt `RestaurantException` rồi in thông báo tiếng Việt.
- `InputReader`: đọc số nguyên trong khoảng và đọc chuỗi khác rỗng. Nhập sai thì hỏi lại. Nhận `Scanner` từ bên ngoài để test được.
- `BillPrinter`: định dạng hóa đơn và thực đơn thành chữ.
- `MoneyFormat`: `format(185000)` trả về `"185.000 đ"`, không phụ thuộc cài đặt ngôn ngữ của máy.
- `restaurant.ui.swing.MainWindow` (bản Final): một `JFrame` gồm bảng bàn (`JTable`), bảng thực đơn, bảng đơn của bàn đang chọn, và các nút Mở bàn, Gọi món, Bớt món, Thanh toán (chọn cách giảm giá bằng `JComboBox`), Hủy đơn. Lỗi nghiệp vụ hiện bằng `JOptionPane`. Chỉ gọi các service sẵn có, không thêm logic nghiệp vụ. `Main` nhận tham số `--gui` để mở Swing, mặc định mở console.

### 5.7 Quan hệ và multiplicity (dùng cho class diagram)

| Quan hệ | Loại | Multiplicity |
|---|---|---|
| `Order` ◆— `OrderItem` | Composition | 1 — 0..* |
| `OrderItem` → `MenuItem` | Association | 0..* — 1 |
| `Order` → `Table` | Association | 0..* — 1 |
| `Combo` ◇— `MenuItem` | Aggregation | 0..* — 1..* |
| `Food`, `Drink` —▷ `SimpleMenuItem` —▷ `MenuItem`; `Combo` —▷ `MenuItem` | Generalisation | |
| `PercentageDiscount` ..▷ `DiscountStrategy`; `InMemoryRepository` ..▷ `Repository`; `FileBillArchive` ..▷ `BillArchive` | Realisation | |
| `BillCalculator` - -> `DiscountStrategy`; `ConsoleMenu` - -> `OrderService` | Dependency | |

## 6. Quy tắc nghiệp vụ

1. Mỗi bàn có tối đa một đơn `OPEN`. Mở bàn đang có khách thì báo `TableNotAvailableException`.
2. Số lượng mỗi dòng từ 1 đến 99. Gọi lại món đã có thì cộng dồn số lượng và giữ đơn giá của dòng đã có. Cộng dồn vượt 99 thì từ chối.
3. Không gọi được món đã hết. Combo coi là hết nếu một món thành phần hết.
4. Đơn `PAID` hoặc `CANCELLED` thì không sửa được (`OrderClosedException`).
5. Đơn giá được chốt lúc gọi món. Đổi giá trên thực đơn không làm thay đổi đơn đang mở.
6. Thanh toán đơn rỗng thì báo `EmptyOrderException` và gợi ý dùng "Hủy đơn".
7. Tiền giảm theo % được làm tròn đến đồng (`Math.round`). Tổng tiền không bao giờ âm.
8. Thanh toán theo thứ tự: tính hóa đơn → đánh dấu `PAID` → trả bàn → lưu hóa đơn. Nếu lưu file lỗi thì `archived = false`, app cảnh báo nhưng việc thanh toán vẫn được ghi nhận.
9. Thống kê chỉ tính đơn `PAID` trong lần chạy hiện tại, sắp theo số lượng giảm dần, bằng nhau thì theo tên món.

## 7. Xử lý lỗi

- `ConsoleMenu` bắt mọi `RestaurantException`, in thông báo rồi quay lại menu. App không bị dừng vì người dùng nhập sai.
- `InputReader` xử lý chữ ở chỗ cần số và số ngoài khoảng bằng cách hỏi lại.
- Lỗi đọc ghi file dùng try-with-resources. Đọc thực đơn lỗi thì báo và dùng thực đơn mẫu. Ghi hóa đơn lỗi thì theo quy tắc 8.

## 8. Hai mốc code (để có câu chuyện refactor cho Milestone 5)

**Bản Milestone 4** (tag `milestone-4`): các lớp model, collections, exception, `Repository` và `InMemoryRepository`, `FileMenuRepository`, menu console. Cố ý viết đơn giản ở 4 chỗ:

1. Giảm giá dùng `enum DiscountType` và if-else bên trong `BillCalculator`.
2. `FileMenuRepository` tạo món bằng `switch` ngay trong hàm đọc file.
3. `OrderService` tự ghi hóa đơn ra file.
4. `ConsoleMenu` tự đọc input và tự định dạng hóa đơn.

Chưa có `Combo`.

**Bản Milestone 5** (tag `milestone-5`): refactor 4 chỗ trên, test cũ vẫn phải qua.

1. if-else → `DiscountStrategy` (nguyên tắc O, pattern Strategy).
2. `switch` → `MenuItemFactory` (pattern Factory).
3. Ghi file → `BillArchive` (nguyên tắc D và S).
4. Tách `InputReader` và `BillPrinter` khỏi `ConsoleMenu` (nguyên tắc S).

Sau đó thêm `Combo` (pattern Composite) mà không phải sửa `Order` hay `BillCalculator` (minh họa nguyên tắc O và L).

**Bản Final** (tag `final`): thêm `MainWindow` (Swing) dùng chung các service với `ConsoleMenu`, đúng thiết kế "ConsoleUI + SwingUI" ở cuối Chapter 10. Phải chứng minh được bằng `git diff` rằng không file nào trong `model`, `pricing`, `repository`, `service` bị sửa ở bước này (minh họa nguyên tắc O và D ở cấp kiến trúc).

## 9. Đối chiếu 10 tiêu chí chấm

| Tiêu chí | Bằng chứng |
|---|---|
| 1. Phân tích yêu cầu | Tác nhân, yêu cầu, use case: viết trong báo cáo dựa trên mục 3, 4, 6 |
| 2. UML và mô hình đối tượng | Class diagram đủ 6 loại quan hệ có multiplicity (mục 5.7), use case diagram, sequence diagram cho thanh toán |
| 3. Đóng gói, thiết kế lớp | Thuộc tính private và final, `Order` tự quản lý `OrderItem`, `getItems()` chỉ đọc, kiểm tra dữ liệu trong constructor |
| 4. Association, composition | `Order` ◆ `OrderItem`, `Combo` ◇ `MenuItem`, `OrderItem` → `MenuItem` |
| 5. Kế thừa, đa hình | Cây `MenuItem` ba cấp; `getPrice()`, `describe()`, `toCsvRow()` được viết lại; overloading `addItem` |
| 6. Interface, trừu tượng | `MenuItem` và `SimpleMenuItem` trừu tượng; interface `DiscountStrategy`, `Repository`, `BillArchive`, `Identifiable` |
| 7. Collections, generics, exception | `List`, `Map`, `Optional`, stream; `Repository<T extends Identifiable<ID>, ID>`; 9 exception tự định nghĩa |
| 8. SOLID, design patterns | Trước và sau ở mục 8; Strategy, Factory, Composite; console và Swing dùng chung service; mục "những gì cố ý không dùng" |
| 9. Chất lượng code, kiểm thử | Hơn 40 test JUnit, độ phủ JaCoCo từ 80% trở lên (không tính gói `ui`), build không cảnh báo, Javadoc cho lớp public, README |
| 10. Demo, thuyết trình | Kịch bản demo (mục 10.3), video dự phòng, slide đối chiếu tiêu chí, danh sách câu hỏi dự kiến |

## 10. Kiểm thử

### 10.1 Công cụ

JUnit 5 và `mvn test`, báo cáo độ phủ JaCoCo. Không dùng Mockito, chỉ dùng lớp giả tự viết.

### 10.2 Các nhóm test

- `OrderTest`: tạo dòng món, cộng dồn, giới hạn số lượng, bớt và sửa món, khóa đơn khi `PAID` hoặc `CANCELLED`, danh sách chỉ đọc, chốt đơn giá.
- `MenuItemTest`, `ComboTest`, `MenuItemFactoryTest`: `describe` khác nhau theo loại; giá combo và làm tròn; combo lồng combo; combo hết khi món thành phần hết; Factory tạo đúng lớp con.
- `DiscountStrategyTest`, `BillCalculatorTest`: 0%, 10% có làm tròn, % ngoài khoảng, giảm cố định lớn hơn tạm tính, giảm bằng lambda.
- `OrderServiceTest` (dùng `InMemoryRepository`, `Clock` cố định, `BillArchive` giả): các quy tắc 1 đến 9, trường hợp lưu hóa đơn lỗi, hủy đơn, thống kê.
- `FileMenuRepositoryTest`, `FileBillArchiveTest` (`@TempDir`): ghi rồi đọc lại, thiếu file, dòng hỏng, ghi tiếng Việt UTF-8.
- `InputReaderTest`, `MoneyFormatTest`: nhập sai thì hỏi lại; định dạng tiền.

### 10.3 Kịch bản demo (chạy tay, cũng dùng khi thuyết trình)

Xem thực đơn → mở bàn 3 → gọi phở ×2, combo ×1 → thử gọi món đã hết → thử nhập chữ vào chỗ cần số → sửa số lượng → xem đơn → thanh toán giảm 10% → xem hóa đơn trong file → thống kê.

## 11. Giới hạn đã biết

- Tắt app thì mất đơn đang mở và trạng thái bàn. Chỉ thực đơn và hóa đơn được lưu.
- Cửa sổ cmd của Windows có thể hiện sai tiếng Việt có dấu. Trong IntelliJ và NetBeans thì thường hiện đúng.
- Phần Swing không có test tự động. Chỉ kiểm tra bằng cách chạy tay theo kịch bản demo.

## 12. Phân công gợi ý (nhóm 4 người)

| Thành viên | Gói phụ trách | Phần báo cáo |
|---|---|---|
| 1 | `model` (kể cả `Combo`) | Tiêu chí 3, 4, 5 |
| 2 | `pricing`, `repository` | Tiêu chí 6, 7 |
| 3 | `service`, các test | Tiêu chí 8, 9 |
| 4 | `ui` (console và Swing) | Tiêu chí 1, 2, 10 |

Nhóm 3 người thì gộp việc của thành viên 2 và 3. Mỗi người tự commit phần mình làm, và trình bày phần đó khi thuyết trình.
