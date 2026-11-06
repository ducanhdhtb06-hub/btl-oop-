package restaurant.model;

import java.util.Optional;
import java.util.function.Function;

/**
 * Tạo đúng lớp con của {@link MenuItem} từ một dòng CSV (pattern Factory).
 * Nơi gọi chỉ biết kiểu {@code MenuItem}, không phải biết có những lớp con nào.
 */
public final class MenuItemFactory {

    private MenuItemFactory() {
    }

    /**
     * Tạo món từ một dòng của file menu.csv: {@code type;id;name;value;available;detail}.
     *
     * @param row dòng CSV
     * @param lookup hàm tìm món đã đọc trước đó theo mã (dùng cho món chứa món khác)
     * @return món vừa tạo
     * @throws IllegalArgumentException nếu dòng sai định dạng, sai loại hoặc dữ liệu không hợp lệ
     */
    public static MenuItem fromCsvRow(String row, Function<String, Optional<MenuItem>> lookup) {
        String[] cols = row.split(";", -1);
        if (cols.length != 6) {
            throw new IllegalArgumentException("cần 6 cột nhưng có " + cols.length);
        }
        String type = cols[0].trim();
        String id = cols[1].trim();
        String name = cols[2].trim();
        long value = parseLong(cols[3]);
        boolean available = parseBoolean(cols[4]);
        return switch (type) {
            case "FOOD" -> new Food(id, name, value, available, parseBoolean(cols[5]));
            case "DRINK" -> new Drink(id, name, value, available, (int) parseLong(cols[5]));
            default -> throw new IllegalArgumentException("loại món không hợp lệ: " + type);
        };
    }

    private static long parseLong(String text) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("số không hợp lệ: " + text, e);
        }
    }

    private static boolean parseBoolean(String text) {
        String value = text.trim();
        if (value.equals("true")) {
            return true;
        }
        if (value.equals("false")) {
            return false;
        }
        throw new IllegalArgumentException("giá trị phải là true hoặc false: " + text);
    }
}
