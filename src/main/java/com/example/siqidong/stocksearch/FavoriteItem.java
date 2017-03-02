package com.example.siqidong.stocksearch;

public class FavoriteItem {

    private String symbol;
    private String name;
    private String price;
    private Double change;
    private String marketcap;

    public FavoriteItem(String symbol, String name, String price, Double change,String marketcap) {
        super();
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.marketcap = marketcap;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public String getPrice() { return price; }

    public Double getChange() {
        return change;
    }

    public String getMarketcap() {
        return marketcap;
    }

}
