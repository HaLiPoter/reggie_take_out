package com.itheima.reggie.entity;

import lombok.Data;

@Data
public class Car {
    private String brand;
    private String model;
    private String color;
    private int year;
    private double price;
    private double mileage;
}
