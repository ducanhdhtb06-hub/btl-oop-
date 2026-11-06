package restaurant.exception;

/** Bàn đang có khách nên không mở đơn mới được. */
public class TableNotAvailableException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param tableNo số bàn
     */
    public TableNotAvailableException(int tableNo) {
        super("Bàn " + tableNo + " đang có khách");
    }
}
