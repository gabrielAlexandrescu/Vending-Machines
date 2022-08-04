package com.domain;

import java.io.*;
import java.util.*;

import com.exceptions.*;
import com.utils.Utils;

public class VendingMachine {
    private User user;
    boolean canTakeBills;

    protected LinkedHashMap<String, Integer> centsInInventory;
    protected LinkedHashMap<Product, Integer> productsInInventory;
    protected LinkedHashMap<String, Integer> centsAddedByUser;
    protected LinkedHashMap<String, Integer> change;

    public VendingMachine(User user, boolean canTakeBills) {
        this.user = user;
        this.canTakeBills = canTakeBills;
        this.centsInInventory = new LinkedHashMap<>();
        this.productsInInventory = new LinkedHashMap<>();
        this.centsAddedByUser = new LinkedHashMap<>();
        this.change = new LinkedHashMap<>();
        centsInInventory.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        centsAddedByUser.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
        change.putAll(Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    }

    public LinkedHashMap<Product, Integer> getProductsInInventory() {
        return productsInInventory;
    }

    public LinkedHashMap<String, Integer> getCentsInInventory() {
        return centsInInventory;
    }

    public boolean giveChange(int cents) throws NotEnoughMoney {
        if (checkIfChangePossible(cents)) {
            for (Map.Entry<String, Integer> entry : change.entrySet()) {
                user.addCoinsToWallet(Integer.parseInt(entry.getKey()), entry.getValue());
                while (entry.getValue() > 0) {
                    entry.setValue(entry.getValue() - 1);
                    if (Integer.parseInt(entry.getKey()) < 100) {
                        System.out.println("A coin of " + entry.getKey() + " eurocents has been returned!");
                    } else if (Integer.parseInt(entry.getKey()) <= 200) {
                        System.out.println("A coin of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!");
                    } else if (canTakeBills) {
                        System.out.println("A bill of " + Integer.parseInt(entry.getKey()) / 100 + " euros has been returned!");
                    }
                }
            }
            System.out.println("=======================");
            return true;
        } else {
            for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
                int value = change.get(entry.getKey());
                entry.setValue(value + entry.getValue());
                change.put(entry.getKey(), 0);
            }
            throw new NotEnoughMoney("Not enough money in inventory for giving change!");
        }
    }

    private boolean checkIfChangePossible(int cents) {
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            if (Integer.parseInt(entry.getKey()) > 500 && !canTakeBills) {
            } else {
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

    public void insertMoney(int cents) throws InvalidCurrency {
        if (cents == 1 || cents == 5 || cents == 10 || cents == 20 || cents == 50 || cents == 100 || cents == 200) {
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            user.removeCoinsFromWallet(cents, 1);
        } else if (canTakeBills && ((cents == 500) || (cents) == 1000 || (cents == 2000) || (cents == 5000))) {
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            user.removeCoinsFromWallet(cents, 1);
        }
        else throw new InvalidCurrency();
    }

    public void buyMoreProducts(Product product) throws NotEnoughMoney {
        int price = (int) (product.getPrice() * 100);
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            String cents = entry.getKey();
            while (Integer.parseInt(cents) <= price && centsAddedByUser.get(cents) > 0 && price > 0) {
                centsAddedByUser.put(cents, centsAddedByUser.get(cents) - 1);
                entry.setValue(entry.getValue() + 1);
                price -= Integer.parseInt(entry.getKey());
                user.removeCoinsFromWallet(Integer.parseInt(cents),1);
            }
        }
        if (price < 0)
            throw new NotEnoughMoney("Not enough money in inventory when user tried to buy more products!");

    }

    public void getStatus() throws IOException {
        FileWriter fw = new FileWriter("output.txt");
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write("Current cents and bills in inventory:\n");
        bw.write("50 euros: " + centsInInventory.get("5000") + "\n");
        bw.write("20 euros: " + centsInInventory.get("2000") + "\n");
        bw.write("10 euros: " + centsInInventory.get("1000") + "\n");
        bw.write("5 euros: " + centsInInventory.get("500") + "\n");
        bw.write("2 euros: " + centsInInventory.get("200") + "\n");
        bw.write("1 euros: " + centsInInventory.get("100") + "\n");
        bw.write("50 cents: " + centsInInventory.get("50") + "\n");
        bw.write("20 cents: " + centsInInventory.get("20") + "\n");
        bw.write("10 cents: " + centsInInventory.get("10") + "\n");
        bw.write("5 cents: " + centsInInventory.get("5") + "\n");
        bw.write("1 cents: " + centsInInventory.get("1") + "\n\n");
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet())
            bw.write(entry.getKey().toString() + ": " + entry.getValue() + "\n");
        bw.close();
    }

