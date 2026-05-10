package com.example.heronhealth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.heronhealth.model.FoodEntry;

import java.util.ArrayList;

public class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.FoodViewHolder> {

    public interface OnFoodClickListener {
        void onFoodClicked(FoodEntry food);
    }

    private final ArrayList<FoodEntry> foodList;
    private final OnFoodClickListener listener;

    public FoodSearchAdapter(ArrayList<FoodEntry> foodList, OnFoodClickListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_result, parent, false);

        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {

        FoodEntry food = foodList.get(position);

        holder.tvFoodName.setText(food.getName());

        holder.tvFoodMacros.setText(
                food.getCalories() + " kcal  |  "
                        + food.getProtein() + "g protein  |  "
                        + "per " + (int) food.getServingSize() + food.getUnit()
        );

        holder.itemView.setOnClickListener(v -> listener.onFoodClicked(food));
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {

        TextView tvFoodName, tvFoodMacros;

        FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFoodName   = itemView.findViewById(R.id.tvFoodName);
            tvFoodMacros = itemView.findViewById(R.id.tvFoodMacros);
        }
    }
}