# Việc của từng bạn: mỗi người sửa một lỗi nhỏ

App đã chạy đúng khi demo. Còn 4 lỗi nhỏ, chỉ xảy ra khi ai đó sửa tay file `data/menu.csv` không đúng kiểu. Mỗi thành viên sửa một lỗi gần với phần mình phụ trách (theo bảng phân công ở mục 12 của bản thiết kế), rồi commit bằng tên của mình. Làm xong, lịch sử git có commit của cả 4 người, và mỗi người có một ví dụ thật để kể khi thầy hỏi.

| Bài | Ai làm | Lỗi | Sửa ở |
|---|---|---|---|
| 1 | Thành viên 1 (`model`) | Mã món viết thường trong file | `MenuItem`, `MenuItemFactory` |
| 2 | Thành viên 2 (`pricing`, `repository`) | Số quá lớn trong file làm sai dữ liệu và tổng tiền | `SimpleMenuItem`, `MenuItemFactory` |
| 3 | Thành viên 3 (`service`) | Mã có phần số quá dài làm hỏng chức năng thêm món | `MenuService.nextId` |
| 4 | Thành viên 4 (`ui`) | Thực đơn hiện F10 trước F9 trên cả hai giao diện | `MenuService.all` |

## Cách làm chung

Làm lần lượt Bài 1 → 2 → 3 → 4. Người trước commit xong thì người sau mới bắt đầu. Bài 1 và 2 cùng sửa `MenuItemFactory`, Bài 3 và 4 cùng sửa `MenuService`, làm song song sẽ bị xung đột khi gộp code.

Mỗi bài có 5 bước:

1. **Lấy bản mới nhất.** Nhóm dùng GitHub thì chạy `git pull`. Không dùng GitHub thì cả nhóm làm lần lượt trên cùng một máy.
2. **Đặt tên của bạn cho repo này.** Bắt buộc, kể cả khi làm trên máy mình. Lệnh không có `--global` nên chỉ áp dụng trong thư mục dự án:
   ```bash
   git config user.name "Nguyễn Văn A"
   git config user.email "email-cua-ban@example.com"
   ```
   Repo không đặt sẵn tên người commit. Nếu quên bước này, git sẽ báo `Please tell me who you are`, hoặc lấy tên đang cài chung trên máy.
3. **Thêm test của bài rồi chạy. Test phải báo đỏ.** Đỏ là đúng, vì nó chứng minh lỗi có thật.
   - VS Code: mở file test, bấm nút ▶ cạnh tên hàm test vừa thêm.
   - Dòng lệnh: `mvn test -Dtest=MenuItemFactoryTest` (thay bằng tên lớp test của bài). Maven sẽ báo `BUILD FAILURE`, đúng như mong đợi.
4. **Sửa code theo gợi ý cho tới khi test xanh.** Sau đó chạy toàn bộ test (`mvn test`, hoặc khung Testing → Run All Tests). Tất cả phải xanh, vì các test cũ là thứ bảo đảm bạn không làm hỏng chỗ khác.
5. **Commit rồi kiểm tra tên:**
   ```bash
   git add src
   git commit -m "fix(model): ..."
   git log -1 --format="%an | %s"
   ```
   Lệnh cuối phải in ra tên bạn. Nếu in ra tên khác thì làm lại bước 2 rồi chạy `git commit --amend --reset-author --no-edit`. Nhóm dùng GitHub thì `git push`, rồi báo bạn kế tiếp.

Lưu ý:

- Dự án coi cảnh báo của trình biên dịch là lỗi. Nếu `mvn` báo `[WARNING]` ở dòng bạn vừa viết (ví dụ ép kiểu thừa) thì sửa luôn.
- Muốn tự thấy lỗi trên app: chạy app một lần cho nó tạo `data/menu.csv`, thêm các dòng ở mục "Triệu chứng" vào cuối file đó, rồi chạy `ChayConsole.java`. Xem xong thì xóa thư mục `data/` để về lại thực đơn mẫu.
- Test mẫu trong file này do AI viết sẵn, phần sửa code là của bạn. Nếu thầy hỏi nhóm dùng AI thế nào, cứ nói đúng như vậy.
- Bị kẹt thì đọc kỹ thông báo đỏ của test trước: nó ghi rõ mong đợi gì và thực tế nhận được gì.

