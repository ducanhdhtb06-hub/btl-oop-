# Restaurant Ordering System – Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ứng dụng Java gọi món nhà hàng (console, sau đó thêm Swing) cho bài tập lớn OOP chủ đề 8, với ba mốc git `milestone-4`, `milestone-5`, `final`.

**Architecture:** Bốn tầng giống case study cuối Chapter 10: `ui` (console, Swing) → `service` → interface `Repository`/`BillArchive` → cài đặt file và bộ nhớ. Model có cây `MenuItem` ba cấp, `Order` sở hữu `OrderItem` (composition). Bản Milestone 4 cố ý viết đơn giản ở 4 chỗ; bản Milestone 5 refactor 4 chỗ đó rồi mới thêm `Combo`.

**Tech Stack:** Java 17 (`--release 17`), Maven 3.9, JUnit Jupiter 5.10.2, JaCoCo 0.8.11, Swing (chỉ bản Final).

**Spec:** `docs/superpowers/specs/2026-11-06-restaurant-ordering-design.md`

## Global Constraints

- `maven.compiler.release` = 17. Main code dùng chỉ JDK; test chỉ dùng `junit-jupiter` 5.10.2 (không Mockito).
- Biên dịch với `-Xlint:all` và `failOnWarning=true`. Mọi exception khai báo `private static final long serialVersionUID = 1L;`.
- JaCoCo: line coverage của BUNDLE ≥ 0.80, loại trừ `restaurant/ui/**` và `restaurant/Main*`, chạy trong `mvn verify`.
- Mọi đọc ghi file và console dùng `StandardCharsets.UTF_8`.
- Tiền là `long` (đồng). Hiển thị bằng `MoneyFormat.format`: `185000` → `"185.000 đ"`, `-16500` → `"-16.500 đ"`, `0` → `"0 đ"`.
- Chữ hiện cho người dùng là tiếng Việt có dấu; tên lớp, phương thức, biến là tiếng Anh.
- Không có trạng thái `static` thay đổi được (bộ đếm mã đơn nằm trong `OrderService`).
- Số lượng mỗi dòng món: 1–99. Phần trăm giảm (đơn và combo): 0–100.
- Mỗi lớp/interface `public` có Javadoc ít nhất một câu. Test dùng `@DisplayName` tiếng Việt.
- Mỗi commit kết thúc bằng hai dòng attribution của phiên làm việc (`Co-Authored-By: ...`, `Claude-Session: ...`).
- Gói gốc `restaurant`; mã nguồn ở `src/main/java/restaurant/...`, test ở `src/test/java/restaurant/...` cùng gói với lớp được test.

## Review Focus

1. Luồng nhập bị đóng giữa chừng (Ctrl+D, hết file khi chạy bằng kịch bản) → app in "Tạm biệt" và thoát, không lặp vô hạn. Test ở Task 12 (`InputReaderTest`) và kiểm tra tay ở Task 8.
2. Tên món chứa `;` hoặc xuống dòng → bị từ chối ngay khi tạo, không làm hỏng `menu.csv`. Test ở Task 2.
3. Thư mục `data/` chưa tồn tại lần chạy đầu → tự tạo thư mục và thực đơn mẫu. Test ở Task 5.
4. `menu.csv` có hai dòng trùng mã (sửa tay) → giữ dòng đầu, cảnh báo dòng sau. Test ở Task 5.
5. Người dùng gõ `" 7 "` hoặc Enter dòng trống → hiểu là 7 / hỏi lại. Test ở Task 12.

---

## File Structure

```
pom.xml, README.md, .gitignore (target/, data/hoa-don.txt)
scripts/demo-input.txt                      kịch bản demo chạy bằng pipe (Task 8, cập nhật Task 13)
src/main/java/restaurant/
  Main.java                                 nối dây phụ thuộc; --gui ở bản Final
  common/  MoneyFormat, BillFormatter(M5)
  exception/ RestaurantException + 9 lớp con
  model/   Identifiable, ItemType, MenuItem, SimpleMenuItem, Food, Drink, Combo(M5), MenuItemFactory(M5),
           Table, TableStatus, Order, OrderItem, OrderStatus, Bill, BillLine
  pricing/ DiscountType(M4, xóa ở M5), BillCalculator, DiscountStrategy/NoDiscount/PercentageDiscount/FixedAmountDiscount(M5)
  repository/ Repository, InMemoryRepository, FileMenuRepository, SampleMenu, BillArchive(M5), FileBillArchive(M5)
  service/ MenuService, TableService, OrderService, CheckoutResult, ItemSales
  ui/      ConsoleMenu, InputReader(M5), InputClosedException(M5)
  ui/swing/ MainWindow (Final)
```

---

# PHẦN A — Bản Milestone 4

### Task 1: Khung Maven và `MoneyFormat`

**Files:**
- Create: `pom.xml`, `.gitignore`, `src/main/java/restaurant/common/MoneyFormat.java`
- Test: `src/test/java/restaurant/common/MoneyFormatTest.java`

**Interfaces:**
- Produces: `public final class MoneyFormat { public static String format(long amount) }`

- [ ] **Step 1: Viết `pom.xml`**

`groupId` `vn.edu.ptit`, `artifactId` `restaurant-ordering`, `version` `1.0`, `project.build.sourceEncoding` UTF-8, `maven.compiler.release` 17. Plugin: `maven-compiler-plugin` 3.13.0 (`<compilerArgs><arg>-Xlint:all</arg></compilerArgs>`, `<failOnWarning>true</failOnWarning>`), `maven-surefire-plugin` 3.2.5, `maven-jar-plugin` 3.4.1 (manifest `mainClass` = `restaurant.Main`, `finalName` `restaurant-ordering`), `jacoco-maven-plugin` 0.8.11 với execution `prepare-agent`, `report` (phase `verify`), `check` (phase `verify`, rule BUNDLE / LINE / COVEREDRATIO minimum `0.80`), `<excludes>` `restaurant/ui/**` và `restaurant/Main*`. Dependency `org.junit.jupiter:junit-jupiter:5.10.2` scope `test`.

- [ ] **Step 2: Viết test thất bại**

```java
@Test @DisplayName("Định dạng tiền kiểu Việt Nam")
void formatsVietnamese() {
    assertEquals("185.000 đ", MoneyFormat.format(185_000));
    assertEquals("1.000.000 đ", MoneyFormat.format(1_000_000));
    assertEquals("0 đ", MoneyFormat.format(0));
    assertEquals("5.000 đ", MoneyFormat.format(5_000));
    assertEquals("-16.500 đ", MoneyFormat.format(-16_500));
}
```

- [ ] **Step 3: Chạy và thấy lỗi** — `mvn -q test` → FAIL (không biên dịch được `MoneyFormat`).

- [ ] **Step 4: Cài đặt `MoneyFormat.format`** — tự nhóm 3 chữ số bằng dấu `.`, không dùng `Locale` mặc định của máy. Constructor private.

