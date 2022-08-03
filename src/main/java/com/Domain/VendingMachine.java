package com.Domain;

import java.io.*;
import java.util.*;
import com.utils.Utils;

public class VendingMachine {
    private User user;
    boolean canTakeBills;

    protected LinkedHashMap<String, Integer> centsInInventory;
    protected LinkedHashMap<Product, Integer> productsInInventory;
    protected LinkedHashMap<String, Integer> centsAddedByUser;
    protected LinkedHashMap<String, Integer> change;
    protected LinkedHashMap<String, Integer> centsUsedToBuyProduct;

    public VendingMachine(User user, boolean canTakeBills) {
        this.user = user;
        this.canTakeBills = canTakeBills;
        this.centsInInventory = new LinkedHashMap<>();
        this.productsInInventory = new LinkedHashMap<>();
        this.centsAddedByUser = new LinkedHashMap<>();
        this.change = new LinkedHashMap<>();
        this.centsUsedToBuyProduct = new LinkedHashMap<>();
        centsInInventory = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        centsAddedByUser = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        change = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    public LinkedHashMap<Product, Integer> getProductsInInventory() {
        return productsInInventory;
    }

    public LinkedHashMap<String, Integer> getCentsInInventory() {
        return centsInInventory;
    }

    public boolean giveChange(int cents) {
        if (checkIfChangePossible(cents)) {
            for (Map.Entry<String, Integer> entry : change.entrySet()) {
                user.userWallet.put(entry.getKey(), user.userWallet.get(entry.getKey()) + entry.getValue());
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
            return false;

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

    public boolean insertMoney(int cents) {
        if (cents == 1 || cents == 5 || cents == 10 || cents == 20 || cents == 50 || cents == 100 || cents == 200) {
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            user.userWallet.put(String.valueOf(cents), user.userWallet.get(String.valueOf(cents)) - 1);
            return true;
        } else if (canTakeBills && ((cents == 500) || (cents) == 1000 || (cents == 2000) || (cents == 5000))) {
            centsAddedByUser.put(String.valueOf(cents), centsAddedByUser.get(String.valueOf(cents)) + 1);
            user.userWallet.put(String.valueOf(cents), user.userWallet.get(String.valueOf(cents)) - 1);
            return true;
        }
        return false;
    }

    public void buyMoreProducts(Product product) {
        int price = (int) (product.getPrice() * 100);
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet())
            while (Integer.parseInt(entry.getKey()) <= price && centsAddedByUser.get(entry.getKey()) > 0 && price > 0) {
                centsAddedByUser.put(entry.getKey(), centsAddedByUser.get(entry.getKey()) - 1);
                entry.setValue(entry.getValue() + 1);
                price -= Integer.parseInt(entry.getKey());
            }

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

    public boolean buyProduct(String code, boolean last) {
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
                        addCentsToInventory();
                        giveChange((int) (cents - (entry.getKey().getPrice() * 100)));
                    }
                    System.out.println("Item " + entry.getKey().getCode() + " has been bought");
                    user.addTransaction(entry.getKey().getName(), 1);
                    return true;
                } else {
                    System.out.println("Not enough money has been inserted!");
                    System.out.println("=====================");
                    System.out.println("=====================");
                    return false;
                }
            }
        }
        System.out.println("The product doesn't exist!");
        System.out.println("=====================");
        return false;
    }

    private void addCentsToInventory() {
        for (Map.Entry<String, Integer> entry : centsInInventory.entrySet()) {
            entry.setValue(centsAddedByUser.get(entry.getKey()) + entry.getValue());
            centsAddedByUser.put(entry.getKey(), 0);
        }
    }

    public void loadProduct(Product product) {
        if (user.isAdmin()) {
            if (!productsInInventory.containsKey(product)) {
                productsInInventory.put(product, 0);
            }
            if (productsInInventory.get(product) == 10) {
                System.out.println("There are too many products on the rack; impossible operation");
            } else {
                productsInInventory.put(product, productsInInventory.get(product) + 1);
            }
        } else System.out.println("Only admins can load products!");
    }

    public void loadMoney(LinkedHashMap<String, Integer> cents) {
        if (user.isAdmin())
            setCentsInInventory(cents);
        else System.out.println("Only users with admin privileges can load money!");
    }

    public void unloadProduct(Product product) {
        if (!user.isAdmin())
            System.out.println("Only users with admin privileges can unload products!");
        else {
            if (productsInInventory.get(product) == 1)
                productsInInventory.remove(product);
            else if (!productsInInventory.containsKey(product))
                System.out.println("There is no such product to unload; impossible operation");
            else productsInInventory.put(product, productsInInventory.get(product) - 1);
        }
    }

    public void unloadMoney() {
        if (user.isAdmin()) {
            LinkedHashMap<String, Integer> zeroCents;
            zeroCents = Utils.formatHashMap(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            setCentsInInventory(zeroCents);
        } else System.out.println("Only users with admin privileges can unload money!");
    }

    public void cancelTransaction() {
        for (Map.Entry<String, Integer> entry : centsAddedByUser.entrySet()) {
            user.userWallet.put(entry.getKey(), entry.getValue());
            entry.setValue(0);
        }
    }


    public void login(User user) {
        this.user = user;
    }


    public void setCentsInInventory(LinkedHashMap<String, Integer> centsInInventory) {
        this.centsInInventory = centsInInventory;
    }

    public void setCentsAddedByUser(LinkedHashMap<String, Integer> centsAddedByUser) {
        this.centsAddedByUser = centsAddedByUser;
    }
}
