package ua.lviv.stapixbusiness.models;

import java.util.UUID;

public class Business {

    private String id;
    private String name;
    private double buyPrice;
    private double hourlyWage;
    private double salesPercent;
    private double dailyTax;
    private double maxTaxDebt;
    private double gangPercent;
    private double commissionPercent;

    private UUID ownerUUID;
    private String ownerName;
    private double businessBalance;
    private double taxDebt;
    private long taxDebtReachedMaxTime; // коли борг досяг максимуму

    private String gangOwner; // назва банди яка тримає дах
    private long lastStreetFightTime; // час останньої стрілки
    private long streetFightScheduledTime; // запланована стрілка

    private double npcX, npcY, npcZ;
    private String npcWorld;
    private int npcId;

    private double holoX, holoY, holoZ;
    private String holoWorld;

    public Business(String id, String name, double buyPrice) {
        this.id = id;
        this.name = name;
        this.buyPrice = buyPrice;
        this.hourlyWage = 100;
        this.salesPercent = 10;
        this.dailyTax = 500;
        this.maxTaxDebt = 10000;
        this.gangPercent = 20;
        this.commissionPercent = 5;
        this.businessBalance = 0;
        this.taxDebt = 0;
        this.taxDebtReachedMaxTime = 0;
        this.ownerUUID = null;
        this.ownerName = null;
        this.gangOwner = null;
        this.lastStreetFightTime = 0;
        this.streetFightScheduledTime = 0;
    }

    public boolean hasOwner() { return ownerUUID != null; }
    public boolean hasGang() { return gangOwner != null && !gangOwner.isEmpty(); }

    public boolean canBuyStreetFight() {
        if (lastStreetFightTime == 0) return true;
        long weekInMs = 7L * 24 * 60 * 60 * 1000;
        return System.currentTimeMillis() - lastStreetFightTime >= weekInMs;
    }

    public boolean canRetryStreetFight() {
        if (lastStreetFightTime == 0) return true;
        long dayInMs = 24L * 60 * 60 * 1000;
        return System.currentTimeMillis() - lastStreetFightTime >= dayInMs;
    }

    // Getters and Setters
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public double getHourlyWage() { return hourlyWage; }
    public void setHourlyWage(double hourlyWage) { this.hourlyWage = hourlyWage; }
    public double getSalesPercent() { return salesPercent; }
    public void setSalesPercent(double salesPercent) { this.salesPercent = salesPercent; }
    public double getDailyTax() { return dailyTax; }
    public void setDailyTax(double dailyTax) { this.dailyTax = dailyTax; }
    public double getMaxTaxDebt() { return maxTaxDebt; }
    public void setMaxTaxDebt(double maxTaxDebt) { this.maxTaxDebt = maxTaxDebt; }
    public double getGangPercent() { return gangPercent; }
    public void setGangPercent(double gangPercent) { this.gangPercent = gangPercent; }
    public double getCommissionPercent() { return commissionPercent; }
    public void setCommissionPercent(double commissionPercent) { this.commissionPercent = commissionPercent; }
    public UUID getOwnerUUID() { return ownerUUID; }
    public void setOwnerUUID(UUID ownerUUID) { this.ownerUUID = ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public double getBusinessBalance() { return businessBalance; }
    public void setBusinessBalance(double businessBalance) { this.businessBalance = businessBalance; }
    public void addToBalance(double amount) { this.businessBalance += amount; }
    public double getTaxDebt() { return taxDebt; }
    public void setTaxDebt(double taxDebt) { this.taxDebt = taxDebt; }
    public void addTaxDebt(double amount) { this.taxDebt += amount; }
    public long getTaxDebtReachedMaxTime() { return taxDebtReachedMaxTime; }
    public void setTaxDebtReachedMaxTime(long time) { this.taxDebtReachedMaxTime = time; }
    public String getGangOwner() { return gangOwner; }
    public void setGangOwner(String gangOwner) { this.gangOwner = gangOwner; }
    public long getLastStreetFightTime() { return lastStreetFightTime; }
    public void setLastStreetFightTime(long time) { this.lastStreetFightTime = time; }
    public long getStreetFightScheduledTime() { return streetFightScheduledTime; }
    public void setStreetFightScheduledTime(long time) { this.streetFightScheduledTime = time; }
    public double getNpcX() { return npcX; }
    public void setNpcX(double npcX) { this.npcX = npcX; }
    public double getNpcY() { return npcY; }
    public void setNpcY(double npcY) { this.npcY = npcY; }
    public double getNpcZ() { return npcZ; }
    public void setNpcZ(double npcZ) { this.npcZ = npcZ; }
    public String getNpcWorld() { return npcWorld; }
    public void setNpcWorld(String npcWorld) { this.npcWorld = npcWorld; }
    public int getNpcId() { return npcId; }
    public void setNpcId(int npcId) { this.npcId = npcId; }
    public double getHoloX() { return holoX; }
    public void setHoloX(double holoX) { this.holoX = holoX; }
    public double getHoloY() { return holoY; }
    public void setHoloY(double holoY) { this.holoY = holoY; }
    public double getHoloZ() { return holoZ; }
    public void setHoloZ(double holoZ) { this.holoZ = holoZ; }
    public String getHoloWorld() { return holoWorld; }
    public void setHoloWorld(String holoWorld) { this.holoWorld = holoWorld; }
}
