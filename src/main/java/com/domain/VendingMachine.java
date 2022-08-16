package com.domain;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.exceptions.*;
import com.utils.Utils;

import static com.utils.Utils.zeroCents;

public class VendingMachine {
    protected User user;
    protected boolean canTakeBills;
    protected final Admin admin;
    protected LinkedHashMap<String, Integer> centsInInventory;
    protected LinkedHashMap<Product, Integer> productsInInventory;
    protected LinkedHashMap<String, Integer> centsAddedByUser;
    protected LinkedHashMap<String, Integer> change;

    protected LinkedHashMap<Product, Integer> transactions;
    protected final static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public VendingMachine(Admin admin, boolean canTakeBills) {
        this.user = null;
        this.admin = admin;
        this.canTakeBills = canTakeBills;
        this.transactions = new LinkedHashMap<>();
        this.centsInInventory = new LinkedHashMap<>();
        this.productsInInventory = new LinkedHashMap<>();
        this.centsAddedByUser = new LinkedHashMap<>();
        this.change = new LinkedHashMap<>();
        centsInInventory.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        centsAddedByUser.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        change.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        centsAddedByUser.put("Current money", 0);
    }

    public LinkedHashMap<Product, Integer> getProductsInInventory() {
        return productsInInventory;
    }

    public LinkedHashMap<String, Integer> getCentsInInventory() {
        return centsInInventory;
    }

