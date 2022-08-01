import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VendingMachineTests {
    private VendingMachine vendingMachine = new VendingMachine(new User("Test"),true);
    @Test
    public void testChange(){
        LinkedHashMap<String,Integer> cents = new LinkedHashMap<String,Integer>();
        cents.put("5000",0);
        cents.put("2000",0);
        cents.put("1000",0);
        cents.put("500",1);
        cents.put("200",1);
        cents.put("100",2);
        cents.put("50",3);
        cents.put("20",0);
        cents.put("10",12);
        cents.put("5",10);
        cents.put("1",0);
        vendingMachine.setCentsInInventory(cents);
        assert(vendingMachine.giveChange(100));
        assert(!vendingMachine.giveChange(101));
        assert(vendingMachine.giveChange(355));
        assert(!vendingMachine.giveChange(1));
        assert(vendingMachine.giveChange(90));
        assert(vendingMachine.giveChange(500));
    }
    @Test
    public void testBuy() throws IOException {
        LinkedHashMap<String,Integer> cents = new LinkedHashMap<String,Integer>();
        cents.put("200",1);
        cents.put("100",2);
        cents.put("50",3);
        cents.put("20",0);
        cents.put("10",12);
        cents.put("5",10);
        cents.put("1",0);
        vendingMachine.setCentsInInventory(cents);
        LinkedHashMap<Product,Integer> products = new LinkedHashMap<>();
        Product p1 = new Product(3.5,"D12");
        Product p2= new Product(12.55,"E1");
        Product p3 = new Product(6.54,"A20");
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p3);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.insertMoney(50);
        assert(vendingMachine.buyProduct(p1.getCode()));
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        assert(vendingMachine.buyProduct(p2.getCode()));
        assert(!vendingMachine.buyProduct(p3.getCode()));
        assert(!vendingMachine.buyProduct(null));
        vendingMachine.insertMoney(1000);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(50);
        vendingMachine.insertMoney(5);
        assert(vendingMachine.buyProduct(p2.getCode()));
        products.put(p1,10);
        products.put(p2,10);
        vendingMachine.getStatus();
    }
    @Test
    public void testLoadingProducts(){
        Product p1 = new Product(12.5,"D12");
        Product p2 = new Product(1,"E2");
        Product p3 = new Product(2.5,"A1");
        Product p4 = new Product(5.3,"F8");
        Product p5 = new Product(7.2,"D5");
        Product p6 = new Product(8.9,"C9");
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p3);
        assertTrue(vendingMachine.productsInInventory.size() == 3);
        vendingMachine.unloadProduct(p3);
        vendingMachine.unloadProduct(p2);
        assertTrue(vendingMachine.productsInInventory.size() == 1);
        assertTrue(vendingMachine.productsInInventory.containsKey(p1));
    }
    @Test
    public void testLoadingMoney(){
        LinkedHashMap<String,Integer> cents = new LinkedHashMap<String,Integer>();
        cents.put("5000",0);
        cents.put("2000",0);
        cents.put("1000",0);
        cents.put("500",1);
        cents.put("200",1);
        cents.put("100",2);
        cents.put("50",3);
        cents.put("20",0);
        cents.put("10",12);
        cents.put("5",10);
        cents.put("1",0);
        vendingMachine.setCentsInInventory(cents);
    }
}
