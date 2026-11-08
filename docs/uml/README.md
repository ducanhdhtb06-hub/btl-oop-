# Biểu đồ UML

**Sửa hình:** mở `UML-NhaHang.drawio` bằng [app.diagrams.net](https://app.diagrams.net) (File → Open), draw.io bản cài máy, hoặc extension **Draw.io Integration** trong VS Code. File có 8 trang, mỗi biểu đồ một trang. Mọi lớp, use case, đường đời và mũi tên là đối tượng draw.io thật: kéo thả, sửa chữ, xuất PNG hoặc PDF được ngay (File → Export as).

**Chèn vào báo cáo, slide:** dùng các file `.png` (hoặc `.svg` để phóng to không vỡ nét).

| Hình | Nội dung |
|---|---|
| `1-lop-model.png` | Biểu đồ lớp gói `model` và `pricing`, đủ thuộc tính và phương thức |
| `1-lop-model-tom-tat.png` | Như trên nhưng chỉ có tên lớp và quan hệ (dùng cho slide) |
| `2-lop-kien-truc.png` | Kiến trúc phân tầng: `restaurant` → `ui` → `service` → `repository`, `pricing` → `model`, `common` |
| `2-lop-kien-truc-tom-tat.png` | Như trên, chỉ tên lớp và quan hệ |
| `3-lop-ngoai-le.png` | Cây ngoại lệ: 9 lớp kế thừa `RestaurantException` (checked) và `InputClosedException` (unchecked) |
| `4-use-case.png` | Use case: 2 tác nhân (Quản lý tổng quát hóa Nhân viên phục vụ), «include», «extend», extension points |
| `5-tuan-tu-goi-mon.png` | Tuần tự chức năng gọi món, đủ các nhánh lỗi |
| `6-tuan-tu-thanh-toan.png` | Tuần tự thanh toán, thấy rõ Strategy khi tính giảm giá |

## Độ chính xác

- Danh sách thuộc tính, phương thức, kiểu dữ liệu, tham số và mức truy cập trong biểu đồ lớp được lấy tự động từ code đã biên dịch (`javap`) và mã nguồn, không gõ tay.
- Từng đường quan hệ trong biểu đồ lớp đã được đối chiếu với code: kế thừa và hiện thực khớp `extends`/`implements`; tên và bội số của kết hợp, tụ hợp, hợp thành khớp kiểu của thuộc tính (danh sách thì `0..*`, một đối tượng thì `1`); «use», «create», «call» khớp lời gọi trong code.
- Biểu đồ tuần tự đi đúng thứ tự lời gọi hàm trong `ConsoleMenu`, `OrderService`, `Order`, `BillCalculator`.

## Quy ước ký hiệu

| Ký hiệu | Ý nghĩa | Ví dụ |
|---|---|---|
| `+` `-` `#` `~` | public, private, protected, package | `~setQuantity(...)` chỉ gọi được trong gói `model` |
| chữ gạch chân | thành viên static | `MenuItemFactory.fromCsvRow(...)` |
| chữ nghiêng | lớp trừu tượng, phương thức trừu tượng | `MenuItem`, `getPrice()` |
| tam giác rỗng, nét liền | kế thừa | `Food` → `SimpleMenuItem` → `MenuItem` |
| tam giác rỗng, nét đứt | hiện thực interface | `PercentageDiscount` hiện thực `DiscountStrategy` |
| hình thoi đặc | hợp thành | `Order` 1 ◆— 0..* `OrderItem` |
| hình thoi rỗng | tụ hợp | `Combo` 0..* ◇— 1..* `MenuItem` |
| mũi tên nét liền | kết hợp (tên ở giữa là tên thuộc tính trong code) | `Order` → `Table` qua thuộc tính `table` |
| mũi tên nét đứt | phụ thuộc «use», «create», «call» | `BillCalculator` «create» `Bill` |

Không vẽ: hàm private phụ trợ, lớp lồng bên trong (`MainWindow.UiAction`), hàm tạo mặc định do Java tự thêm, `serialVersionUID`, hàm tạo và các hàm truy cập của record (record chỉ vẽ các thành phần), `values()`/`valueOf()` của enum. Ghi chú trên hình nói rõ những phần khác được lược cho gọn (ví dụ các thuộc tính Swing của `MainWindow`).

Biểu đồ tuần tự theo ký hiệu UML 2: gọi hàm là nét liền mũi tên đặc; trả về và tạo đối tượng («create») là nét đứt mũi tên hở.

## File nguồn

| File | Dùng để |
|---|---|
| `UML-NhaHang.drawio` | Sửa hình bằng draw.io (cách dễ nhất) |
| `*.dot` | Nguồn Graphviz của biểu đồ lớp: `dot -Tpng -Gdpi=170 1-lop-model.dot -o 1-lop-model.png` |
| `*.mmd`, `mermaid-config.json` | Nguồn Mermaid của biểu đồ tuần tự: `mmdc -i 5-tuan-tu-goi-mon.mmd -o 5-tuan-tu-goi-mon.png -c mermaid-config.json -b white -s 2 -w 1600` |
| `4-use-case.svg` | Hình use case dạng vector |