- [ ] **Step 5: Chạy test** — `mvn -q test` → PASS. (`mvn verify` chưa chạy được vì chưa đủ code để đạt độ phủ; chạy ở Task 8.)

- [ ] **Step 6: Commit** — `git add pom.xml .gitignore src && git commit -m "build: khung Maven và MoneyFormat"`

---

### Task 2: Exception và cây `MenuItem` (chưa có Combo)

**Files:**
- Create: `exception/RestaurantException.java` và 9 lớp con; `model/OrderStatus.java`, `model/Identifiable.java`, `model/ItemType.java`, `model/MenuItem.java`, `model/SimpleMenuItem.java`, `model/Food.java`, `model/Drink.java`
- Test: `model/MenuItemTest.java`

**Interfaces:**
- Produces:
  - `public class RestaurantException extends Exception { public RestaurantException(String message) }` — checked.
  - Lớp con (constructor → thông báo):
    - `MenuItemNotFoundException(String itemId)` → `"Không tìm thấy món " + itemId`
    - `ItemUnavailableException(String itemName)` → `"Món " + itemName + " đã hết"`
    - `InvalidQuantityException(int quantity)` → `"Số lượng phải từ 1 đến 99 (nhận được " + quantity + ")"`
    - `TableNotFoundException(int tableNo)` → `"Không có bàn số " + tableNo`
    - `TableNotAvailableException(int tableNo)` → `"Bàn " + tableNo + " đang có khách"`
    - `NoOpenOrderException(int tableNo)` → `"Bàn " + tableNo + " chưa có đơn đang mở"`
    - `ItemNotInOrderException(String itemId)` → `"Đơn không có món " + itemId`
    - `EmptyOrderException(int orderId)` → `"Đơn " + orderId + " chưa có món nào. Nếu khách không gọi món, hãy dùng Hủy đơn"`
    - `OrderClosedException(int orderId, OrderStatus status)` → `"Đơn " + orderId + " đã " + ("thanh toán" | "hủy") + ", không sửa được"` (tạo `OrderStatus` ở Task 3; ở task này viết luôn enum `OrderStatus { OPEN, PAID, CANCELLED }` trong `model`)
  - `public interface Identifiable<ID> { ID getId(); }`
  - `public enum ItemType { FOOD, DRINK }` (thêm `COMBO` ở Task 13)
  - `public abstract class MenuItem implements Identifiable<String>`: constructor `protected MenuItem(String id, String name, boolean available)`; `getId()`, `getName()`, `isAvailable()`, `setAvailable(boolean)`, `public boolean isVegetarian()` (mặc định `false`), `protected final boolean availableFlag()` (trả về cờ gốc, dùng cho CSV); trừu tượng `long getPrice()`, `ItemType getType()`, `String describe()`, `String toCsvRow()`; `equals`/`hashCode` theo `id`; `toString()` = `describe()`.
  - `public abstract class SimpleMenuItem extends MenuItem`: `protected SimpleMenuItem(String id, String name, long price, boolean available)`; `getPrice()`; `public void changePrice(long newPrice)`.
  - `public final class Food extends SimpleMenuItem`: `Food(String id, String name, long price, boolean available, boolean vegetarian)`.
  - `public final class Drink extends SimpleMenuItem`: `Drink(String id, String name, long price, boolean available, int volumeMl)`; `getVolumeMl()`; `isVegetarian()` trả về `true`.

- [ ] **Step 1: Viết test thất bại** (`MenuItemTest`)

```java
Food pho = new Food("F01", "Phở bò", 55_000, true, false);
Food goi = new Food("F04", "Gỏi cuốn chay", 35_000, true, true);
Drink tra = new Drink("D01", "Trà đá", 5_000, true, 300);

// "Mỗi lớp con tự mô tả" 
assertEquals("Món ăn - Phở bò", pho.describe());
assertEquals("Món ăn - Gỏi cuốn chay [chay]", goi.describe());
assertEquals("Đồ uống - Trà đá (300 ml)", tra.describe());
// "Ghi ra CSV theo loại"
assertEquals("FOOD;F01;Phở bò;55000;true;false", pho.toCsvRow());
assertEquals("DRINK;D01;Trà đá;5000;true;300", tra.toCsvRow());
// "Đa hình qua kiểu cha"
List<MenuItem> items = List.of(pho, tra);
assertEquals(60_000, items.stream().mapToLong(MenuItem::getPrice).sum());
assertTrue(goi.isVegetarian()); assertFalse(pho.isVegetarian()); assertTrue(tra.isVegetarian());
// "Bằng nhau theo mã"
assertEquals(pho, new Food("F01", "Tên khác", 1, false, true));
// "Đổi giá"
pho.changePrice(60_000); assertEquals(60_000, pho.getPrice());
// "Từ chối dữ liệu sai" (Review Focus 2)
assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở; bò", 1, true, false));
assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở\nbò", 1, true, false));
assertThrows(IllegalArgumentException.class, () -> new Food("F09", "  ", 1, true, false));
assertThrows(IllegalArgumentException.class, () -> new Food("", "Phở", 1, true, false));
assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở", 0, true, false));
assertThrows(IllegalArgumentException.class, () -> pho.changePrice(-1));
assertThrows(IllegalArgumentException.class, () -> new Drink("D09", "Nước", 1, true, 0));
```

Mỗi nhóm assert là một `@Test` riêng với `@DisplayName` ở dòng chú thích.

- [ ] **Step 2: Chạy và thấy lỗi** — `mvn -q test -Dtest=MenuItemTest` → FAIL (chưa có lớp).
- [ ] **Step 3: Cài đặt các lớp ở Interfaces.** Kiểm tra dữ liệu trong constructor bằng `IllegalArgumentException`; mọi thuộc tính `private`, `final` trừ `available` và `price`.
- [ ] **Step 4: Chạy test** — `mvn -q test` → PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(model): exception và cây MenuItem"`

---

### Task 3: `Table`, `Order`, `OrderItem` (composition)

**Files:**
- Create: `model/TableStatus.java`, `model/Table.java`, `model/OrderItem.java`, `model/Order.java`
- Test: `model/TableTest.java`, `model/OrderTest.java`