## Bài 1. Thành viên 1 (`model`): mã món viết thường trong file

**Triệu chứng.** Thêm vào `data/menu.csv`:

```
food;f06;Bún chả;45000;true;false
FOOD;f07;Bún riêu;40000;true;false
```

- Dòng đầu bị bỏ qua: `Cảnh báo: Dòng …: loại món không hợp lệ: food, bỏ qua`.
- Dòng sau đọc được nhưng thực đơn hiện mã `f07`. Gọi món `f07` thì báo `Lỗi: Không tìm thấy món F07`, vì console đổi mã gõ vào thành chữ hoa.

**Thêm test** vào cuối lớp `src/test/java/restaurant/model/MenuItemFactoryTest.java` (trước dấu `}` cuối cùng):

```java
    @Test
    @DisplayName("Mã và loại món viết thường trong file vẫn đọc được, mã được đổi thành chữ hoa")
    void readsLowerCaseRows() {
        assertEquals("F02", new Food("f02", "Cơm gà", 50_000, true, false).getId());
        MenuItem comGa = MenuItemFactory.fromCsvRow("food;f02;Cơm gà;50000;true;false", none);
        assertEquals("F02", comGa.getId());
        Function<String, Optional<MenuItem>> lookup = id -> id.equals("F02") ? Optional.of(comGa) : Optional.empty();
        Combo combo = assertInstanceOf(Combo.class,
                MenuItemFactory.fromCsvRow("combo;c05;Combo cơm;10;true;f02", lookup));
        assertEquals("C05", combo.getId());
        assertEquals(List.of(comGa), combo.getComponents());
    }
```

**Sửa ở đâu:** constructor của `MenuItem`, và hàm `fromCsvRow`, `components` của `MenuItemFactory`. Khoảng 3 dòng code và 2 dòng `import`.

**Gợi ý:**

- Đổi mã sang chữ hoa ngay trong constructor của `MenuItem`. Như vậy mọi nơi tạo món đều được chuẩn hóa, không phải nhớ sửa từng chỗ.
- Dùng `toUpperCase(Locale.ROOT)`, đừng dùng `toUpperCase()` không tham số: trên máy đặt ngôn ngữ Thổ Nhĩ Kỳ, chữ `i` sẽ bị đổi thành `İ`.
- Cột loại món (`food`) cũng phải đổi sang chữ hoa trước khi vào `switch`.
- Mã các món thành phần của combo (`f02` trong `f02|d01`) cũng phải đổi sang chữ hoa trước khi tìm, vì các món đã được lưu bằng mã chữ hoa. Test có kiểm tra đúng chỗ này.

**Commit:** `fix(model): đọc được mã và loại món viết thường trong menu.csv`

**Khi thầy hỏi:** `equals` và `hashCode` của `MenuItem` dựa vào mã, nên mã phải được chuẩn hóa ở một chỗ duy nhất là constructor. Đó là đóng gói: đối tượng tự bảo đảm dữ liệu của mình luôn hợp lệ.

## Bài 2. Thành viên 2 (`pricing`, `repository`): số quá lớn trong file

**Triệu chứng.** Thêm vào `data/menu.csv`:

```
DRINK;D05;Nước suối;10000;true;4294967596
FOOD;F08;Món đắt;9000000000000000000;true;false
```

- Nước suối hiện `(300 ml)`. Số 4294967596 bị ép kiểu `(int)` cắt mất phần đầu mà không báo gì. Cột phần trăm giảm của combo cũng bị y như vậy.
- Gọi 2 phần Món đắt thì `Tạm tính: -446.744.073.709.551.616 đ`. Tổng tiền vượt giới hạn của kiểu `long` nên quay vòng thành số âm.

**Liên quan phần của bạn:** tính sai tổng tiền là chuyện của `pricing`, dữ liệu bị đổi âm thầm khi đọc file là chuyện của `repository`. Nhưng chặn đúng chỗ là lúc tạo món (`model`), để món mang số sai không bao giờ được tạo ra. Khi đó `BillCalculator` và mọi chỗ dùng món phía sau đều an toàn mà không phải sửa gì.

**Thêm test** vào cuối lớp `MenuItemFactoryTest` (cùng file với Bài 1):

