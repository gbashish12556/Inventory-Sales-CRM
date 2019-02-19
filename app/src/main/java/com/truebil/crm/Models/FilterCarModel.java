package com.truebil.crm.Models;

public class FilterCarModel {

    String name;
    int id;

    public FilterCarModel(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName (){
        return name;
    }

    public int getId() {
        return id;
    }

}