    public boolean buyProduct(String code, boolean last) throws ProductNotFound, NotEnoughMoney {
        int cents = 0;
        for (Map.Entry<String, Integer> entry : centsAddedByUser.entrySet())
            cents += Integer.parseInt(entry.getKey()) * entry.getValue();
        for (Map.Entry<Product, Integer> entry : productsInInventory.entrySet()) {
            if (Objects.equals(entry.getKey().getCode(), code)) {
                if (cents >= (int) (entry.getKey().getPrice() * 100)) {
                    entry.setValue(entry.getValue() - 1);
                    if (!last) {
                        buyMoreProducts(entry.getKey());
                    } else {
                        for(Map.Entry<String, Integer> secondEntry : centsAddedByUser.entrySet())
                            user.removeCoinsFromWallet(Integer.parseInt(secondEntry.getKey()), secondEntry.getValue());
                        addCentsToInventory();
                        giveChange((int) (cents - (entry.getKey().getPrice() * 100)));
                    }
                    System.out.println("Item " + entry.getKey().getCode() + " has been bought");
                    user.addTransaction(entry.getKey().getName());
                    return true;
                } else {
                    throw new NotEnoughMoney();
                }
            }
        }
        throw new ProductNotFound();
    }

    private void addCentsToInventory() {
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            entry.setValue(centsAddedByUser.get(entry.getKey()) + entry.getValue());
            centsAddedByUser.put(entry.getKey(), 0);
        }
    }

    public void loadProduct(Product product) throws TooManyProducts, NoAdminPrivileges {
        if (user.isAdmin()) {
            if (!productsInInventory.containsKey(product)) {
                productsInInventory.put(product, 0);
            }
            if (productsInInventory.get(product) == 10) {
                throw new TooManyProducts();
            } else {
                productsInInventory.put(product, productsInInventory.get(product) + 1);
            }
        } else {
            throw new NoAdminPrivileges("Loading product unsuccesful");
        }
    }

    public void loadMoney(LinkedHashMap<String, Integer> cents) throws NoAdminPrivileges {
        if (user.isAdmin())
            setCentsInInventory(cents);
        else throw new NoAdminPrivileges("Loading money unsuccesful!");
    }

    public void unloadProduct(Product product) throws ProductNotFound, NoAdminPrivileges {
        if (!user.isAdmin())
            throw new NoAdminPrivileges("Unloading product unsuccesful!");
        else {
            if (productsInInventory.get(product) == 1)
                productsInInventory.remove(product);
            else if (!productsInInventory.containsKey(product))
                throw new ProductNotFound();
            else productsInInventory.put(product, productsInInventory.get(product) - 1);
        }
    }

    public void unloadMoney() throws NoAdminPrivileges {
        if (user.isAdmin()) {
            LinkedHashMap<String, Integer> zeroCents;
            zeroCents = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            setCentsInInventory(zeroCents);
        } else throw new NoAdminPrivileges("Unloading money unsuccesful");
    }

    public void cancelTransaction() {
        for (Map.Entry<String, Integer> entry : centsAddedByUser.entrySet()) {
            user.addCoinsToWallet(Integer.parseInt(entry.getKey()), entry.getValue());
            entry.setValue(0);
        }
    }


    public void login(User user) {
        this.user = user;
    }


    public void setCentsInInventory(LinkedHashMap<String, Integer> centsInInventory) {
        this.centsInInventory.putAll(centsInInventory);
    }

    public void setCentsAddedByUser(LinkedHashMap<String, Integer> centsAddedByUser) {
        this.centsAddedByUser.putAll(centsAddedByUser);
    }
}
