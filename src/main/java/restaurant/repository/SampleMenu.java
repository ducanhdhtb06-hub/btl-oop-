package restaurant.repository;

import java.util.List;
import restaurant.model.Combo;
import restaurant.model.Drink;
import restaurant.model.Food;
import restaurant.model.MenuItem;

/** Thực đơn mẫu, dùng khi chưa có file menu.csv. */
public final class SampleMenu {

    private SampleMenu() {
    }

    /**
     * Tạo thực đơn mẫu mới (mỗi lần gọi là các đối tượng mới).
     *
     * @return danh sách món mẫu (combo đứng sau các món thành phần), Trà đào đang hết để demo
     */
    public static List<MenuItem> items() {
        Food comGa = new Food("F02", "Cơm gà", 50_000, true, false);
        Food goiCuon = new Food("F04", "Gỏi cuốn chay", 35_000, true, true);
        Food dauHu = new Food("F05", "Đậu hũ sốt cà chua", 40_000, true, true);
        Drink traDa = new Drink("D01", "Trà đá", 5_000, true, 300);
        Drink nuocCam = new Drink("D03", "Nước cam", 30_000, true, 330);
        return List.of(
                new Food("F01", "Phở bò", 55_000, true, false),
                comGa,
                new Food("F03", "Bún chả", 50_000, true, false),
                goiCuon,
                dauHu,
                traDa,
                new Drink("D02", "Cà phê sữa đá", 25_000, true, 250),
                nuocCam,
                new Drink("D04", "Trà đào", 35_000, false, 500),
                new Combo("C01", "Combo trưa", true, 10, List.of(comGa, traDa)),
                new Combo("C02", "Combo chay", true, 15, List.of(goiCuon, dauHu, nuocCam)));
    }
}