```java
    @Test
    @DisplayName("Số quá lớn trong file bị từ chối, không bị cắt bớt âm thầm")
    void rejectsHugeNumbers() {
        assertEquals(100_000_000, MenuItemFactory.fromCsvRow("FOOD;F09;Món đắt;100000000;true;false", none).getPrice());
        assertThrows(IllegalArgumentException.class,
                () -> MenuItemFactory.fromCsvRow("FOOD;F09;Món đắt;100000001;true;false", none));
        assertThrows(IllegalArgumentException.class,
                () -> MenuItemFactory.fromCsvRow("DRINK;D09;Nước suối;10000;true;4294967596", none));
        Food pho = new Food("F01", "Phở bò", 55_000, true, false);
        assertThrows(IllegalArgumentException.class,
                () -> MenuItemFactory.fromCsvRow("COMBO;C09;Combo phở;4294967306;true;F01", id -> Optional.of(pho)));
        assertThrows(IllegalArgumentException.class, () -> pho.changePrice(100_000_001));
    }
```

**Sửa ở đâu:** `SimpleMenuItem` và `MenuItemFactory`. Khoảng 10 dòng.

**Gợi ý:**

- Trong `SimpleMenuItem`, thêm hằng `public static final long MAX_PRICE = 100_000_000;` rồi sửa hàm `requirePositive` để từ chối cả giá lớn hơn `MAX_PRICE`, nhớ sửa câu báo lỗi. Constructor và `changePrice` đều gọi hàm này nên chỉ cần sửa một chỗ. Nên đổi tên hàm cho đúng nghĩa, ví dụ `requireValidPrice`.
- Giá tối đa 100 triệu, mỗi món tối đa 99 phần, nên tổng tiền còn rất xa giới hạn của `long`.
- Trong `MenuItemFactory`, thay hai chỗ ép kiểu `(int)` bằng `Math.toIntExact(...)`. Hàm này ném `ArithmeticException` khi số không vừa kiểu `int`.
- Bắt `ArithmeticException` rồi ném `IllegalArgumentException`, ví dụ viết một hàm nhỏ `toInt(long value)`. Bắt buộc phải là `IllegalArgumentException`, vì `FileMenuRepository` chỉ bắt lỗi này để bỏ qua dòng sai. Ném lỗi khác thì app dừng ngay lúc mở.

**Commit:** `fix(model): từ chối giá quá 100 triệu và số quá lớn trong menu.csv`

**Khi thầy hỏi:** vì sao `(int)` nguy hiểm (ép kiểu thu hẹp chỉ giữ 32 bit cuối, không báo lỗi), và vì sao kiểm tra ở constructor thay vì ở `BillCalculator` (kiểm tra một lần ở nơi tạo dữ liệu, thay vì ở mọi nơi dùng dữ liệu).

## Bài 3. Thành viên 3 (`service`): mã có phần số quá dài làm hỏng chức năng thêm món

**Triệu chứng.** Thêm vào `data/menu.csv`:

```
FOOD;F99999999999;Món thử;1000;true;false
```

Từ đó mọi lần chọn **9 → 1. Thêm món ăn** đều báo `Lỗi: For input string: "99999999999"`. Chỉ một dòng lạ mà không thêm được món ăn nào nữa.

**Thêm test** vào cuối lớp `src/test/java/restaurant/service/MenuServiceTest.java`, và thêm dòng `import restaurant.model.Food;` vào nhóm `import` ở đầu file:

```java
    @Test
    @DisplayName("Mã món có phần số quá dài trong file không làm hỏng việc sinh mã mới")
    void nextIdIgnoresHugeNumbers() {
        repo.save(new Food("F99999999999", "Món lạ", 1_000, true, false));
        assertEquals("F06", menu.addFood("Bánh xèo", 45_000, false).getId());
    }
```

**Sửa ở đâu:** hàm `nextId` trong `MenuService`. Sửa 1 dòng là đủ.

**Gợi ý:**

- `Integer.parseInt` chỉ đọc được số tới 2.147.483.647 (10 chữ số). Số dài hơn thì nó ném `NumberFormatException`.
- Cách gọn nhất: chỉ xét những mã có phần số dài từ 1 đến 9 chữ số, bằng cách sửa biểu thức `\\d+` trong `filter`. Mã lạ vẫn nằm trong thực đơn, chỉ không được dùng để tính mã kế tiếp.