**Interfaces:**
- Consumes: `MenuItem`, `OrderStatus`, exception ở Task 2.
- Produces:
  - `enum TableStatus { FREE, OCCUPIED }`
  - `public final class Table implements Identifiable<Integer>`: `Table(int number, int seats)` (cả hai ≥ 1); `getNumber()`, `getSeats()`, `getStatus()`, `isFree()`, `occupy()` và `release()` (chuyển trạng thái sai thì `IllegalStateException`).
  - `public final class OrderItem`: constructor package-private `OrderItem(MenuItem item, int quantity)` chốt `unitPrice = item.getPrice()`; `getItem()`, `getUnitPrice()`, `getQuantity()`, `lineTotal()`; package-private `void setQuantity(int)`.
  - `public final class Order implements Identifiable<Integer>`: `public static final int MAX_QUANTITY = 99`; `Order(int id, Table table, LocalDateTime createdAt)`; `getTable()`, `getCreatedAt()`, `getStatus()`;
    `void addItem(MenuItem item) throws OrderClosedException, ItemUnavailableException, InvalidQuantityException` (gọi `addItem(item, 1)`),
    `void addItem(MenuItem item, int quantity) throws OrderClosedException, ItemUnavailableException, InvalidQuantityException`,
    `void updateQuantity(String itemId, int quantity) throws OrderClosedException, ItemNotInOrderException, InvalidQuantityException`,
    `void removeItem(String itemId) throws OrderClosedException, ItemNotInOrderException`,
    `List<OrderItem> getItems()` (`Collections.unmodifiableList`), `long subtotal()`, `boolean isEmpty()`,
    `void markPaid() throws OrderClosedException`, `void cancel() throws OrderClosedException`.
  - Thứ tự kiểm tra trong `addItem`: đơn đóng → số lượng 1–99 → món còn hàng → cộng dồn (tổng mới > 99 thì `InvalidQuantityException(tổng mới)`).

- [ ] **Step 1: Viết test thất bại**

```java
// TableTest
Table t = new Table(3, 4); assertTrue(t.isFree());
t.occupy(); assertEquals(TableStatus.OCCUPIED, t.getStatus());
assertThrows(IllegalStateException.class, t::occupy);
t.release(); assertTrue(t.isFree());
assertThrows(IllegalArgumentException.class, () -> new Table(0, 4));

// OrderTest — pho 55.000, tra 5.000, traDao hết hàng (available=false)
order.addItem(pho, 2); order.addItem(tra);
assertEquals(2, order.getItems().size());
assertEquals(115_000, order.subtotal());
// "Gọi lại món cũ thì cộng dồn"
order.addItem(pho, 3); assertEquals(5, order.getItems().get(0).getQuantity()); assertEquals(2, order.getItems().size());
// "Giới hạn số lượng"
assertThrows(InvalidQuantityException.class, () -> order.addItem(tra, 0));
assertThrows(InvalidQuantityException.class, () -> order.addItem(tra, 100));
assertThrows(InvalidQuantityException.class, () -> order.addItem(pho, 95)); // 5 + 95 > 99
assertThrows(ItemUnavailableException.class, () -> order.addItem(traDao, 1));
// "Sửa và bớt món"
order.updateQuantity("D01", 4); assertEquals(4, order.getItems().get(1).getQuantity());
order.removeItem("D01"); assertEquals(1, order.getItems().size());
assertThrows(ItemNotInOrderException.class, () -> order.removeItem("D99"));
// "Danh sách món chỉ đọc" (composition)
assertThrows(UnsupportedOperationException.class, () -> order.getItems().clear());
// "Đơn giá chốt lúc gọi món"
pho.changePrice(70_000); assertEquals(55_000, order.getItems().get(0).getUnitPrice());
// "Đơn đã thanh toán hoặc hủy thì khóa"
order.markPaid(); assertThrows(OrderClosedException.class, () -> order.addItem(tra));
assertThrows(OrderClosedException.class, order::cancel);
other.cancel(); assertEquals(OrderStatus.CANCELLED, other.getStatus());
assertThrows(OrderClosedException.class, () -> other.updateQuantity("F01", 1));
```

- [ ] **Step 2: Chạy và thấy lỗi** — `mvn -q test -Dtest='TableTest,OrderTest'` → FAIL.
- [ ] **Step 3: Cài đặt theo Interfaces.** `Order` giữ `private final List<OrderItem> items = new ArrayList<>()` và là nơi duy nhất gọi `new OrderItem(...)`.
- [ ] **Step 4: Chạy test** — `mvn -q test` → PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(model): Table, Order, OrderItem với composition"`

---

### Task 4: `Bill` và `BillCalculator` bản if-else

**Files:**
- Create: `model/BillLine.java`, `model/Bill.java`, `pricing/DiscountType.java`, `pricing/BillCalculator.java`
- Test: `pricing/BillCalculatorTest.java`

**Interfaces:**
- Produces:
  - `public record BillLine(String itemName, int quantity, long unitPrice, long lineTotal)`
  - `public record Bill(int orderId, int tableNumber, LocalDateTime issuedAt, List<BillLine> lines, long subtotal, String discountLabel, long discount, long total)` — compact constructor gán `lines = List.copyOf(lines)`.
  - `public enum DiscountType { NONE, PERCENT, FIXED }`
  - `public class BillCalculator { public Bill calculate(Order order, DiscountType type, long value, LocalDateTime issuedAt) }` — if-else theo `type`:
    - `NONE`: giảm 0, nhãn `"Không giảm giá"`
    - `PERCENT`: `value` phải 0–100 (sai thì `IllegalArgumentException`), giảm `Math.round(subtotal * value / 100.0)`, nhãn `"Giảm " + value + "%"`
    - `FIXED`: `value` ≥ 0, giảm `min(value, subtotal)`, nhãn `"Giảm " + MoneyFormat.format(value)`
    - `total = subtotal - discount`.

- [ ] **Step 1: Viết test thất bại** (đơn: phở ×2 + trà đá ×1, tạm tính 115.000; giờ `2026-11-20T12:30`)

```java
Bill b = calc.calculate(order, DiscountType.PERCENT, 10, at);
assertEquals(115_000, b.subtotal()); assertEquals(11_500, b.discount()); assertEquals(103_500, b.total());
assertEquals("Giảm 10%", b.discountLabel()); assertEquals(2, b.lines().size());
assertEquals(new BillLine("Phở bò", 2, 55_000, 110_000), b.lines().get(0));
assertEquals(0, calc.calculate(order, DiscountType.NONE, 0, at).discount());
// làm tròn: đơn tạm tính 12.345, giảm 10% → 1.235
assertEquals(1_235, calc.calculate(oddOrder, DiscountType.PERCENT, 10, at).discount());
// giảm cố định vượt tạm tính → tổng 0
assertEquals(0, calc.calculate(smallOrder30k, DiscountType.FIXED, 50_000, at).total());
assertEquals("Giảm 50.000 đ", calc.calculate(smallOrder30k, DiscountType.FIXED, 50_000, at).discountLabel());
assertThrows(IllegalArgumentException.class, () -> calc.calculate(order, DiscountType.PERCENT, 101, at));
assertThrows(IllegalArgumentException.class, () -> calc.calculate(order, DiscountType.FIXED, -1, at));
```

(`oddOrder`: một `Drink("D09","Nước test",12_345,true,330)` ×1.)

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt theo Interfaces.**
- [ ] **Step 4: Chạy test** — PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(pricing): BillCalculator bản if-else (sẽ refactor ở M5)"`

