package restaurant.model;

/** Loại món trên thực đơn; cũng là giá trị cột {@code type} trong file menu.csv. */
public enum ItemType {
    /** Món ăn. */
    FOOD,
    /** Đồ uống. */
    DRINK,
    /** Combo gồm nhiều món. */
    COMBO
}
