package com.domain;

import com.utils.Utils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
public class User{
    private final String username;
    private final HashMap<String, LocalDateTime> transactions;
    private LinkedHashMap<String,Integer> userWallet;
    public User(String username) {
        this.username = username;
        this.transactions = new HashMap<>();
        this.userWallet = new LinkedHashMap<>();
        this.userWallet = Utils.formatHashMap(0,0,0,0,0,0,0,0,0,0,0);
    }

    public void addTransaction(Product product){
        transactions.put(product.getName(),LocalDateTime.now());
    }

    public void addCoinsToWallet(int cents,int amount){
        this.userWallet.put(String.valueOf(cents),userWallet.get(String.valueOf(cents))+amount);
    }
    public void removeCoinsFromWallet(int cents,int amount){
        this.userWallet.put(String.valueOf(cents),userWallet.get(String.valueOf(cents))-amount);
    }
    public void setUserWallet(LinkedHashMap<String, Integer> userWallet) {
        this.userWallet.putAll(userWallet);
    }
    public boolean isAdmin(){
        return false;
    }

    public LinkedHashMap<String, Integer> getUserWallet() {
        return userWallet;
    }

    public HashMap<String, LocalDateTime> getTransactions() {
        return transactions;
    }

    public String getUserName() {
        return username;
    }
}