---

### Task 5: `Repository`, `InMemoryRepository`, `FileMenuRepository`, `SampleMenu`

**Files:**
- Create: `repository/Repository.java`, `repository/InMemoryRepository.java`, `repository/SampleMenu.java`, `repository/FileMenuRepository.java`
- Test: `repository/InMemoryRepositoryTest.java`, `repository/FileMenuRepositoryTest.java`

**Interfaces:**
- Produces:
  - `public interface Repository<T extends Identifiable<ID>, ID> { List<T> findAll(); Optional<T> findById(ID id); void save(T entity); }`
  - `public class InMemoryRepository<T extends Identifiable<ID>, ID> implements Repository<T, ID>` — `LinkedHashMap`, `findAll()` trả `List.copyOf` theo thứ tự thêm vào; `save` cùng mã thì thay thế.
  - `public final class SampleMenu { public static List<MenuItem> items() }` — 9 món:
    `F01 Phở bò 55000 mặn`, `F02 Cơm gà 50000 mặn`, `F03 Bún chả 50000 mặn`, `F04 Gỏi cuốn chay 35000 chay`, `F05 Đậu hũ sốt cà chua 40000 chay`,
    `D01 Trà đá 5000 300ml`, `D02 Cà phê sữa đá 25000 250ml`, `D03 Nước cam 30000 330ml`, `D04 Trà đào 35000 500ml available=false`.
  - `public class FileMenuRepository implements Repository<MenuItem, String>`: `FileMenuRepository(Path file)`; `List<String> getWarnings()`. Header `type;id;name;value;available;detail`.
    - Thiếu file: tạo thư mục cha (`Files.createDirectories`), ghi `SampleMenu.items()`.
    - Đọc file lỗi `IOException`: thêm cảnh báo `"Không đọc được ..."`, dùng `SampleMenu.items()`, không ghi đè file.
    - Mỗi dòng (bỏ dòng header): `split(";", -1)` phải có 6 cột; `switch` theo cột `type` (`FOOD`, `DRINK`) ngay trong một phương thức `private MenuItem parseRow(String row)` — đây là chỗ sẽ refactor ở Task 10. Dòng lỗi (sai số cột, loại lạ, số sai, `IllegalArgumentException` từ constructor, trùng mã với dòng trước) → bỏ qua, thêm cảnh báo bắt đầu bằng `"Dòng " + số dòng (tính từ 1, kể cả header) + ": "`.
    - `save`: cập nhật map rồi ghi lại cả file theo thứ tự map; `IOException` → `UncheckedIOException`.

- [ ] **Step 1: Viết test thất bại**

```java
// InMemoryRepositoryTest
repo.save(new Table(2, 4)); repo.save(new Table(1, 4));
assertEquals(List.of(2, 1), repo.findAll().stream().map(Table::getNumber).toList());
assertTrue(repo.findById(9).isEmpty());
assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());

// FileMenuRepositoryTest (@TempDir dir)
// "Thiếu thư mục data thì tự tạo thực đơn mẫu" (Review Focus 3)
Path file = dir.resolve("data/menu.csv");
var repo = new FileMenuRepository(file);
assertTrue(Files.exists(file)); assertEquals(9, repo.findAll().size());
// "Ghi rồi đọc lại, giữ tiếng Việt UTF-8"
repo.save(new Food("F06", "Bánh xèo", 45_000, true, false));
var again = new FileMenuRepository(file);
assertEquals("Bánh xèo", again.findById("F06").orElseThrow().getName());
assertTrue(Files.readString(file, UTF_8).contains("Phở bò"));
// "Dòng hỏng và dòng trùng mã bị bỏ qua" (Review Focus 4)
Files.writeString(f2, String.join("\n",
    "type;id;name;value;available;detail",
    "FOOD;F01;Phở bò;55000;true;false",
    "FOOD;F02;Thiếu cột",
    "FOOD;F01;Phở trùng;1;true;false",
    "XYZ;X01;Lạ;1;true;x",
    "DRINK;D01;Trà đá;abc;true;300"), UTF_8);
var r2 = new FileMenuRepository(f2);
assertEquals(1, r2.findAll().size());
assertEquals(4, r2.getWarnings().size());
assertTrue(r2.getWarnings().get(0).startsWith("Dòng 3: "));
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt theo Interfaces.** Đọc ghi bằng `Files.newBufferedReader/Writer(file, UTF_8)` trong try-with-resources.
- [ ] **Step 4: Chạy test** — PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(repository): Repository generic, bộ nhớ và file CSV"`

---

### Task 6: `MenuService` và `TableService`

**Files:**
- Create: `service/MenuService.java`, `service/TableService.java`
- Test: `service/MenuServiceTest.java`, `service/TableServiceTest.java`

**Interfaces:**
- Consumes: `Repository<MenuItem,String>`, `Repository<Table,Integer>`.
- Produces:
  - `public class MenuService`: `MenuService(Repository<MenuItem, String> repo)`;
    `List<MenuItem> all()` (sắp theo mã), `List<MenuItem> byType(ItemType type)`, `List<MenuItem> vegetarian()` (giữ món có `isVegetarian()` và `getType() != ItemType.DRINK`), `MenuItem find(String id) throws MenuItemNotFoundException`,
    `Food addFood(String name, long price, boolean vegetarian)`, `Drink addDrink(String name, long price, int volumeMl)` (mã = tiền tố `F`/`D` + `String.format("%02d", số lớn nhất hiện có của tiền tố đó + 1)`),
    `void changePrice(String id, long newPrice) throws MenuItemNotFoundException` (món không phải `SimpleMenuItem` → `IllegalArgumentException("Combo tự tính giá, không sửa giá trực tiếp")`; đây là chỗ duy nhất dùng `instanceof`, chỉ để kiểm tra món có sửa giá được không),
    `void setAvailable(String id, boolean available) throws MenuItemNotFoundException`. Mọi thay đổi gọi `repo.save`.
  - `public class TableService`: `TableService(Repository<Table, Integer> repo)`; `List<Table> all()`, `Table find(int number) throws TableNotFoundException`; `public static List<Table> defaultTables()` → 10 bàn: bàn 1–6 có 4 ghế, bàn 7–8 có 6 ghế, bàn 9–10 có 2 ghế.

- [ ] **Step 1: Viết test thất bại** (dùng `InMemoryRepository` nạp `SampleMenu.items()`)

