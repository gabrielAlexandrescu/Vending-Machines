package com.domain;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.exceptions.*;
import com.utils.Utils;

import static com.utils.Utils.zeroMoney;

public class VendingMachine {
    protected User user;
    protected boolean canTakeBills;
    protected final Admin admin;
    protected LinkedHashMap<String, Integer> moneyInInventory;
    protected LinkedHashMap<Product, Integer> productsInInventory;
    protected LinkedHashMap<String, Integer> moneyAddedByUser;
    protected LinkedHashMap<String, Integer> change;
    // The four hashmaps count money in cents:
    // 50 euros = 5000 cents
    // 20 euros = 2000 cents
    // 10 euros = 1000 cents
    // 5 euros = 500 cents
    // 2 euros = 200 cents
    // 1 euro = 100 cents

    protected LinkedHashMap<Product, Integer> transactions;
    protected final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public VendingMachine(Admin admin, boolean canTakeBills) {
        this.user = null;
        this.admin = admin;
        this.canTakeBills = canTakeBills;
        this.transactions = new LinkedHashMap<>();
        this.moneyInInventory = new LinkedHashMap<>();
        this.productsInInventory = new LinkedHashMap<>();
        this.moneyAddedByUser = new LinkedHashMap<>();
        this.change = new LinkedHashMap<>();
        moneyInInventory.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        moneyAddedByUser.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        change.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        moneyAddedByUser.put("Current money", 0);
    }

    public LinkedHashMap<Product, Integer> getProductsInInventory() {
        return productsInInventory;
    }

    public LinkedHashMap<String, Integer> getMoneyInInventory() {
        return moneyInInventory;
    }

    public boolean giveChange(int money) throws NotEnoughMoney {
        if (money == 0) {
            logger.log(Level.INFO, "No change needed!\n");
            return true;
        } else {
            if (checkIfChangePossible(money)) {
                for (Map.Entry<String, Integer> entry : change.entrySet()) {
                    user.addCoinsToWallet(Integer.parseInt(entry.getKey()), entry.getValue());
                    while (entry.getValue() > 0) {
                        entry.setValue(entry.getValue() - 1);
                        if (Integer.parseInt(entry.getKey()) < 100) {
                            logger.log(Level.INFO, "A coin of " + entry.getKey() + " euro-money has been returned!\n");
                        } else if (Integer.parseInt(entry.getKey()) == 100) {
                            logger.log(Level.INFO, "A coin of " + Integer.parseInt(entry.getKey()) / 100 + " euro has been returned!\n");
                        } else if (Integer.parseInt(entry.getKey()) == 200) {
                            logger.log(Level.INFO, "A coin of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!\n");
                        } else if (canTakeBills) {
                            logger.log(Level.INFO, "A bill of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!\n");
                        }
                    }
                }
                logger.log(Level.INFO, "Change was given\n");
                return true;
            } else {
                for (Map.Entry<String, Integer> entry : moneyInInventory.entrySet()) {
                    int value = change.get(entry.getKey());
                    entry.setValue(value + entry.getValue());
                    change.put(entry.getKey(), 0);
                }
                logger.log(Level.SEVERE, "Error", new NotEnoughMoney("Not enough money in inventory for giving change"));
                throw new NotEnoughMoney("Not enough money in inventory for giving change!");
            }
        }
    }


    private boolean checkIfChangePossible(int money) {
        for (Map.Entry<String, Integer> entry : moneyInInventory.entrySet()) {
            if (!(Integer.parseInt(entry.getKey()) > 500 && !canTakeBills)) {
                int maxCoin = Integer.parseInt(entry.getKey());
                while (money >= maxCoin && entry.getValue() > 0) {
                    change.put(String.valueOf(maxCoin), change.get(String.valueOf(maxCoin)) + 1);
                    money -= maxCoin;
                    entry.setValue(entry.getValue() - 1);
                }
            }
        }
        return money == 0;
    }

