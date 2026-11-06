package restaurant.model;

/** Một bàn trong nhà hàng. */
public final class Table implements Identifiable<Integer> {

    private final int number;
    private final int seats;
    private TableStatus status = TableStatus.FREE;

    /**
     * Tạo bàn trống.
     *
     * @param number số bàn, từ 1 trở lên
     * @param seats số ghế, từ 1 trở lên
     * @throws IllegalArgumentException nếu số bàn hoặc số ghế nhỏ hơn 1
     */
    public Table(int number, int seats) {
        if (number < 1 || seats < 1) {
            throw new IllegalArgumentException("Số bàn và số ghế phải từ 1 trở lên");
        }
        this.number = number;
        this.seats = seats;
    }

    @Override
    public Integer getId() {
        return number;
    }

    /**
     * Trả về số bàn.
     *
     * @return số bàn
     */
    public int getNumber() {
        return number;
    }

    /**
     * Trả về số ghế.
     *
     * @return số ghế
     */
    public int getSeats() {
        return seats;
    }

    /**
     * Trả về trạng thái hiện tại.
     *
     * @return trạng thái bàn
     */
    public TableStatus getStatus() {
        return status;
    }

    /**
     * Cho biết bàn có trống không.
     *
     * @return {@code true} nếu bàn trống
     */
    public boolean isFree() {
        return status == TableStatus.FREE;
    }

    /**
     * Đánh dấu bàn có khách.
     *
     * @throws IllegalStateException nếu bàn đang có khách
     */
    public void occupy() {
        if (status == TableStatus.OCCUPIED) {
            throw new IllegalStateException("Bàn " + number + " đang có khách");
        }
        status = TableStatus.OCCUPIED;
    }

    /**
     * Trả bàn về trạng thái trống.
     *
     * @throws IllegalStateException nếu bàn đang trống
     */
    public void release() {
        if (status == TableStatus.FREE) {
            throw new IllegalStateException("Bàn " + number + " đang trống");
        }
        status = TableStatus.FREE;
    }
}