```java
assertEquals(9, menu.all().size()); assertEquals("F01", menu.all().get(0).getId());
assertEquals(4, menu.byType(ItemType.DRINK).size());
assertEquals(List.of("F04", "F05"), menu.vegetarian().stream().map(MenuItem::getId).toList());
assertThrows(MenuItemNotFoundException.class, () -> menu.find("F99"));
assertEquals("F06", menu.addFood("Bánh xèo", 45_000, false).getId());
assertEquals("D05", menu.addDrink("Sinh tố bơ", 40_000, 400).getId());
assertEquals("F01", new MenuService(new InMemoryRepository<>()).addFood("Món đầu", 1, false).getId());
menu.changePrice("F01", 60_000); assertEquals(60_000, menu.find("F01").getPrice());
menu.setAvailable("D04", true); assertTrue(menu.find("D04").isAvailable());
// TableServiceTest
assertEquals(10, TableService.defaultTables().size());
assertEquals(6, TableService.defaultTables().get(6).getSeats());
assertThrows(TableNotFoundException.class, () -> tables.find(11));
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt theo Interfaces** (lọc và sắp bằng stream).
- [ ] **Step 4: Chạy test** — PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(service): MenuService và TableService"`

---

### Task 7: `OrderService` bản Milestone 4

**Files:**
- Create: `service/CheckoutResult.java`, `service/ItemSales.java`, `service/OrderService.java`
- Test: `service/OrderServiceTest.java`

**Interfaces:**
- Consumes: Task 3–6.
- Produces:
  - `public record CheckoutResult(Bill bill, boolean archived)`
  - `public record ItemSales(String itemName, int quantity, long revenue)`
  - `public class OrderService`: `OrderService(Repository<Order, Integer> orders, TableService tables, MenuService menu, BillCalculator calculator, Path billFile, Clock clock)`; mã đơn bắt đầu từ 1, tăng dần.
    `Order openOrder(int tableNo) throws TableNotFoundException, TableNotAvailableException`,
    `Order currentOrder(int tableNo) throws TableNotFoundException, NoOpenOrderException`,
    `void addItem(int tableNo, String itemId, int quantity) throws RestaurantException`,
    `void updateQuantity(int tableNo, String itemId, int quantity) throws RestaurantException`,
    `void removeItem(int tableNo, String itemId) throws RestaurantException`,
    `CheckoutResult checkout(int tableNo, DiscountType type, long value) throws RestaurantException`,
    `void cancel(int tableNo) throws RestaurantException`,
    `List<ItemSales> bestSellers(int limit)`.
  - `checkout` theo đúng thứ tự spec quy tắc 8: đơn rỗng → `EmptyOrderException`; tính `Bill` với `LocalDateTime.now(clock)`; `markPaid()`; `table.release()`; tự định dạng hóa đơn bằng một phương thức private rồi ghi thêm vào `billFile` (UTF-8, `CREATE`, `APPEND`, tạo thư mục cha). `IOException` → `archived=false`. Định dạng văn bản (cũng dùng ở Task 8 và Task 11):

```
========== HÓA ĐƠN ==========
Đơn #1 - Bàn 3 - 20/11/2026 12:30
Phở bò x2 @ 55.000 đ = 110.000 đ
Trà đá x1 @ 5.000 đ = 5.000 đ
Tạm tính: 115.000 đ
Giảm 10%: -11.500 đ
TỔNG CỘNG: 103.500 đ
=============================
```

  - `bestSellers`: chỉ đơn `PAID`; gom theo mã món; sắp số lượng giảm dần, bằng nhau thì tên tăng dần; `revenue` = tổng `lineTotal`.

- [ ] **Step 1: Viết test thất bại** (`Clock.fixed(Instant.parse("2026-11-20T05:30:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"))`, `billFile = dir.resolve("data/hoa-don.txt")`)

