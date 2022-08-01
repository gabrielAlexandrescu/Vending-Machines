public class Product {
    private double price;
    private String code;

    public Product(double price, String code) {
        this.price = price;

        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "price=" + price +
                ", code='" + code + '\'' +
                '}';
    }
}
