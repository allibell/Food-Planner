package com.foodplanner;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.Streams;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.foodplanner.Recipe.recid;

public class AddRecipe extends Fragment {

    private RecyclerView mRecyclerView;
    private RemovableItemListAdapter mAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    Button addRecipeButton;
    Button addIngredientButton;

    EditText name;
    EditText amount;
    Spinner type;
    ListView lv;
    static TextView ingredientsCount;
    static List<Ingredient> ings = new ArrayList<>();
    List<Ingredient> reversed = new ArrayList<>();
    List<String> jsonIngs = new ArrayList<>();

    GsonBuilder builder = new GsonBuilder();
    Gson gson = builder.excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC).create();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_add_recipe, container, false);

        addIngredientButton = (Button) view.findViewById(R.id.add_ingredient);
        addIngredientButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredient();
            }
        });

        addRecipeButton = (Button) view.findViewById(R.id.add_recipe);
        addRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewRecipe();
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        //ShoppingListItems counter
        ingredientsCount = (TextView) v.findViewById(R.id.ingredientsCount);
        ingredientsCount.setText(String.valueOf(ings.size()));

        // Populate "measurementType" drop-down spinner list with measurement types
        Spinner measurement_dropdown = (Spinner) v.findViewById(R.id.measurementType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.measurements_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        measurement_dropdown.setAdapter(adapter);

        // Set up for RecyclerView list
        mRecyclerView = (RecyclerView) v.findViewById(R.id.ingredients_list);
        mRecyclerView.setHasFixedSize(true);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        //ings = Ingredient.getAllForRecipe(recid);

        mAdapter = new RemovableItemListAdapter(reversed);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new RemovableItemTouchHelper(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(mRecyclerView);

    }

    // When "Add Ingredient" button is clicked, save ingredient to list of ingredients
    public void addIngredient(){
        View v = getView();
        // Get values for ingredient name, amount and measurement type
        addIngredientButton = (Button) v.findViewById(R.id.add_ingredient);
        name = (EditText) v.findViewById(R.id.ingredient);
        amount = (EditText) v.findViewById(R.id.amount);
        type = (Spinner) v.findViewById(R.id.measurementType);
        String nameTemp = name.getText().toString();
        String amountTemp = amount.getText().toString();
        String typeTemp = type.getSelectedItem().toString();

        if(nameTemp.length() == 0){
            name.setError("Name required.");
        }else if(amountTemp.length() == 0){
            amount.setError("Amount required.");
        } else {
            int amountStringToInt = Integer.parseInt(amountTemp);
            // Create new Ingredient and save to ingredients
            Ingredient temp = new Ingredient(Ingredient.ingid, recid, nameTemp, amountStringToInt, typeTemp);
            temp.save();
            Ingredient.ingid++;

            ings.add(temp);
            jsonIngs.add(gson.toJson(temp, Ingredient.class));

            List<Ingredient> reversed = ings;
            Collections.reverse(reversed);
            mAdapter.updateList(reversed);

            // Create new ShoppingListItem and save to ShoppingListItems
            ingredientsCount.setText(String.valueOf(ings.size()));

        }
    }

    // When "Add Recipe" button is clicked, save recipe to MyRecipes.recipes()
    public void addNewRecipe(){
        View v = getView();

        EditText recipe_name = (EditText) v.findViewById(R.id.title);
        EditText description = (EditText) v.findViewById(R.id.recipe_instructions);
        String recTemp = recipe_name.getText().toString();
        String desTemp = description.getText().toString();

        // Convert from List to Json
        String jsonIngredients = new Gson().toJson(jsonIngs);

        // Create new Recipe and save to MyRecipes
        Recipe temp = new Recipe();
        temp.remoteId = recid;
        recid++;
        temp.title = recTemp;
        temp.description = desTemp;
        temp.ingredients = jsonIngredients;
        temp.save();

        // Clear
        ings = null;
        openRecipes();
    }
    public void openRecipes() {
        // Create new fragment and transaction
        Fragment newFragment = new MyRecipes();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        transaction.replace(R.id.mainframe, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    public static void updateListSize(){
        ingredientsCount.setText(String.valueOf(ings.size()));
    }
}