```java
Order o = service.openOrder(3); assertEquals(1, o.getId());
assertFalse(tables.find(3).isFree());
assertThrows(TableNotAvailableException.class, () -> service.openOrder(3));
assertThrows(TableNotFoundException.class, () -> service.openOrder(11));
assertThrows(NoOpenOrderException.class, () -> service.addItem(4, "F01", 1));
assertThrows(MenuItemNotFoundException.class, () -> service.addItem(3, "F99", 1));
assertThrows(ItemUnavailableException.class, () -> service.addItem(3, "D04", 1));
assertThrows(EmptyOrderException.class, () -> service.checkout(3, DiscountType.NONE, 0));
service.addItem(3, "F01", 2); service.addItem(3, "D01", 1);
CheckoutResult r = service.checkout(3, DiscountType.PERCENT, 10);
assertEquals(103_500, r.bill().total()); assertTrue(r.archived());
assertTrue(tables.find(3).isFree()); assertEquals(OrderStatus.PAID, o.getStatus());
String text = Files.readString(billFile, UTF_8);
assertTrue(text.contains("Đơn #1 - Bàn 3 - 20/11/2026 12:30"));
assertTrue(text.contains("TỔNG CỘNG: 103.500 đ"));
assertThrows(NoOpenOrderException.class, () -> service.currentOrder(3));
// "Lưu hóa đơn lỗi vẫn ghi nhận thanh toán": billFile trỏ vào một thư mục
// → archived()==false, đơn PAID, bàn trống
// "Hủy đơn trả bàn": openOrder(5); cancel(5) → bàn 5 trống, đơn CANCELLED
// "Thống kê": bàn1 phở×2+trà×1 PAID; bàn2 trà×2+cơm gà×2 PAID; bàn3 phở×5 OPEN; bàn4 cơm gà×3 CANCELLED
List<ItemSales> top = service.bestSellers(5);
assertEquals(List.of(new ItemSales("Trà đá", 3, 15_000), new ItemSales("Cơm gà", 2, 100_000), new ItemSales("Phở bò", 2, 110_000)), top);
assertEquals(1, service.bestSellers(1).size());
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt theo Interfaces.**
- [ ] **Step 4: Chạy test** — PASS.
- [ ] **Step 5: Commit** — `git commit -m "feat(service): OrderService bản M4 (tự ghi hóa đơn ra file)"`

---

### Task 8: `ConsoleMenu`, `Main`, kịch bản demo — gắn tag `milestone-4`

**Files:**
- Create: `ui/ConsoleMenu.java`, `Main.java`, `scripts/demo-input.txt`, `README.md`

**Interfaces:**
- Consumes: ba service.
- Produces: `public class ConsoleMenu { ConsoleMenu(Scanner in, PrintStream out, MenuService menu, TableService tables, OrderService orders); void run() }`. `Main.main(String[] args)`: tạo `PrintStream` UTF-8 cho `System.out`, `Scanner(System.in, UTF_8)`, `FileMenuRepository(Path.of("data/menu.csv"))` (in mọi `getWarnings()`), `InMemoryRepository` cho bàn (nạp `TableService.defaultTables()`) và đơn, `Clock.systemDefaultZone()`, `billFile = Path.of("data/hoa-don.txt")`.

- [ ] **Step 1: Cài đặt `ConsoleMenu`** — menu 0–10 đúng bảng mục 4 của spec (mục 7 hỏi loại giảm giá 1 = Không / 2 = % / 3 = số tiền; mục 8 hỏi xác nhận `c/k`; mục 9 có menu con: thêm món ăn, thêm đồ uống, sửa giá, bật tắt còn món). Bản này cố ý tự đọc số (`Integer.parseInt` trên `nextLine().trim()`, sai thì in "Vui lòng nhập một số" và hỏi lại) và tự định dạng hóa đơn để in (theo đúng mẫu ở Task 7). Vòng lặp bắt `RestaurantException` và `IllegalArgumentException`, in `"Lỗi: " + message`. `!in.hasNextLine()` ở bất kỳ lần đọc nào → in `"Tạm biệt"` và kết thúc `run()`. Thực đơn in mỗi món một dòng: `id`, `describe()`, `MoneyFormat.format(getPrice())`, `"(hết)"` nếu không còn.
- [ ] **Step 2: Viết `scripts/demo-input.txt`** theo kịch bản demo ở mục 10.3 của spec (trừ combo, sẽ thêm ở Task 13): xem thực đơn → mở bàn 3 → gọi F01×2, D01×1 → gọi D04 (đã hết) → nhập `abc` ở chỗ cần số → sửa D01 thành 2 → xem đơn → thanh toán giảm 10% → thống kê → 0.
- [ ] **Step 3: Kiểm tra build và độ phủ** — `mvn -q clean verify` → BUILD SUCCESS, không có cảnh báo, JaCoCo check qua.
- [ ] **Step 4: Chạy kịch bản** — `rm -rf data && java -jar target/restaurant-ordering.jar < scripts/demo-input.txt` → output có `"Món Trà đào đã hết"`, `"Vui lòng nhập một số"`, `"TỔNG CỘNG: 108.000 đ"` (phở ×2 = 110.000 + trà ×2 = 10.000 → 120.000, giảm 12.000), `"Tạm biệt"`; file `data/hoa-don.txt` chứa hóa đơn.
- [ ] **Step 5: Kiểm tra luồng vào bị đóng (Review Focus 1)** — `printf "1\n" | java -jar target/restaurant-ordering.jar` → in thực đơn, rồi `"Tạm biệt"`, thoát trong dưới 2 giây.
- [ ] **Step 6: README** — cách mở bằng NetBeans/IntelliJ, `mvn verify`, `java -jar target/restaurant-ordering.jar`, ghi chú tiếng Việt trên cmd (`chcp 65001`).
- [ ] **Step 7: Commit và gắn tag** — `git commit -m "feat(ui): menu console bản M4" && git tag milestone-4`

---

# PHẦN B — Bản Milestone 5 (refactor rồi thêm Combo)

Quy tắc chung của phần B: mỗi task refactor giữ nguyên **giá trị đầu vào và kết quả mong đợi** của test cũ. Chỉ đổi cách gọi khi chữ ký thay đổi. Chạy `mvn -q test` trước và sau mỗi task.

### Task 9: if-else giảm giá → Strategy

**Files:**
- Create: `pricing/DiscountStrategy.java`, `pricing/NoDiscount.java`, `pricing/PercentageDiscount.java`, `pricing/FixedAmountDiscount.java`
- Modify: `pricing/BillCalculator.java`, `service/OrderService.java`, `ui/ConsoleMenu.java`, `pricing/BillCalculatorTest.java`, `service/OrderServiceTest.java`
- Delete: `pricing/DiscountType.java`
- Test: `pricing/DiscountStrategyTest.java`

**Interfaces:**
- Produces:
  - `@FunctionalInterface public interface DiscountStrategy { long discountFor(long subtotal); default String label() { return "Giảm giá"; } }`
  - `NoDiscount` (nhãn `"Không giảm giá"`, giảm 0); `PercentageDiscount(int percent)` (0–100, giảm `Math.round(subtotal * percent / 100.0)`, nhãn `"Giảm " + percent + "%"`); `FixedAmountDiscount(long amount)` (≥ 0, giảm `min(amount, subtotal)`, nhãn `"Giảm " + MoneyFormat.format(amount)`).
  - `BillCalculator.calculate(Order order, DiscountStrategy strategy, LocalDateTime issuedAt)` — kẹp tiền giảm trong `[0, subtotal]` để chặn lambda trả giá trị sai.
  - `OrderService.checkout(int tableNo, DiscountStrategy strategy)`.

- [ ] **Step 1: Viết test thất bại** (`DiscountStrategyTest`)

```java
assertEquals(0, new NoDiscount().discountFor(115_000));
assertEquals(11_500, new PercentageDiscount(10).discountFor(115_000));
assertEquals(1_235, new PercentageDiscount(10).discountFor(12_345));
assertEquals(5_000, new PercentageDiscount(15).discountFor(33_333));
assertEquals(30_000, new FixedAmountDiscount(50_000).discountFor(30_000));
assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(101));
assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(-1));
assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscount(-1));
// "Lambda cũng là một strategy" (BillCalculatorTest)
Bill b = calc.calculate(order, s -> 200_000, at);  // lambda trả quá tạm tính
assertEquals(115_000, b.discount()); assertEquals(0, b.total()); assertEquals("Giảm giá", b.discountLabel());
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt; chuyển `BillCalculatorTest`, `OrderServiceTest`, `ConsoleMenu` sang chữ ký mới; xóa `DiscountType`.**
- [ ] **Step 4: Chạy test** — `mvn -q test` → PASS, các giá trị cũ (11.500, 1.235, tổng 0, 103.500) vẫn đúng.
- [ ] **Step 5: Commit** — `git commit -m "refactor(pricing): thay if-else giảm giá bằng Strategy (OCP)"`

---

### Task 10: `switch` tạo món → `MenuItemFactory`

**Files:**
- Create: `model/MenuItemFactory.java`
- Modify: `repository/FileMenuRepository.java` (bỏ `parseRow`)
- Test: `model/MenuItemFactoryTest.java`

**Interfaces:**
- Produces: `public final class MenuItemFactory { public static MenuItem fromCsvRow(String row, Function<String, Optional<MenuItem>> lookup) }` — ném `IllegalArgumentException` với thông báo ngắn khi dòng sai. `FileMenuRepository` gọi hàm này với `lookup = id -> Optional.ofNullable(map.get(id))` và giữ nguyên tiền tố `"Dòng N: "` cho cảnh báo.

- [ ] **Step 1: Viết test thất bại**

