package restaurant.common;

/**
 * Định dạng số tiền (đơn vị đồng) theo kiểu Việt Nam, ví dụ {@code 185000 -> "185.000 đ"}.
 * Không dùng {@link java.util.Locale} mặc định của máy, để kết quả giống nhau trên mọi máy.
 */
public final class MoneyFormat {

    private MoneyFormat() {
    }

    /**
     * Định dạng một số tiền.
     *
     * @param amount số tiền tính bằng đồng, có thể âm (dòng tiền giảm trên hóa đơn)
     * @return chuỗi đã nhóm 3 chữ số bằng dấu chấm, kèm hậu tố " đ"
     */
    public static String format(long amount) {
        String digits = Long.toString(Math.abs(amount));
        StringBuilder grouped = new StringBuilder();
        int firstGroup = digits.length() % 3;
        if (firstGroup == 0) {
            firstGroup = 3;
        }
        grouped.append(digits, 0, firstGroup);
        for (int i = firstGroup; i < digits.length(); i += 3) {
            grouped.append('.').append(digits, i, i + 3);
        }
        return (amount < 0 ? "-" : "") + grouped + " đ";
    }
}
