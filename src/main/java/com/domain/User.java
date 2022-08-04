package com.domain;

import com.utils.Utils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class User{
    private String username;
    private HashMap<String,Integer> transactions;
    private LinkedHashMap<String,Integer> userWallet;
    public User(String username) {
        this.username = username;
        this.transactions = new HashMap<String,Integer>();
        this.userWallet = new LinkedHashMap<>();
        this.userWallet = Utils.formatHashMap(0,0,0,0,0,0,0,0,0,0,0);
    }

    public void addTransaction(String productName){
        if(!transactions.containsKey(productName))
            this.transactions.put(productName,0);
        this.transactions.put(productName, transactions.get(productName)+1);
    }

    public void addCoinsToWallet(int cents,int amount){
        this.userWallet.put(String.valueOf(cents),userWallet.get(String.valueOf(cents))+amount);
    }
    public void removeCoinsFromWallet(int cents,int amount){
        this.userWallet.put(String.valueOf(cents),userWallet.get(String.valueOf(cents))-amount);
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