```java
MenuItem f = MenuItemFactory.fromCsvRow("FOOD;F01;Phở bò;55000;true;false", id -> Optional.empty());
assertInstanceOf(Food.class, f); assertEquals(55_000, f.getPrice());
assertInstanceOf(Drink.class, MenuItemFactory.fromCsvRow("DRINK;D01;Trà đá;5000;true;300", id -> Optional.empty()));
assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("XYZ;X;Lạ;1;true;x", id -> Optional.empty()));
assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("FOOD;F01;Thiếu", id -> Optional.empty()));
// toCsvRow rồi fromCsvRow ra lại món bằng nhau và cùng giá
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt; cho `FileMenuRepository` dùng factory.**
- [ ] **Step 4: Chạy test** — `mvn -q test` → PASS, `FileMenuRepositoryTest` không phải sửa.
- [ ] **Step 5: Commit** — `git commit -m "refactor(model): tách tạo món sang MenuItemFactory"`

---

### Task 11: Ghi hóa đơn → `BillArchive`, gom định dạng vào `BillFormatter`

**Files:**
- Create: `common/BillFormatter.java`, `repository/BillArchive.java`, `repository/FileBillArchive.java`
- Modify: `service/OrderService.java` (constructor nhận `BillArchive` thay cho `Path`), `ui/ConsoleMenu.java` (in hóa đơn bằng `BillFormatter`), `Main.java`, `service/OrderServiceTest.java`
- Test: `common/BillFormatterTest.java`, `repository/FileBillArchiveTest.java`

**Interfaces:**
- Produces:
  - `public final class BillFormatter { public static String format(Bill bill) }` — đúng mẫu văn bản ở Task 7, kết thúc bằng xuống dòng.
  - `public interface BillArchive { void archive(Bill bill) throws IOException; }`
  - `public class FileBillArchive implements BillArchive { FileBillArchive(Path file) }` — ghi thêm `BillFormatter.format(bill)`, UTF-8, tạo thư mục cha.
  - `OrderService(Repository<Order,Integer> orders, TableService tables, MenuService menu, BillCalculator calculator, BillArchive archive, Clock clock)`.

- [ ] **Step 1: Viết test thất bại**

```java
// BillFormatterTest: so sánh nguyên văn với mẫu của Task 7 (bill dựng sẵn bằng record)
// FileBillArchiveTest (@TempDir): archive hai lần → file chứa 2 lần "========== HÓA ĐƠN"; đọc lại UTF-8 thấy "Phở bò"
// OrderServiceTest: thay billFile bằng
BillArchive failing = bill -> { throw new IOException("đĩa đầy"); };
List<Bill> saved = new ArrayList<>(); BillArchive memory = saved::add;
// → archived()==false với failing; saved.size()==1 với memory
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt; xóa phần định dạng bị lặp trong `OrderService` và `ConsoleMenu`.**
- [ ] **Step 4: Chạy test** — `mvn -q test` → PASS.
- [ ] **Step 5: Commit** — `git commit -m "refactor: BillArchive (DIP) và BillFormatter (DRY)"`

---

### Task 12: Tách `InputReader` khỏi `ConsoleMenu`

**Files:**
- Create: `ui/InputReader.java`, `ui/InputClosedException.java`
- Modify: `ui/ConsoleMenu.java` (constructor nhận `InputReader` thay cho `Scanner`), `Main.java`
- Test: `ui/InputReaderTest.java`

**Interfaces:**
- Produces:
  - `public class InputClosedException extends RuntimeException`
  - `public class InputReader { InputReader(Scanner in, PrintStream out); int readInt(String prompt, int min, int max); String readNonBlank(String prompt); boolean confirm(String prompt) }` — `confirm` nhận `c`/`có` là đúng, `k`/`không` là sai, không phân biệt hoa thường, câu khác thì hỏi lại. Hết dữ liệu nhập → `InputClosedException`. `ConsoleMenu.run()` bắt exception này, in `"Tạm biệt"`.

- [ ] **Step 1: Viết test thất bại** (`Scanner` dựng từ chuỗi, `out` là `ByteArrayOutputStream`)