    public boolean giveChange(int cents) throws NotEnoughMoney {
        if (cents == 0) {
            logger.log(Level.INFO, "No change needed!\n");
            return true;
        } else {
            if (checkIfChangePossible(cents)) {
                for (Map.Entry<String, Integer> entry : change.entrySet()) {
                    user.addCoinsToWallet(Integer.parseInt(entry.getKey()), entry.getValue());
                    while (entry.getValue() > 0) {
                        entry.setValue(entry.getValue() - 1);
                        if (Integer.parseInt(entry.getKey()) < 100) {
                            logger.log(Level.INFO, "A coin of " + entry.getKey() + " euro-cents has been returned!\n");
                        } else if (Integer.parseInt(entry.getKey()) <= 200) {
                            logger.log(Level.INFO, "A coin of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!\n");
                        } else if (canTakeBills) {
                            logger.log(Level.INFO, "A bill of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!\n");
                        }
                    }
                }
                logger.log(Level.INFO, "Change was given\n");
                return true;
            } else {
                for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
                    int value = change.get(entry.getKey());
                    entry.setValue(value + entry.getValue());
                    change.put(entry.getKey(), 0);
                }
                logger.log(Level.SEVERE, "Error", new NotEnoughMoney("Not enough money in inventory for giving change"));
                throw new NotEnoughMoney("Not enough money in inventory for giving change!");
            }
        }
    }


    private boolean checkIfChangePossible(int cents) {
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            if (!(Integer.parseInt(entry.getKey()) > 500 && !canTakeBills)) {
                int maxCoin = Integer.parseInt(entry.getKey());
                while (cents >= maxCoin && entry.getValue() > 0) {
                    change.put(String.valueOf(maxCoin), change.get(String.valueOf(maxCoin)) + 1);
                    cents -= maxCoin;
                    entry.setValue(entry.getValue() - 1);
                }
            }
        }
        return cents == 0;
    }

    private void checkIfMoneyValid(int cents) throws InvalidCurrency, TooMuchMoney, NotEnoughMoney {
        try {
            if (centsInInventory.get(String.valueOf(cents)) >= 100) {
                cancelTransaction();
                if (cents > 200 && canTakeBills) {
                    logger.log(Level.SEVERE, "Error: ", new TooMuchMoney("The machine has too many bills of " + cents / 100));
                    throw new TooMuchMoney("The machine has too many bills of " + cents / 100);
                }
                logger.log(Level.SEVERE, "Error: ", new TooMuchMoney("The machine has too many coins of " + cents + " cents"));
                throw new TooMuchMoney("The machine has too many coins of " + cents + " cents");
            }
        } catch (NullPointerException e) {
            logger.log(Level.SEVERE, "Error: ", new InvalidCurrency());
            throw new InvalidCurrency();
        }
    }

    public void insertMoney(int cents) throws InvalidCurrency, TooMuchMoney, NotEnoughMoney {
        try {
            checkIfMoneyValid(cents);
        }
        catch (InvalidCurrency e){
            throw new InvalidCurrency();
        }
        catch (TooMuchMoney e)
        {
            throw new TooMuchMoney("The machine has too many coins of " + cents + " cents");
        }
        if (cents == 1 || cents == 5 || cents == 10 || cents == 20 || cents == 50 || cents == 100 || cents == 200) {
            if (user.getUserWallet().get(String.valueOf(cents)) <= 0) {
                cancelTransaction();
                throw new NotEnoughMoney("The user doesn't have a coin of " + cents + " to insert!");
            }
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            centsAddedByUser.put("Current money", centsAddedByUser.get("Current money") + cents);
            user.removeCoinsFromWallet(cents, 1);
            logger.log(Level.INFO, "User " + user.getUserName() + " added " + cents + " euro-cents");
        } else if (canTakeBills && ((cents == 500) || (cents) == 1000 || (cents == 2000) || (cents == 5000))) {
            if (user.getUserWallet().get(String.valueOf(cents)) <= 0) {
                cancelTransaction();
                throw new NotEnoughMoney("The user doesn't have a bill of " + cents + " to insert!");
            }
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            centsAddedByUser.put("Current money", centsAddedByUser.get("Current money") + cents);
            user.removeCoinsFromWallet(cents, 1);
            logger.log(Level.INFO, "User " + user.getUserName() + " added " + cents + " euros");
        }
    }

    public void getStatus(String filename) throws IOException {
        File file = new File("src/main/resources/" + filename);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        double sumOfTransactions = 0;
        bw.write("\tCurrent cents and bills in inventory:\n");
        bw.write("\t-------------\n");
        bw.write("\t50 euros -> " + centsInInventory.get("5000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t20 euros -> " + centsInInventory.get("2000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t10 euros -> " + centsInInventory.get("1000") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t5 euros -> " + centsInInventory.get("500") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t2 euros -> " + centsInInventory.get("200") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t1 euro -> " + centsInInventory.get("100") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t50 cents -> " + centsInInventory.get("50") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t20 cents -> " + centsInInventory.get("20") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t10 cents -> " + centsInInventory.get("10") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t5 cents -> " + centsInInventory.get("5") + "\n");
        bw.write("\t-------------\n");
        bw.write("\t1 cents -> " + centsInInventory.get("1") + "\n");
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
        int cents = centsAddedByUser.get("Current money");
        if (!containsProductInCode(code)) {
            throw new ProductNotFound();
        }
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet()) {
            if (Objects.equals(entry.getKey().getCode(), code)) {
                if (cents >= (int) (entry.getKey().getPrice() * 100)) {
                    entry.setValue(entry.getValue() - 1);
                    if (!last) {
                        centsAddedByUser.put("Current money", (cents - (int) (entry.getKey().getPrice() * 100)));
                    } else {
                        centsAddedByUser.put("Current money", 0);
                        addCentsToInventory();
                        giveChange((cents - (int) (entry.getKey().getPrice() * 100)));
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

    private void addCentsToInventory() {
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            if (!Objects.equals(entry.getKey(), "Current money")) {
                entry.setValue(centsAddedByUser.get(entry.getKey()) + entry.getValue());
                centsAddedByUser.put(entry.getKey(), 0);
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

    public void loadMoney(LinkedHashMap<String, Integer> cents) throws NoAdminPrivileges, TooMuchMoney {
        if (user.isAdmin()) {
            for (String key : centsInInventory.keySet()) {
                if (centsInInventory.get(key) >= 100) {
                    logger.log(Level.SEVERE, "There is too much money in the machine!\n");
                    if (Integer.parseInt(key) >= 200 && canTakeBills)
                        throw new TooMuchMoney("The machine is full of " + key + " euros bills!");
                    else {
                        throw new TooMuchMoney("The machine is full of " + key + " cents coins!");
                    }
                }
            }
            setCentsInInventory(cents);
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
            for (String key : centsInInventory.keySet()) {
                user.addCoinsToWallet(Integer.parseInt(key), centsInInventory.get(key));
            }
            setCentsInInventory(zeroCents);
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
            for (String key : centsInInventory.keySet()) {
                if (centsInInventory.get(key) > 10) {
                    profits += Integer.parseInt(key) * (centsInInventory.get(key) - 10);
                    user.addCoinsToWallet(Integer.parseInt(key), centsInInventory.get(key) - 10);
                    centsInInventory.put(key, 10);
                    valid = true;
                } else if (centsInInventory.get(key) > 0) {
                    profits -= (10 - centsInInventory.get(key)) * Integer.parseInt(key);
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

    public void cancelTransaction() throws NotEnoughMoney {
        addCentsToInventory();
        giveChange(centsAddedByUser.get("Current money"));
        for (Map.Entry<String, Integer> entry : centsAddedByUser.entrySet()) {
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


    public void setCentsInInventory(LinkedHashMap<String, Integer> centsInInventory) {
        this.centsInInventory.putAll(centsInInventory);
    }

    public void setCentsAddedByUser(LinkedHashMap<String, Integer> centsAddedByUser) throws TooMuchMoney, InvalidCurrency, NotEnoughMoney {
        for (Map.Entry<String, Integer> key : centsAddedByUser.entrySet())
            while (key.getValue() > 0) {
                insertMoney(Integer.parseInt(key.getKey()));
                key.setValue(key.getValue() - 1);
            }
    }
}
