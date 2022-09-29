package me.raducapatina.server.data;

import java.util.ArrayList;
import java.util.List;

public class Account {

    protected String username;
    protected String password;

    protected String ownerName;
    protected String id;
    protected int balance = 0;
    protected List<MoneyTransfer> transfers = new ArrayList<>(0);
    protected MerchantType merchantType = MerchantType.UNKNOWN;
    protected boolean sysAdmin = false;

    public Account() {}

    Account(String username, String password, String ownerName, String id, MerchantType merchantType, boolean sysAdmin) {
        this.username = username;
        this.password = password;
        this.ownerName = ownerName;
        this.id = id;
        this.merchantType = merchantType;
        this.sysAdmin = sysAdmin;
    }

    public enum MerchantType {
        INDIVIDUAL,
        TOURISM,
        GROCERIES,
        TRANSPORT,

        UNKNOWN
    }

    public Account setOwnerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public Account setMerchantType(MerchantType merchantType) {
        this.merchantType = merchantType;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public List<MoneyTransfer> getTransfers() {
        return transfers;

    }

    public MerchantType getMerchantType() {
        return merchantType;
    }

    public boolean isSysAdmin() {
        return sysAdmin;
    }
}