```java
assertEquals(5, reader("abc\n5\n").readInt("Chọn: ", 1, 10));            // output chứa "Vui lòng nhập một số"
assertEquals(7, reader(" 7 \n").readInt("Chọn: ", 1, 10));               // Review Focus 5
assertEquals(3, reader("0\n11\n\n3\n").readInt("Chọn: ", 1, 10));        // output chứa "từ 1 đến 10"
assertThrows(InputClosedException.class, () -> reader("").readInt("Chọn: ", 1, 10)); // Review Focus 1
assertEquals("Phở", reader("\n   \nPhở\n").readNonBlank("Tên: "));
assertTrue(reader("x\nC\n").confirm("Hủy? "));
assertFalse(reader("không\n").confirm("Hủy? "));
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt; `ConsoleMenu` bỏ toàn bộ code tự đọc số.**
- [ ] **Step 4: Chạy test và kịch bản** — `mvn -q clean verify` → PASS; `java -jar target/restaurant-ordering.jar < scripts/demo-input.txt` → output vẫn giống Task 8 Step 4.
- [ ] **Step 5: Commit** — `git commit -m "refactor(ui): tách InputReader khỏi ConsoleMenu (SRP)"`

---

### Task 13: Thêm `Combo` (Composite) — gắn tag `milestone-5`

**Files:**
- Create: `model/Combo.java`
- Modify: `model/ItemType.java` (+`COMBO`), `model/MenuItemFactory.java` (+`COMBO`), `repository/SampleMenu.java` (+2 combo), `ui/ConsoleMenu.java` (lọc combo), `scripts/demo-input.txt` (gọi 1 combo)
- Test: `model/ComboTest.java`, thêm ca combo vào `model/MenuItemFactoryTest.java`, sửa số món mong đợi trong test dùng `SampleMenu`

**Interfaces:**
- Produces: `public final class Combo extends MenuItem { Combo(String id, String name, boolean available, int discountPercent, List<MenuItem> components); List<MenuItem> getComponents() }`
  - `getPrice()` = `sum - Math.round(sum * discountPercent / 100.0)`, với `sum` là tổng `getPrice()` của các món thành phần (tự đệ quy khi combo lồng combo).
  - `isAvailable()` = cờ riêng && mọi món thành phần còn. `isVegetarian()` = mọi món thành phần đều chay.
  - `describe()` = `"Combo - " + name + ": " + tên các món nối bằng " + " + " (giảm " + discountPercent + "%)"`.
  - `toCsvRow()` = `"COMBO;" + id + ";" + name + ";" + discountPercent + ";" + availableFlag() + ";" + mã các món nối bằng "|"`.
  - Constructor: danh sách rỗng hoặc % ngoài 0–100 thì `IllegalArgumentException`. Lưu `List.copyOf(components)`.
  - Factory `COMBO`: `value` = %, `detail` = mã nối bằng `|`; mã không có trong `lookup` → `IllegalArgumentException("không có món " + id)`.
  - `SampleMenu` thêm `C01 Combo trưa = F02|D01, 10%` (giá 49.500) và `C02 Combo chay = F04|F05|D03, 15%` (giá 89.250); tổng 11 món.

- [ ] **Step 1: Viết test thất bại** (`ComboTest`)

```java
Combo trua = new Combo("C01", "Combo trưa", true, 10, List.of(comGa, traDa));
assertEquals(49_500, trua.getPrice());
assertEquals("Combo - Combo trưa: Cơm gà + Trà đá (giảm 10%)", trua.describe());
assertEquals("COMBO;C01;Combo trưa;10;true;F02|D01", trua.toCsvRow());
Combo lon = new Combo("C09", "Combo lớn", true, 0, List.of(trua, nuocCam));
assertEquals(79_500, lon.getPrice());                                   // combo lồng combo
assertEquals(11_110, new Combo("C08", "Thử", true, 10, List.of(nuoc12345)).getPrice()); // 12.345 - 1.235
traDa.setAvailable(false); assertFalse(trua.isAvailable()); assertFalse(lon.isAvailable());
assertTrue(new Combo("C02", "Combo chay", true, 15, List.of(goiCuon, dauHu, nuocCam)).isVegetarian());
assertEquals(89_250, new Combo("C02", "Combo chay", true, 15, List.of(goiCuon, dauHu, nuocCam)).getPrice());
assertFalse(trua.isVegetarian());                                        // cơm gà là món mặn
// MenuServiceTest với SampleMenu mới: vegetarian() → ["C02", "F04", "F05"]
assertThrows(IllegalArgumentException.class, () -> new Combo("C06", "Rỗng", true, 0, List.of()));
assertThrows(IllegalArgumentException.class, () -> new Combo("C06", "Sai", true, 101, List.of(traDa)));
// "Gọi combo trong đơn dùng chung code tính tiền" (OCP/LSP)
order.addItem(trua, 2); assertEquals(99_000, order.subtotal());
// Factory: "COMBO;C01;Combo trưa;10;true;F02|D01" với lookup có F02, D01 → Combo giá 49.500;
//          "COMBO;C03;Hỏng;10;true;F02|F99" → IllegalArgumentException
```

- [ ] **Step 2: Chạy và thấy lỗi** — FAIL.
- [ ] **Step 3: Cài đặt `Combo`, cập nhật factory, `SampleMenu`, `ConsoleMenu` (mục 1 thêm lọc "combo"), kịch bản demo (gọi `C01`×1; tổng mong đợi cập nhật: 120.000 + 49.500 = 169.500, giảm 10% = 16.950, `TỔNG CỘNG: 152.550 đ`).**
- [ ] **Step 4: Kiểm tra toàn bộ** — `mvn -q clean verify` → PASS; chạy kịch bản → `TỔNG CỘNG: 152.550 đ`.
- [ ] **Step 5: Commit** — `git commit -m "feat(model): thêm Combo theo pattern Composite"`
- [ ] **Step 6: Chứng minh OCP** — `git show --stat HEAD` không liệt kê `Order.java`, `OrderItem.java`, `BillCalculator.java`, `OrderService.java`. Nếu có, tìm lại thiết kế trước khi gắn tag.
- [ ] **Step 7: Gắn tag** — `git tag milestone-5`

---

# PHẦN C — Bản Final

### Task 14: Giao diện Swing dùng chung service — gắn tag `final`

**Files:**
- Create: `ui/swing/MainWindow.java`
- Modify: `Main.java` (`--gui`), `README.md`

**Interfaces:**
- Consumes: `MenuService`, `TableService`, `OrderService`, `BillFormatter`, `DiscountStrategy` và ba cài đặt.
- Produces: `public final class MainWindow { MainWindow(MenuService menu, TableService tables, OrderService orders); void show() }` — giữ `private final JFrame frame` (composition). Không kế thừa `JFrame`, vì lớp con của `JFrame` sinh cảnh báo `serial` và `this-escape` trên JDK 21, làm hỏng build vốn coi cảnh báo là lỗi.

- [ ] **Step 1: Cài đặt `MainWindow`** — `frame` dùng `BorderLayout`: WEST bảng bàn (Số bàn, Số ghế, Trạng thái); CENTER bảng thực đơn (Mã, Món, Giá, Còn); EAST bảng đơn của bàn đang chọn (Món, SL, Đơn giá, Thành tiền) cùng nhãn tạm tính; SOUTH nút Mở bàn, Gọi món (số lượng bằng `JSpinner` 1–99), Bớt món, Thanh toán (`JComboBox` Không giảm / Giảm % / Giảm tiền, cùng `JTextField` giá trị), Hủy đơn (hỏi xác nhận bằng `JOptionPane.showConfirmDialog`). Lỗi `RestaurantException` hoặc `IllegalArgumentException` hiện bằng `JOptionPane.showMessageDialog`. Thanh toán xong hiện `BillFormatter.format(bill)` trong `JTextArea` chỉ đọc. Dùng `DefaultTableModel`, có một phương thức `refresh()` dựng lại cả ba bảng sau mỗi thao tác. Không thêm logic nghiệp vụ.
- [ ] **Step 2: `Main`** — `args` chứa `--gui` thì `SwingUtilities.invokeLater(() -> new MainWindow(...).show())`, ngược lại chạy console như cũ.
- [ ] **Step 3: Chứng minh kiến trúc** — `git diff milestone-5 --stat -- src/main/java/restaurant/model src/main/java/restaurant/pricing src/main/java/restaurant/repository src/main/java/restaurant/service` → rỗng.
- [ ] **Step 4: Kiểm tra** — `mvn -q clean verify` → PASS. Nếu có `xvfb-run`: chạy `xvfb-run -a java -jar target/restaurant-ordering.jar --gui` trong nền, chụp màn hình cửa sổ (ImageMagick `import -window root`) để xem bố cục, rồi tắt. Nếu không có màn hình ảo: ghi rõ trong báo cáo bàn giao rằng phần Swing mới chỉ biên dịch, chưa chạy thử.
- [ ] **Step 5: README** — thêm cách chạy `--gui`.
- [ ] **Step 6: Commit và gắn tag** — `git commit -m "feat(ui): giao diện Swing dùng chung service" && git tag final`

---

### Task 15: Kiểm tra cuối và bàn giao

**Files:** không thêm code.

- [ ] **Step 1:** `mvn clean verify` → `Tests run: ≥ 40, Failures: 0, Errors: 0`, JaCoCo check qua, không có dòng `[WARNING]` từ compiler. Ghi lại số test và % độ phủ (từ `target/site/jacoco/index.html`).
- [ ] **Step 2:** Chạy kịch bản demo một lần nữa, lưu output vào `docs/demo-output.txt` để đưa vào báo cáo.
- [ ] **Step 3:** `git tag` liệt kê đủ `milestone-4`, `milestone-5`, `final`; `git status` sạch.
- [ ] **Step 4:** Đóng gói `restaurant-ordering.zip` gồm mã nguồn, `.git` (để giữ lịch sử và tag), không gồm `target/` và `data/`. Gửi cho người dùng kèm số test, % độ phủ và những gì chưa kiểm tra được.