    private void checkIfMoneyValid(int money) throws InvalidCurrency, TooMuchMoney, NotEnoughMoney {
        try {
            if (moneyInInventory.get(String.valueOf(money)) >= 100) {
                finaliseTransaction();
                if (money > 200 && canTakeBills) {
                    logger.log(Level.SEVERE, "Error: ", new TooMuchMoney("The machine has too many bills of " + money / 100));
                    throw new TooMuchMoney("The machine has too many bills of " + money / 100);
                }
                logger.log(Level.SEVERE, "Error: ", new TooMuchMoney("The machine has too many coins of " + money + " cents"));
                throw new TooMuchMoney("The machine has too many coins of " + money + " cents");
            }
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Error: ", new InvalidCurrency());
            throw new InvalidCurrency();
        }
    }

    public void insertMoney(int money) throws InvalidCurrency, TooMuchMoney, NotEnoughMoney {
        try {
            checkIfMoneyValid(money);
        } catch (InvalidCurrency e) {
            throw new InvalidCurrency();
        } catch (TooMuchMoney e) {
            throw new TooMuchMoney("The machine has too many coins of " + money + " cents");
        }
        if (money == 1 || money == 5 || money == 10 || money == 20 || money == 50 || money == 100 || money == 200) {
            if (user.getUserWallet().get(String.valueOf(money)) <= 0) {
                finaliseTransaction();
                throw new NotEnoughMoney("The user doesn't have a coin of " + money + " to insert!");
            }
            moneyAddedByUser.put(String.valueOf(money), moneyAddedByUser.get(String.valueOf(money)) + 1);
            moneyAddedByUser.put("Current money", moneyAddedByUser.get("Current money") + money);
            user.removeCoinsFromWallet(money, 1);
            logger.log(Level.INFO, "User " + user.getUserName() + " added " + money + " euro-cents");
        } else if (canTakeBills && ((money == 500) || (money) == 1000 || (money == 2000) || (money == 5000))) {
            if (user.getUserWallet().get(String.valueOf(money)) <= 0) {
                finaliseTransaction();
                throw new NotEnoughMoney("The user doesn't have a bill of " + money + " to insert!");
            }
            moneyAddedByUser.put(String.valueOf(money), moneyAddedByUser.get(String.valueOf(money)) + 1);
            moneyAddedByUser.put("Current money", moneyAddedByUser.get("Current money") + money);
            user.removeCoinsFromWallet(money, 1);
            logger.log(Level.INFO, "User " + user.getUserName() + " added " + money + " euros");
        }
    }

