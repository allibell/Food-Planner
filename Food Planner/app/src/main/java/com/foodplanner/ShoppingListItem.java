package com.foodplanner;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

@Table(name = "ShoppingListItems")
public class ShoppingListItem extends Model {

    public static long itemid = 0;

    // Unique id given by server
    @Column(name = "remote_id", unique = true)
    public long remoteId;

    //Regular fields
    @Column(name = "MealId")
    long mealId;
    @Column(name = "Name")
    String name;
    @Column(name = "Amount")
    int amount;
    @Column(name = "MeasurementType")
    String measurementType;

    public ShoppingListItem() {
        super();
    }

    public ShoppingListItem(long remoteId, int mealId, String name, int amount, String measurementType){
        super();
        this.remoteId = remoteId;
        this.mealId = mealId;
        this.name = name;
        this.amount = amount;
        this.measurementType = measurementType;
    }

    public static List<ShoppingListItem> getAllForMeal(int mealId){
        return new Select()
                .from(Ingredient.class)
                .where("MealId = ?", mealId)
                .orderBy("Name ASC")
                .execute();
    }

    // Method to get all Meals
    public static List<ShoppingListItem> getAll() {
        return new Select()
                .from(ShoppingListItem.class)
                .execute();
    }

    @Override
    public String toString() {
        return name + ", " + amount + " " + measurementType;
    }
}
