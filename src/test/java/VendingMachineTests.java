import com.domain.*;
import com.exceptions.*;
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class VendingMachineTests {
    private final Admin admin = new Admin("Test", "nuj");
    private final VendingMachine vendingMachine = new VendingMachine(new Admin("Test", "nuj"), true);
    private final VendingMachine nonAdminVendingMachine = new VendingMachine(new Admin("Test", "nuj"), true);
    private final SnacksVendingMachine snacksVendingMachine = new SnacksVendingMachine(new Admin("Test", "nuj"));
    private final SodasVendingMachine sodasVendingMachine = new SodasVendingMachine(new Admin("Test", "nuj"));
    private final UtilitiesVendingMachine utilitiesVendingMachine = new UtilitiesVendingMachine(new Admin("Test", "nuj"));

    @Test
    public void testChange() throws NoAdminPrivileges, NotEnoughMoney, TooMuchMoney, InvalidCredentials {
        LinkedHashMap<String, Integer> cents;
        cents = Utils.formatHashMap(0, 0, 0, 1, 1, 2, 3, 0, 12, 10, 0);
        vendingMachine.login(admin);
        vendingMachine.loadMoney(cents);
        assertTrue(vendingMachine.giveChange(100));
        Exception exception = assertThrows(NotEnoughMoney.class, () -> vendingMachine.giveChange(101));
        assertSame("Not enough money in inventory for giving change!", exception.getMessage());
        assertTrue(vendingMachine.giveChange(355));
        Exception secondException = assertThrows(NotEnoughMoney.class, () -> vendingMachine.giveChange(1));
        assertSame("Not enough money in inventory for giving change!", secondException.getMessage());
        assertTrue(vendingMachine.giveChange(90));
        assertTrue(vendingMachine.giveChange(500));
    }

    @Test
    public void testBuy() throws IOException, ProductNotFound, TooManyProducts, NotEnoughMoney, InvalidCurrency, NoAdminPrivileges, InvalidProductType, TooMuchMoney, InvalidCredentials {
        LinkedHashMap<String, Integer> cents;
        vendingMachine.login(admin);
        cents = Utils.formatHashMap(0, 0, 0, 0, 1, 2, 3, 0, 12, 10, 0);
        vendingMachine.loadMoney(cents);
        Product p1 = new Product(3.5, "D12", "Coke");
        Product p2 = new Product(12.55, "E1", "Cigar");
        Product p3 = new Product(6.54, "A20", "Nuj");
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p2);
        vendingMachine.loadProduct(p3);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.insertMoney(50);
        assertTrue(vendingMachine.buyProduct(p1.getCode(), false));
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        assertTrue(vendingMachine.buyProduct(p2.getCode(), false));
        assertThrows(NotEnoughMoney.class, () -> vendingMachine.buyProduct(p3.getCode(), false));
        assertThrows(ProductNotFound.class, () -> vendingMachine.buyProduct(null, false));
        vendingMachine.insertMoney(1000);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(50);
        vendingMachine.insertMoney(5);
        assertTrue(vendingMachine.buyProduct(p2.getCode(), true));
        vendingMachine.getStatus();
    }

    @Test
    public void testLoadingProducts() throws TooManyProducts, ProductNotFound, NoAdminPrivileges, InvalidProductType, InvalidCredentials {
        Product p1 = new Product(12.5, "D12", null);
        Product p2 = new Product(1, "E2", null);
        Product p3 = new Product(2.5, "A1", null);
        vendingMachine.login(admin);
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
    public void testLoadingMoney() throws NoAdminPrivileges, TooMuchMoney, InvalidCredentials {
        LinkedHashMap<String, Integer> cents;
        vendingMachine.login(admin);
        cents = Utils.formatHashMap(1, 1, 10, 1, 1, 2, 3, 1, 12, 10, 1);
        vendingMachine.loadMoney(cents);
        nonAdminVendingMachine.login(new User("enrique"));
        nonAdminVendingMachine.setCentsInInventory(cents);
        vendingMachine.unloadMoney();
        assertThrows(NoAdminPrivileges.class, nonAdminVendingMachine::unloadMoney);
        assertThrows(InvalidCurrency.class, () -> vendingMachine.insertMoney(13));
        LinkedHashMap<String, Integer> zeroCents;
        zeroCents = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(vendingMachine.getCentsInInventory(), zeroCents);
        assertNotEquals(nonAdminVendingMachine.getCentsInInventory(), zeroCents);
    }

    @Test
    public void testUsers() throws ProductNotFound, TooManyProducts, NotEnoughMoney, NoAdminPrivileges, InvalidProductType, TooMuchMoney, InvalidCredentials {
        User user = new User("pablo");
        vendingMachine.login(admin);
        LinkedHashMap<String, Integer> cents;
        cents = Utils.formatHashMap(1, 1, 10, 1, 1, 3, 4, 2, 14, 10, 1);
        LinkedHashMap<String, Integer> zeroCents;
        zeroCents = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        vendingMachine.loadMoney(cents);
        user.setUserWallet(cents);
        vendingMachine.loadProduct(new Product(70.11, "D12", "Tigari de foi"));
        vendingMachine.loadProduct(new Product(100.15, "E12", "Tigari cu aur"));
        vendingMachine.loadProduct(new Product(9.85, "A2", "Tigari cu foi"));
        vendingMachine.loadProduct(new Product(2.1, "F3", "Tigari"));
        vendingMachine.loadProduct(new Product(2.1, "F3", "Tigari"));
        vendingMachine.logOut();
        vendingMachine.login(user);
        vendingMachine.setCentsAddedByUser(cents);
        vendingMachine.buyProduct("D12", false);
        vendingMachine.buyProduct("E12", false);
        vendingMachine.buyProduct("A2", false);
        vendingMachine.buyProduct("F3", false);
        vendingMachine.buyProduct("F3", true);
        assertEquals(user.getUserWallet(), zeroCents);
        LinkedHashMap<String, Integer> userTransactions = new LinkedHashMap<>();
        userTransactions.put("Tigari de foi", 1);
        userTransactions.put("Tigari cu aur", 1);
        userTransactions.put("Tigari cu foi", 1);
        userTransactions.put("Tigari", 2);
        assertEquals(user.getTransactions().keySet(), userTransactions.keySet());
    }

    @Test
    public void testCancellingTransaction() throws InvalidCurrency, NoAdminPrivileges, TooMuchMoney, InvalidCredentials {
        User user = new User("test");
        LinkedHashMap<String, Integer> userWallet;
        userWallet = Utils.formatHashMap(0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0);
        LinkedHashMap<String, Integer> copyUserWallet = new LinkedHashMap<>(userWallet);
        vendingMachine.login(new Admin("Test", "nuj"));
        vendingMachine.loadMoney(userWallet);
        vendingMachine.logOut();
        vendingMachine.login(user);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.cancelTransaction();
        assertEquals(userWallet, copyUserWallet);
    }

    @Test
    public void testProductTypes() throws InvalidProductType, NoAdminPrivileges, TooManyProducts, InvalidCredentials {
        snacksVendingMachine.login(admin);
        sodasVendingMachine.login(admin);
        utilitiesVendingMachine.login(admin);
        Product p1 = new Product(2.5, "D12", "Coke");
        Exception exception = assertThrows(InvalidProductType.class, () -> snacksVendingMachine.loadProduct(p1));
        assertEquals("Invalid product type! Type snack was expected!", exception.getMessage());
        sodasVendingMachine.loadProduct(p1);
        assertEquals(1, sodasVendingMachine.getProductsInInventory().size());
        Exception secondException = assertThrows(InvalidProductType.class, () -> utilitiesVendingMachine.loadProduct(p1));
        assertEquals("Invalid product type! Type utility was expected!", secondException.getMessage());
    }

    @Test
    public void testTakeProfits() throws InvalidCredentials, NotEnoughMoney, NoAdminPrivileges {
        vendingMachine.login(admin);
        LinkedHashMap<String, Integer> cents = Utils.formatHashMap(11, 10, 5, 3, 2, 1, 5, 5, 5, 5, 5);
        vendingMachine.setCentsInInventory(cents);
        assertTrue(vendingMachine.takeProfits());
        Exception exception = assertThrows(NotEnoughMoney.class, vendingMachine::takeProfits);
        assertEquals("There are no profits!", exception.getMessage());
    }

    @Test
    public void testCustomTest() throws InvalidCredentials, InvalidProductType, NoAdminPrivileges, TooManyProducts {
        /*
        Admin - restock / take supplementary money and refill 5x each, check status
        U1 to VM1 selects A3,B2,A1,B2 -> pays all his money -> rest 3x5c
        U2 to VM3 selects A3, A1, A3, A2 -> pays all -> rest 3x1E 1x20c
        U2 then goes to VM2 and selects C3,A3,A3,A3,C1 -> pays 3x1E -> rest 1x50c, 1x20c, 1x5c
        U2 to VM1 A1, C3 -> no money
        U3 to VM1 C1 pays exact amount
        U3 to VM2 selects A2, B2 pays with 2E gets 1x 50c 1x 10c
        U3 to VM3 select A4 pays only 50c , then pays correct 55c, remains with 5c
        admin takes money and resets money and products
         */
        User u1 = new User("User1");
        User u2 = new User("User2");
        User u3 = new User("User3");
        LinkedHashMap<String,Integer> c1 = Utils.formatHashMap(0,0,0,0,0,2,1,1,0,0,0);
        LinkedHashMap<String,Integer> c2 = Utils.formatHashMap(0,1,0,0,0,0,0,0,0,0,0);
        LinkedHashMap<String,Integer> c3 = Utils.formatHashMap(0,0,0,0,1,1,0,0,0,6,0);
        snacksVendingMachine.login(admin);
        utilitiesVendingMachine.login(admin);
        sodasVendingMachine.login(admin);
        Product coke = new Product(1.3,"A1","COKE");
        Product fanta = new Product(1.2,"A2","FANTA");
        Product secondFanta = new Product(1.2,"B2","FANTA");
        Product sprite = new Product(1.25,"C1","SPRITE");
        Product secondSprite = new Product(1.25,"C3","SPRITE");
        Product peanuts = new Product(.6,"A2","PEANUTS");
        Product secondPeanuts = new Product(.6,"C1","PEANUTS");
        Product chips = new Product(.8,"B2","CHIPS");
        Product skittles = new Product(.55,"A3","SKITTLES");
        sodasVendingMachine.loadProduct(coke);
        sodasVendingMachine.loadProduct(fanta);
        sodasVendingMachine.loadProduct(secondFanta);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(secondSprite);
        snacksVendingMachine.loadProduct(peanuts);
        snacksVendingMachine.loadProduct(secondPeanuts);
        snacksVendingMachine.loadProduct(chips);
        snacksVendingMachine.loadProduct(chips);
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
    }
}