    public void getStatus(String filename) throws IOException {
        File file = new File("src/main/resources/" + filename);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        double sumOfTransactions = 0;
        bw.write("\tCurrent cents and bills in inventory:\n");
        bw.write("\t-------------\n");
        bw.write("\t50 euros -> " + moneyInInventory.get("5000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t20 euros -> " + moneyInInventory.get("2000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t10 euros -> " + moneyInInventory.get("1000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t5 euros -> " + moneyInInventory.get("500") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t2 euros -> " + moneyInInventory.get("200") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t1 euro -> " + moneyInInventory.get("100") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t50 cents -> " + moneyInInventory.get("50") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t20 cents -> " + moneyInInventory.get("20") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t10 cents -> " + moneyInInventory.get("10") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t5 cents -> " + moneyInInventory.get("5") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t1 cents -> " + moneyInInventory.get("1") + "\n");
        bw.write("\t-------------------------------------------------------\n");
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet()) {
            bw.write("\t" + entry.getKey().toString() + " " + entry.getValue() + " products in " + entry.getKey().getCode() + "\n");
            bw.write("\t-------------------------------------------------------\n");
        }
        bw.write("\n\tTransactions since last admin operation: \n\n");
        for (Map.Entry<Product, Integer> entry : transactions.entrySet()) {
            int counter = entry.getValue();
            while (counter > 0) {
                sumOfTransactions += entry.getKey().getPrice();
                bw.write("\tItem " + entry.getKey().getName() + " in row " + entry.getKey().getCode() + " has been bought for " + entry.getKey().getPrice() + " euros \n");
                bw.write("\t--------------------------------------------------------------\n");
                counter--;
            }
        }
        bw.write("\tAll these transactions add up to " + String.format("%.2f", sumOfTransactions) + " euros");
        bw.close();
        logger.log(Level.INFO, "Output machine status in txt file\n");
    }

    private boolean containsProductInCode(String code) {
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet()) {
            if (Objects.equals(entry.getKey().getCode(), code))
                return true;
        }
        return false;
    }

    public boolean buyProduct(String code, boolean last) throws ProductNotFound, NotEnoughMoney {
        int money = moneyAddedByUser.get("Current money");
        if (code.charAt(0) < 'A' || code.charAt(0) > 'G' || Integer.parseInt(code.substring(1)) < 1 || Integer.parseInt(code.substring(1)) > 8) {
            logger.log(Level.SEVERE, "User put an invalid code!\n");
            throw new ProductNotFound("Invalid code!");
        }
        if (!containsProductInCode(code)) {
            logger.log(Level.SEVERE, "The user tried to grab a product from an empty row!\n");
            throw new ProductNotFound();
        }
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet()) {
            if (Objects.equals(entry.getKey().getCode(), code)) {
                if (money >= (int) (entry.getKey().getPrice() * 100)) {
                    entry.setValue(entry.getValue() - 1);
                    if (!last) {
                        moneyAddedByUser.put("Current money", (money - (int) (entry.getKey().getPrice() * 100)));
                    } else {
                        moneyAddedByUser.put("Current money", 0);
                        addMoneyToInventory();
                        giveChange((money - (int) (entry.getKey().getPrice() * 100)));
                    }
                    if (!transactions.containsKey(entry.getKey())) {
                        transactions.put(entry.getKey(), 1);
                    } else {
                        transactions.put(entry.getKey(), transactions.get(entry.getKey()) + 1);
                    }
                    logger.log(Level.INFO, "Item " + entry.getKey().getCode() + " has been bought by user " + user.getUserName() + "\n");
                    user.addTransaction(entry.getKey());
                    if (last)
                        user = null;
                    if (entry.getValue() == 0)
                        productsInInventory.remove(entry.getKey());
                    return true;
                } else {
                    logger.log(Level.WARNING, "The user hasn't inserted enough money!\n");
                    throw new NotEnoughMoney();
                }
            }
        }
        return false;
    }

    private void addMoneyToInventory() {
        for (Map.Entry<String, Integer> entry : moneyInInventory.entrySet()) {
            if (!Objects.equals(entry.getKey(), "Current money")) {
                entry.setValue(moneyAddedByUser.get(entry.getKey()) + entry.getValue());
                moneyAddedByUser.put(entry.getKey(), 0);
            }
        }
    }

    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges, InvalidProductType {
        if (user.isAdmin()) {
            if (!productsInInventory.containsKey(product)) {
                productsInInventory.put(product, 0);
            }
            if (productsInInventory.get(product) == 5) {
                logger.log(Level.SEVERE, "There are too many products in requested row!\n");
                throw new TooManyProducts();
            } else {
                productsInInventory.put(product, productsInInventory.get(product) + 1);
                logger.log(Level.INFO, "The product has been successfully loaded!\n");
            }
        } else {
            logger.log(Level.SEVERE, "User tried to load a product!\n");
            throw new NoAdminPrivileges("Loading product unsuccessful");
        }
    }

    public void loadMoney(LinkedHashMap<String, Integer> money) throws NoAdminPrivileges, TooMuchMoney {
        if (user.isAdmin()) {
            for (String key : moneyInInventory.keySet()) {
                if (moneyInInventory.get(key) >= 100) {
                    logger.log(Level.SEVERE, "There is too much money in the machine!\n");
                    if (Integer.parseInt(key) >= 200 && canTakeBills)
                        throw new TooMuchMoney("The machine is full of " + key + " euros bills!");
                    else {
                        throw new TooMuchMoney("The machine is full of " + key + " cents coins!");
                    }
                }
            }
            setMoneyInInventory(money);
        } else {
            logger.log(Level.SEVERE, "User tried to load money without admin privileges!\n");
            throw new NoAdminPrivileges("Loading money unsuccessful!");
        }
    }

    public void unloadProduct(Product product) throws ProductNotFound, NoAdminPrivileges {
        if (!user.isAdmin())
            throw new NoAdminPrivileges("Unloading product unsuccessful!");
        else {
            if (productsInInventory.size() == 1 && productsInInventory.get(product) == 1) {
                transactions.clear();
                logger.log(Level.INFO, "All the products have been unloaded; transactions have been reset!\n");
            }
            if (productsInInventory.get(product) == 1)
                productsInInventory.remove(product);
            else if (!productsInInventory.containsKey(product)) {
                logger.log(Level.SEVERE, "The product to unload cannot be found!\n");
                throw new ProductNotFound();
            } else productsInInventory.put(product, productsInInventory.get(product) - 1);
            logger.log(Level.INFO, "The product has been unloaded!\n");
        }
    }

    public void unloadMoney() throws NoAdminPrivileges {
        if (user.isAdmin()) {
            for (String key : moneyInInventory.keySet()) {
                user.addCoinsToWallet(Integer.parseInt(key), moneyInInventory.get(key));
            }
            setMoneyInInventory(zeroMoney);
            logger.log(Level.INFO, "Successfully unloaded money!Transactions have been reset!\n");
            transactions.clear();
        } else {
            logger.log(Level.SEVERE, "User tried to unload money without admin privileges!\n");
            throw new NoAdminPrivileges("Unloading money unsuccessful");
        }
    }

    public boolean takeProfits() throws NoAdminPrivileges, NotEnoughMoney {
        if (user.isAdmin()) {
            int profits = 0;
            boolean valid = false;
            for (String key : moneyInInventory.keySet()) {
                if (moneyInInventory.get(key) > 10) {
                    profits += Integer.parseInt(key) * (moneyInInventory.get(key) - 10);
                    user.addCoinsToWallet(Integer.parseInt(key), moneyInInventory.get(key) - 10);
                    moneyInInventory.put(key, 10);
                    valid = true;
                } else if (moneyInInventory.get(key) > 0) {
                    profits -= (10 - moneyInInventory.get(key)) * Integer.parseInt(key);
                }
            }
            if (valid) {
                logger.log(Level.INFO, "The profits have been added to admins wallet. With a total profit of: " + (double) profits / 100 + " euros\n");
                return true;
            } else {
                logger.log(Level.SEVERE, "There are no profits to be taken!\n");
                throw new NotEnoughMoney("There are no profits!");
            }
        } else {
            logger.log(Level.SEVERE, "User tried to take profits from machine!\n");
            throw new NoAdminPrivileges("Only admins can take profit from machine!");
        }
    }

    public void finaliseTransaction() throws NotEnoughMoney {
        addMoneyToInventory();
        giveChange(moneyAddedByUser.get("Current money"));
        for (Map.Entry<String, Integer> entry : moneyAddedByUser.entrySet()) {
            entry.setValue(0);
        }
        user = null;
        logger.log(Level.INFO, "The transaction has been canceled!\n");
    }


    public void login(User user) {
        if (this.user != null) {
            logger.log(Level.INFO, "Next user tried to login while current user is still logged in!\n");
        } else {
            this.user = user;
            logger.log(Level.INFO, "Login successful!\n");
        }
    }

    public void login(Admin admin) throws InvalidCredentials {
        if (Objects.equals(this.admin.getPassword(), admin.getPassword()) && Objects.equals(admin.getUserName(), this.admin.getUserName())) {
            this.user = admin;
            logger.log(Level.INFO, "The admin has started using the machine!\n");
        } else {
            logger.log(Level.SEVERE, "Invalid credentials were input for admin login!\n");
            throw new InvalidCredentials("Logging in as admin failed!");
        }
    }

    public void logOut() throws NoAdminPrivileges {
        if (user.isAdmin()) {
            user = null;
            logger.log(Level.INFO, "The admin has left the machine!\n");
        } else {
            logger.log(Level.SEVERE, "User tried to log out manually!\n");
            throw new NoAdminPrivileges("Users should not be able to manually log out!");
        }
    }


    public void setMoneyInInventory(LinkedHashMap<String, Integer> moneyInInventory) {
        this.moneyInInventory.putAll(moneyInInventory);
    }

    public void setMoneyAddedByUser(LinkedHashMap<String, Integer> moneyAddedByUser) throws TooMuchMoney, InvalidCurrency, NotEnoughMoney {
        for (Map.Entry<String, Integer> key : moneyAddedByUser.entrySet())
            while (key.getValue() > 0) {
                insertMoney(Integer.parseInt(key.getKey()));
                key.setValue(key.getValue() - 1);
            }
    }
}
