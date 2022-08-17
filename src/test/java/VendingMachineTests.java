import com.domain.*;
import com.exceptions.*;
import com.utils.Utils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;

import static com.utils.Utils.zeroMoney;
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
        LinkedHashMap<String, Integer> money;
        money = Utils.formatHashMap(0, 0, 0, 1, 1, 2, 3, 0, 12, 10, 0);
        vendingMachine.login(admin);
        vendingMachine.loadMoney(money);
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
        LinkedHashMap<String, Integer> money;
        vendingMachine.login(admin);
        money = Utils.formatHashMap(0, 0, 0, 0, 1, 2, 3, 0, 12, 10, 0);
        vendingMachine.loadMoney(money);
        Product p1 = new Product(3.5, "D5", "Coke");
        Product p2 = new Product(12.55, "E1", "Cigar");
        Product p3 = new Product(6.54, "A6", "Nuj");
        admin.addCoinsToWallet(1000,1);
        admin.addCoinsToWallet(200,8);
        admin.addCoinsToWallet(100,2);
        admin.addCoinsToWallet(50,2);
        admin.addCoinsToWallet(5,1);
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
        assertThrows(ProductNotFound.class, () -> vendingMachine.buyProduct("A1", false));
        Exception e =  assertThrows(ProductNotFound.class, () -> vendingMachine.buyProduct("H1", false));
        assertEquals(e.getMessage(),"Invalid code!");
        vendingMachine.insertMoney(1000);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(50);
        vendingMachine.insertMoney(5);
        assertTrue(vendingMachine.buyProduct(p2.getCode(), true));
        vendingMachine.getStatus("output.txt");
    }

    @Test
    public void testLoadingProducts() throws TooManyProducts, ProductNotFound, NoAdminPrivileges, InvalidProductType, InvalidCredentials {
        Product p1 = new Product(12.5, "D5", null);
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
        LinkedHashMap<String, Integer> money;
        vendingMachine.login(admin);
        money = Utils.formatHashMap(1, 1, 10, 1, 1, 2, 3, 1, 12, 10, 1);
        vendingMachine.loadMoney(money);
        nonAdminVendingMachine.login(new User("enrique"));
        nonAdminVendingMachine.setMoneyInInventory(money);
        vendingMachine.unloadMoney();
        assertThrows(NoAdminPrivileges.class, nonAdminVendingMachine::unloadMoney);
        assertThrows(InvalidCurrency.class, () -> vendingMachine.insertMoney(13));
        assertEquals(vendingMachine.getMoneyInInventory(), zeroMoney);
        assertNotEquals(nonAdminVendingMachine.getMoneyInInventory(), zeroMoney);
    }

    @Test
    public void testUsers() throws ProductNotFound, TooManyProducts, NotEnoughMoney, NoAdminPrivileges, InvalidProductType, TooMuchMoney, InvalidCurrency, InvalidCredentials {
        User user = new User("pablo");
        vendingMachine.login(admin);
        LinkedHashMap<String, Integer> money;
        money = Utils.formatHashMap(1, 1, 10, 1, 1, 3, 4, 2, 14, 10, 1);
        vendingMachine.loadMoney(money);
        user.setUserWallet(money);
        vendingMachine.loadProduct(new Product(70.11, "D5", "Tigari de foi"));
        vendingMachine.loadProduct(new Product(100.15, "E5", "Tigari cu aur"));
        vendingMachine.loadProduct(new Product(9.85, "A2", "Tigari cu foi"));
        vendingMachine.loadProduct(new Product(2.1, "F3", "Tigari"));
        vendingMachine.loadProduct(new Product(2.1, "F3", "Tigari"));
        vendingMachine.logOut();
        vendingMachine.login(user);
        vendingMachine.setMoneyAddedByUser(money);
        vendingMachine.buyProduct("D5", false);
        vendingMachine.buyProduct("E5", false);
        vendingMachine.buyProduct("A2", false);
        vendingMachine.buyProduct("F3", false);
        vendingMachine.buyProduct("F3", true);
        assertEquals(user.getUserWallet(), zeroMoney);
        LinkedHashMap<String, Integer> userTransactions = new LinkedHashMap<>();
        userTransactions.put("Tigari de foi", 1);
        userTransactions.put("Tigari cu aur", 1);
        userTransactions.put("Tigari cu foi", 1);
        userTransactions.put("Tigari", 2);
        assertEquals(user.getTransactions().keySet(), userTransactions.keySet());
    }

    @Test
    public void testCancellingTransaction() throws InvalidCurrency, NoAdminPrivileges, TooMuchMoney, NotEnoughMoney, InvalidCredentials {
        User user = new User("test");
        LinkedHashMap<String, Integer> userWallet;
        userWallet = Utils.formatHashMap(0, 0, 0, 0, 2, 1, 0, 0, 0, 0, 0);
        LinkedHashMap<String, Integer> copyUserWallet = new LinkedHashMap<>(userWallet);
        vendingMachine.login(new Admin("Test", "nuj"));
        vendingMachine.loadMoney(userWallet);
        vendingMachine.logOut();
        vendingMachine.login(user);
        user.setUserWallet(userWallet);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(200);
        vendingMachine.insertMoney(100);
        vendingMachine.finaliseTransaction();
        assertEquals(userWallet, copyUserWallet);
    }

    @Test
    public void testProductTypes() throws InvalidProductType, NoAdminPrivileges, TooManyProducts, InvalidCredentials {
        snacksVendingMachine.login(admin);
        sodasVendingMachine.login(admin);
        utilitiesVendingMachine.login(admin);
        Product p1 = new Product(2.5, "D5", "Coke");
        Exception exception = assertThrows(InvalidProductType.class, () -> snacksVendingMachine.loadProduct(p1));
        assertEquals("Invalid product type! Type snack was expected!", exception.getMessage());
        sodasVendingMachine.loadProduct(p1);
        assertEquals(1, sodasVendingMachine.getProductsInInventory().size());
        Exception secondException = assertThrows(InvalidProductType.class, () -> utilitiesVendingMachine.loadProduct(p1));
        assertEquals("Invalid product type! Type utility was expected!", secondException.getMessage());
    }

    @Test
    public void testTakeProfits() throws NotEnoughMoney, NoAdminPrivileges, InvalidCredentials {
        vendingMachine.login(admin);
        LinkedHashMap<String, Integer> money = Utils.formatHashMap(11, 10, 5, 3, 2, 1, 5, 5, 5, 5, 5);
        vendingMachine.setMoneyInInventory(money);
        assertTrue(vendingMachine.takeProfits());
        Exception exception = assertThrows(NotEnoughMoney.class, vendingMachine::takeProfits);
        assertEquals("There are no profits!", exception.getMessage());
    }

    @Test
    public void testUserStatus() throws InvalidProductType, NoAdminPrivileges, TooManyProducts, ProductNotFound, NotEnoughMoney, IOException, TooMuchMoney, InvalidCurrency, InvalidCredentials {
        User u1 = new User("User1");
        User u2 = new User("User2");
        User u3 = new User("User3");
        vendingMachine.login(admin);
        vendingMachine.loadProduct(new Product(1,"D5","COKE"));
        vendingMachine.loadProduct(new Product(2,"E3","BAKE_ROLLS"));
        vendingMachine.logOut();
        vendingMachine.login(u1);
        u1.addCoinsToWallet(100,1);
        u1.addCoinsToWallet(200,1);
        vendingMachine.insertMoney(100);
        vendingMachine.buyProduct("D5",true);
        vendingMachine.login(u2);
        u2.addCoinsToWallet(200,1);
        vendingMachine.insertMoney(200);
        vendingMachine.buyProduct("E3",true);
        u1.getStatus();
        u2.getStatus();
        u3.getStatus();
    }
    @Test
    public void testCustomTest() throws InvalidProductType, NoAdminPrivileges, TooManyProducts, ProductNotFound, NotEnoughMoney, TooMuchMoney, InvalidCurrency, IOException, InvalidCredentials {
        LinkedHashMap<String,Integer> cents = Utils.formatHashMap(10,10,10,10,10,10,10,10,10,10,10);
        User u1 = new User("User1");
        User u2 = new User("User2");
        User u3 = new User("User3");
        LinkedHashMap<String,Integer> c1 = Utils.formatHashMap(0,0,0,0,0,2,1,1,0,0,0);
        LinkedHashMap<String,Integer> c2 = Utils.formatHashMap(0,1,0,0,0,0,0,0,0,0,0);
        LinkedHashMap<String,Integer> c3 = Utils.formatHashMap(0,0,0,0,1,1,0,0,0,6,0);
        u1.setUserWallet(c1);
        u2.setUserWallet(c2);
        u3.setUserWallet(c3);
        snacksVendingMachine.login(admin);
        utilitiesVendingMachine.login(admin);
        sodasVendingMachine.login(admin);
        snacksVendingMachine.setMoneyInInventory(cents);
        sodasVendingMachine.setMoneyInInventory(cents);
        utilitiesVendingMachine.setMoneyInInventory(cents);
        //============================================================//
        Product coke = new Product(1.3,"A1","COKE");
        Product fanta = new Product(1.2,"A2","FANTA");
        Product secondFanta = new Product(1.2,"B2","FANTA");
        Product sprite = new Product(1.25,"C1","SPRITE");
        Product secondSprite = new Product(1.25,"C3","SPRITE");
        Product secondCoke = new Product(1.3,"B1","COKE");
        Product thirdSprite = new Product(1.25,"A3","SPRITE");
        //========================================================================//
        Product peanuts = new Product(.6,"A2","PEANUTS");
        Product secondPeanuts = new Product(.6,"C1","PEANUTS");
        Product chips = new Product(.8,"B2","CHIPS");
        Product skittles = new Product(.55,"A3","SKITTLES");
        Product secondChips = new Product(.8,"A2","CHIPS");
        Product secondSkittles = new Product(.55,"C1","SKITTLES");
        Product thirdSkittles = new Product(.55,"C3","SKITTLES");
        //===========================================================================//
        Product mask = new Product(1.8,"A1","FPP2_MASK");
        Product magnet = new Product(15,"A3","MAGNET");
        Product gum = new Product(0.55,"A4","GUM");
        Product cigar = new Product(2.5,"A2","CIGAR");
        //====================================================================//
        for(int i=0;i<5;i++){
            snacksVendingMachine.loadProduct(skittles);
            utilitiesVendingMachine.loadProduct(gum);
        }
        //===============================================//
        sodasVendingMachine.loadProduct(coke);
        sodasVendingMachine.loadProduct(coke);
        sodasVendingMachine.loadProduct(fanta);
        sodasVendingMachine.loadProduct(secondFanta);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(secondSprite);
        //==============================================//
        snacksVendingMachine.loadProduct(peanuts);
        snacksVendingMachine.loadProduct(secondPeanuts);
        snacksVendingMachine.loadProduct(chips);
        snacksVendingMachine.loadProduct(chips);
        //================================================//
        utilitiesVendingMachine.loadProduct(mask);
        utilitiesVendingMachine.loadProduct(mask);
        utilitiesVendingMachine.loadProduct(magnet);
        //=============================================//
        snacksVendingMachine.logOut();
        sodasVendingMachine.logOut();
        utilitiesVendingMachine.logOut();
        sodasVendingMachine.login(u1);
        sodasVendingMachine.insertMoney(100);
        sodasVendingMachine.insertMoney(100);
        sodasVendingMachine.insertMoney(20);
        sodasVendingMachine.insertMoney(50);
        assertThrows(ProductNotFound.class,()-> sodasVendingMachine.buyProduct("A3",false));
        sodasVendingMachine.buyProduct("B2",false);
        sodasVendingMachine.buyProduct("A1",false);
        assertThrows(ProductNotFound.class,()-> sodasVendingMachine.buyProduct("B2",true));
        sodasVendingMachine.finaliseTransaction();
        assertEquals(1, (int) u1.getUserWallet().get("20"));
        //========================================================================================//
        utilitiesVendingMachine.login(u2);
        utilitiesVendingMachine.insertMoney(2000);
        utilitiesVendingMachine.buyProduct("A3",false);
        utilitiesVendingMachine.buyProduct("A1",false);
        assertThrows(ProductNotFound.class,()->utilitiesVendingMachine.buyProduct("A2",false));
        assertThrows(ProductNotFound.class,()->utilitiesVendingMachine.buyProduct("A3",false));
        utilitiesVendingMachine.finaliseTransaction();
        assertEquals(1,u2.getUserWallet().get("200"));
        assertEquals(1,u2.getUserWallet().get("100"));
        assertEquals(1,u2.getUserWallet().get("20"));
        snacksVendingMachine.login(u2);
        snacksVendingMachine.insertMoney(200);
        snacksVendingMachine.insertMoney(100);
        assertThrows(ProductNotFound.class,()-> snacksVendingMachine.buyProduct("C3",false));
        snacksVendingMachine.buyProduct("A3",false);
        snacksVendingMachine.buyProduct("A3",false);
        snacksVendingMachine.buyProduct("A3",false);
        snacksVendingMachine.buyProduct("C1",true);
        assertEquals(1,u2.getUserWallet().get("50"));
        assertEquals(2,u2.getUserWallet().get("20"));
        assertEquals(1,u2.getUserWallet().get("5"));
        sodasVendingMachine.login(u2);
        sodasVendingMachine.insertMoney(50);
        sodasVendingMachine.insertMoney(20);
        sodasVendingMachine.insertMoney(20);
        sodasVendingMachine.insertMoney(5);
        assertThrows(NotEnoughMoney.class,()-> sodasVendingMachine.buyProduct("A1",false));
        assertThrows(NotEnoughMoney.class,()-> sodasVendingMachine.buyProduct("C3",true));
        sodasVendingMachine.finaliseTransaction();
        assertEquals(1,u2.getUserWallet().get("50"));
        assertEquals(2,u2.getUserWallet().get("20"));
        assertEquals(1,u2.getUserWallet().get("5"));
        //=========================================================================================//
        sodasVendingMachine.login(u3);
        sodasVendingMachine.insertMoney(100);
        sodasVendingMachine.insertMoney(5);
        sodasVendingMachine.insertMoney(5);
        sodasVendingMachine.insertMoney(5);
        sodasVendingMachine.insertMoney(5);
        sodasVendingMachine.insertMoney(5);
        sodasVendingMachine.buyProduct("C3",true);
        assertEquals(u3.getUserWallet().get("100"),0);
        assertEquals(u3.getUserWallet().get("200"),1);
        assertEquals(u3.getUserWallet().get("5"),1);
        snacksVendingMachine.login(u3);
        snacksVendingMachine.insertMoney(200);
        snacksVendingMachine.buyProduct("A2",false);
        snacksVendingMachine.buyProduct("B2",true);
        assertEquals(1,u3.getUserWallet().get("50"));
        assertEquals(1,u3.getUserWallet().get("10"));
        utilitiesVendingMachine.login(u3);
        utilitiesVendingMachine.insertMoney(50);
        assertThrows(NotEnoughMoney.class,()->utilitiesVendingMachine.buyProduct("A4",false));
        utilitiesVendingMachine.insertMoney(10);
        utilitiesVendingMachine.buyProduct("A4",true);
        assertEquals(u3.getUserWallet().get("5"),2);
        //==================================================================//
        admin.setUserWallet(zeroMoney);
        snacksVendingMachine.login(admin);
        utilitiesVendingMachine.login(admin);
        sodasVendingMachine.login(admin);
        System.out.println(snacksVendingMachine.getMoneyInInventory());
        System.out.println(sodasVendingMachine.getMoneyInInventory());
        System.out.println(utilitiesVendingMachine.getMoneyInInventory());
        sodasVendingMachine.getStatus();
        snacksVendingMachine.getStatus();
        utilitiesVendingMachine.getStatus();
        sodasVendingMachine.takeProfits();
        snacksVendingMachine.takeProfits();
        utilitiesVendingMachine.takeProfits();
        //====================================================//
        snacksVendingMachine.setMoneyInInventory(cents);
        sodasVendingMachine.setMoneyInInventory(cents);
        utilitiesVendingMachine.setMoneyInInventory(cents);
        for(int i=0;i<5;i++)
        {
            sodasVendingMachine.loadProduct(secondCoke);
            sodasVendingMachine.loadProduct(secondFanta);
            sodasVendingMachine.loadProduct(thirdSprite);
            sodasVendingMachine.loadProduct(secondSprite);
            //============================================//
            snacksVendingMachine.loadProduct(peanuts);
            snacksVendingMachine.loadProduct(secondChips);
            snacksVendingMachine.loadProduct(secondSkittles);
            snacksVendingMachine.loadProduct(thirdSkittles);
            //==============================================//
            utilitiesVendingMachine.loadProduct(cigar);
            utilitiesVendingMachine.loadProduct(magnet);
            if(i!=4){
                sodasVendingMachine.loadProduct(coke);
                sodasVendingMachine.loadProduct(fanta);
                //=======================================//
                snacksVendingMachine.loadProduct(chips);
                //========================================//
                utilitiesVendingMachine.loadProduct(mask);
            }
        }
        sodasVendingMachine.loadProduct(sprite);
        sodasVendingMachine.loadProduct(sprite);
        //======================================//
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
        snacksVendingMachine.loadProduct(skittles);
        //=============================================//
        utilitiesVendingMachine.loadProduct(gum);
        //=============================================//
        sodasVendingMachine.getStatus();
        snacksVendingMachine.getStatus();
        utilitiesVendingMachine.getStatus();
    }
    @Test
    public void testTooManyProducts() throws InvalidProductType, NoAdminPrivileges, TooManyProducts, InvalidCredentials {
        Product p1 = new Product(3.5, "D5", "Coke");
        vendingMachine.login(admin);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        vendingMachine.loadProduct(p1);
        assertThrows(TooManyProducts.class,()->vendingMachine.loadProduct(p1));
    }
    @Test
    public void testTooMuchMoney() throws InvalidCredentials {
        LinkedHashMap<String,Integer> oneHundred = Utils.formatHashMap(100,100,100,100,100,100,100,100,100,100,100);
        admin.setUserWallet(oneHundred);
        vendingMachine.setMoneyInInventory(oneHundred);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(5000));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(2000));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(1000));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(500));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(200));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(100));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(50));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(20));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(10));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(5));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.insertMoney(1));
        vendingMachine.login(admin);
        assertThrows(TooMuchMoney.class,()->vendingMachine.loadMoney(oneHundred));
    }
    @Test
    public void testCredentials(){
        Admin falseAdmin = new Admin("False","false");
        assertThrows(InvalidCredentials.class,()->vendingMachine.login(falseAdmin));
    }

}