**Commit:** `fix(service): thêm món mới không bị lỗi khi file có mã số quá dài`

**Khi thầy hỏi:** giải thích chuỗi stream trong `nextId` (`filter` → `mapToInt` → `max` → `orElse`), và vì sao test dùng `InMemoryRepository` chứ không dùng file: test chạy nhanh, không đụng ổ đĩa, làm được vì `MenuService` chỉ biết interface `Repository`.

## Bài 4. Thành viên 4 (`ui`): thực đơn hiện F10 trước F9

**Triệu chứng.** Thêm vào `data/menu.csv`:

```
FOOD;F9;Chè đậu;15000;true;true
FOOD;F10;Xôi gà;30000;true;false
FOOD;F100;Bánh mì;20000;true;false
```

Cả console (chức năng 1) lẫn cửa sổ Swing đều hiện theo thứ tự `F10, F100, F9`. Mã đang được sắp như chữ, so từng ký tự một, chứ không theo giá trị số.

**Liên quan phần của bạn:** lỗi hiện ra ở cả hai giao diện, nhưng sửa đúng chỗ là `MenuService.all()`, vì console và Swing đều lấy thực đơn từ hàm này. Sửa một chỗ, hai giao diện cùng đúng. Đây là ví dụ rất tốt để trình bày lợi ích của việc chia tầng.

**Thêm test** vào cuối lớp `MenuServiceTest` (nếu file chưa có `import restaurant.model.Food;` thì thêm vào):

```java
    @Test
    @DisplayName("Thực đơn sắp theo mã tự nhiên: F9 trước F10, F11 trước F100")
    void sortsIdsNaturally() {
        InMemoryRepository<MenuItem, String> items = new InMemoryRepository<>();
        for (String id : List.of("F100", "F11", "F123456789012345678901", "F10", "F9")) {
            items.save(new Food(id, "Món " + id, 1_000, true, false));
        }
        assertEquals(List.of("F9", "F10", "F11", "F100", "F123456789012345678901"),
                new MenuService(items).all().stream().map(MenuItem::getId).toList());
    }
```

**Sửa ở đâu:** hàm `all()` trong `MenuService`. Khoảng 10 đến 15 dòng: một `Comparator` và một hai hàm phụ.

**Gợi ý:**

- Tách mã thành phần chữ ở đầu (`F`) và phần số ở cuối (`10`). So sánh phần chữ trước, rồi đến phần số.
- Đừng đổi phần số bằng `Long.parseLong`: mã gõ tay có thể dài hơn 19 chữ số, và test có một mã như vậy. Cách không bao giờ tràn số: bỏ các chữ số 0 ở đầu, số nào nhiều chữ số hơn thì lớn hơn, nếu bằng nhau thì so sánh như chuỗi.
- Cuối cùng so sánh cả mã (`thenComparing(MenuItem::getId)`), để những mã như `F01` và `F1` vẫn có thứ tự cố định.
- `byType()` và `vegetarian()` đều gọi `all()` nên tự đúng theo. Các test cũ vẫn phải xanh: `C01` đứng đầu, lọc món chay ra `[C02, F04, F05]`.

**Commit:** `fix(service): sắp thực đơn theo mã tự nhiên, F9 trước F10`

**Khi thầy hỏi:** `Comparator.comparing(...).thenComparing(...)` là cách truyền một cách so sánh vào hàm `sorted` (cùng ý tưởng với pattern Strategy), và vì sao không sửa riêng trong `ConsoleMenu` hay `MainWindow`.

## Khi cả 4 bài xong

1. Chạy `mvn verify` trên đúng máy sẽ dùng để demo. Tất cả test phải xanh.
2. Chạy `git log --format="%an | %s" -6`, phải thấy tên đủ 4 bạn.
3. Thêm một dòng `v1.4` vào bảng "Các mốc trong git" trong `README.md`, commit, rồi gắn mốc bằng `git tag v1.4` (dùng GitHub thì `git push --tags`).
4. Mỗi bạn chuẩn bị 3 câu để kể: lỗi là gì, vì sao xảy ra, test nào chứng minh đã sửa xong.
