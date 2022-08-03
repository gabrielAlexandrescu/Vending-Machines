import com.Domain.*;
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class VendingMachineTests {
    private final VendingMachine vendingMachine = new VendingMachine(new Admin("Test","nuj"),true);
    private final VendingMachine nonAdminVendingMachine = new VendingMachine(new User("Test"),true);


    @Test
    public void testChange(){
        LinkedHashMap<String,Integer> cents;
        cents = Utils.formatHashMap(0,0,0,1,1,2,3,0,12,10,0);
        vendingMachine.loadMoney(cents);
        assertTrue(vendingMachine.giveChange(100));
        assertFalse(vendingMachine.giveChange(101));
        assertTrue(vendingMachine.giveChange(355));
        assertFalse(vendingMachine.giveChange(1));
        assertTrue(vendingMachine.giveChange(90));
        assertTrue(vendingMachine.giveChange(500));
    }
    @Test
    public void testBuy() throws IOException {
        LinkedHashMap<String,Integer> cents;
        cents = Utils.formatHashMap(0,0,0,0,1,2,3,0,12,10,0);
        vendingMachine.loadMoney(cents);
        Product p1 = new Product(3.5,"D12",null);
        Product p2= new Product(12.55,"E1",null);
        Product p3 = new Product(6.54,"A20",null);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p3);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.insertMoney(50);
        assertTrue(vendingMachine.buyProduct(p1.getCode(),false));
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        assertTrue(vendingMachine.buyProduct(p2.getCode(),false));
        assertFalse(vendingMachine.buyProduct(p3.getCode(), false));
        assertFalse(vendingMachine.buyProduct(null, false));
        vendingMachine.insertMoney(1000);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(50);
        vendingMachine.insertMoney(5);
        assertTrue(vendingMachine.buyProduct(p2.getCode(),true));
        vendingMachine.getStatus();
    }
    @Test
    public void testLoadingProducts(){
        Product p1 = new Product(12.5,"D12",null);
        Product p2 = new Product(1,"E2",null);
        Product p3 = new Product(2.5,"A1",null);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p3);
        assertEquals(3, vendingMachine.getProductsInInventory().size());
        vendingMachine.unloadProduct(p3);
        vendingMachine.unloadProduct(p2);
        assertEquals(1, vendingMachine.getProductsInInventory().size());
        assertTrue(vendingMachine.getProductsInInventory().containsKey(p1));
    }
    @Test
    public void testLoadingMoney(){
        LinkedHashMap<String,Integer> cents;
        cents = Utils.formatHashMap(1,1,10,1,1,2,3,1,12,10,1);
        vendingMachine.loadMoney(cents);
        nonAdminVendingMachine.setCentsInInventory(cents);
        vendingMachine.unloadMoney();
        nonAdminVendingMachine.unloadMoney();
        assertFalse(vendingMachine.insertMoney(13));
        LinkedHashMap<String,Integer> zeroCents;
        zeroCents = Utils.formatHashMap(0,0,0,0,0,0,0,0,0,0,0);
        assertEquals(vendingMachine.getCentsInInventory(),zeroCents);
        assertNotEquals(nonAdminVendingMachine.getCentsInInventory(),zeroCents);
    }
    @Test
    public void testUsers(){
        User user = new User("pablo");
        LinkedHashMap<String,Integer> cents;
        cents = Utils.formatHashMap(1,1,10,1,1,2,3,2,13,10,1);
        LinkedHashMap<String,Integer> zeroCents;
        zeroCents = Utils.formatHashMap(0,0,0,0,0,0,0,0,0,0,0);
        vendingMachine.loadMoney(cents);
        user.setUserWallet(cents);
        vendingMachine.loadProduct(new Product(70.11,"D12","Tigari de foi"));
        vendingMachine.loadProduct(new Product(100.15,"E12","Tigari cu aur"));
        vendingMachine.loadProduct(new Product(9.85,"A2","Tigari cu foi"));
        vendingMachine.loadProduct(new Product(2.1,"F3","Tigari"));
        vendingMachine.loadProduct(new Product(2.1,"F3","Tigari"));
        vendingMachine.login(user);
        vendingMachine.setCentsAddedByUser(cents);
        vendingMachine.buyProduct("D12",false);
        vendingMachine.buyProduct("E12",false);
        vendingMachine.buyProduct("A2",false);
        vendingMachine.buyProduct("F3",false);
        vendingMachine.buyProduct("F3",true);
        assertEquals(user.getUserWallet(), zeroCents);
        LinkedHashMap<String,Integer> userTransactions = new LinkedHashMap<>();
        userTransactions.put("Tigari de foi",1);
        userTransactions.put("Tigari cu aur",1);
        userTransactions.put("Tigari cu foi",1);
        userTransactions.put("Tigari",2);
        assertEquals(user.getTransactions(),userTransactions);
    }
    @Test
    public void testCancellingTransaction(){
        User user = new User("test");
        LinkedHashMap<String,Integer> userWallet;
        userWallet = Utils.formatHashMap(0,0,0,0,2,1,0,0,0,0,0);
        LinkedHashMap<String,Integer> copyUserWallet = new LinkedHashMap<>(userWallet);
        vendingMachine.login(new Admin("test","test"));
        vendingMachine.loadMoney(userWallet);
        vendingMachine.login(user);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.cancelTransaction();
        assertEquals(userWallet,copyUserWallet);

    }
}
