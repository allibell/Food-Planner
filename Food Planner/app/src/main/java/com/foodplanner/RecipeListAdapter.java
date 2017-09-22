package com.foodplanner;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sol on 1/5/2017.
 */

public class RecipeListAdapter extends RecyclerView.Adapter<RecipeListAdapter.ViewHolder> implements ItemTouchHelperAdapter {
    private List<Recipe> mDataset;

    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) itemView.findViewById(R.id.recipe_item);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public RecipeListAdapter(List<Recipe> recipes) {
        mDataset = recipes;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecipeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recipe_list_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mTextView.setText(mDataset.get(position).toString());

    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // On swipe
    @Override
    public void onItemDismiss(int position) {
        // Access swiped recipe in database
        Recipe temp = Recipe.load(Recipe.class, mDataset.get(position).getId());

        // Delete any instances of recipe in Meal Plan
        new Delete().from(Meal.class).where("Recipe = ?", temp.getId()).execute();

        // Delete from database
        temp.delete();

        // Delete from list
        mDataset.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemSwipeEnd(int position) {
        Meal.mealid++;
        // Add meal to database (meals table)
        Meal meal = new Meal();
        meal.remoteId = Meal.mealid;
        meal.recipe = mDataset.get(position);
        meal.save();
        // Add meal ingredients to shopping list items table
        // Convert from Json to List
        String jsonIngredients = meal.recipe.getIngredients();
        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> listJsonIngredients = new Gson().fromJson(jsonIngredients, listType);
        List<Ingredient> listIngredients = new ArrayList<>();
        for(int i=0; i < listJsonIngredients.size(); i++){
            listIngredients.add(i, gson.fromJson(listJsonIngredients.get(i), Ingredient.class));
        }

        for(Ingredient ingredient : listIngredients){
            ShoppingListItem item = new ShoppingListItem();
            item.remoteId = ShoppingListItem.itemid;
            item.mealId = meal.remoteId;
            item.name = ingredient.name;
            item.amount = ingredient.amount;
            item.measurementType = ingredient.measurementType;
            item.save();
            ShoppingListItem.itemid++;
        }
    }

    // Drag and drop
    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mDataset, i, i+1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mDataset, i, i-1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }
}
