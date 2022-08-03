package com.Domain;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class User{
    private String username;
    private HashMap<String,Integer> transactions;
    LinkedHashMap<String,Integer> userWallet;
    // de gandit cum se baga in baza de date
    public User(String username) {
        this.username = username;
        this.transactions = new HashMap<String,Integer>();
        this.userWallet = new LinkedHashMap<>();
        this.userWallet.put("5000",0);
        this.userWallet.put("2000",0);
        this.userWallet.put("1000",0);
        this.userWallet.put("500",0);
        this.userWallet.put("200",0);
        this.userWallet.put("100",0);
        this.userWallet.put("50",0);
        this.userWallet.put("20",0);
        this.userWallet.put("10",0);
        this.userWallet.put("5",0);
        this.userWallet.put("1",0);
    }

    public void addTransaction(String productName, int amountBought){
        if(!transactions.containsKey(productName))
            this.transactions.put(productName,0);
        this.transactions.put(productName, transactions.get(productName)+1);
    }


    public void setUserWallet(LinkedHashMap<String, Integer> userWallet) {
        this.userWallet = userWallet;
    }
    public boolean isAdmin(){
        return false;
    }

    public LinkedHashMap<String, Integer> getUserWallet() {
        return userWallet;
    }

    public HashMap<String, Integer> getTransactions() {
        return transactions;
    }
}
