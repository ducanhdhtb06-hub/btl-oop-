# Biểu đồ UML

Vẽ theo đúng code hiện tại. Mỗi hình có file ảnh `.png` để chèn vào báo cáo và slide, và file nguồn để sửa.

| Hình | Nội dung | File nguồn |
|---|---|---|
| `1-lop-model.png` | Biểu đồ lớp gói `model` và `pricing`: đủ 6 loại quan hệ (kế thừa 3 tầng, hiện thực interface, kết hợp, tụ hợp, hợp thành, phụ thuộc) và bội số | `1-lop-model.mmd` |
| `2-lop-kien-truc.png` | Biểu đồ lớp kiến trúc phân tầng: ui → service → interface `Repository`, `BillArchive` → cài đặt bộ nhớ và file | `2-lop-kien-truc.mmd` |
| `3-lop-ngoai-le.png` | Cây ngoại lệ nghiệp vụ: 9 lớp kế thừa `RestaurantException` | `3-lop-ngoai-le.mmd` |
| `4-use-case.png` | Biểu đồ use case: 2 tác nhân (Quản lý tổng quát hóa Nhân viên phục vụ), «include», «extend» | `4-use-case.svg` |
| `5-tuan-tu-goi-mon.png` | Biểu đồ tuần tự chức năng gọi món, có các nhánh lỗi | `5-tuan-tu-goi-mon.mmd` |
| `6-tuan-tu-thanh-toan.png` | Biểu đồ tuần tự thanh toán, thấy rõ pattern Strategy khi tính giảm giá | `6-tuan-tu-thanh-toan.mmd` |

Các ký hiệu quan hệ trong hình 1:

| Quan hệ | Ví dụ trong code | Ký hiệu |
|---|---|---|
| Kế thừa | `Food` → `SimpleMenuItem` → `MenuItem` | mũi tên tam giác rỗng, nét liền |
| Hiện thực | `PercentageDiscount` hiện thực `DiscountStrategy` | mũi tên tam giác rỗng, nét đứt |
| Hợp thành | `Order` 1 ◆— 0..* `OrderItem` (OrderItem chỉ được tạo bên trong Order) | hình thoi đặc |
| Tụ hợp | `Combo` 0..* ◇— 1..* `MenuItem` (món thành phần tồn tại độc lập) | hình thoi rỗng |
| Kết hợp | `Order` 0..* → 1 `Table`, `OrderItem` 0..* → 1 `MenuItem` | mũi tên nét liền |
| Phụ thuộc | `BillCalculator` dùng `DiscountStrategy`, tạo `Bill` | mũi tên nét đứt |

Sửa file `.mmd`: dán vào https://mermaid.live, hoặc cài extension "Markdown Preview Mermaid Support" trong VS Code. Sửa xong xuất lại ảnh bằng lệnh:

```bash
mmdc -i 1-lop-model.mmd -o 1-lop-model.png -c mermaid-config.json -b white -s 2
```

Hình use case vẽ trực tiếp bằng SVG, mở được bằng trình duyệt, Inkscape hoặc draw.io.
