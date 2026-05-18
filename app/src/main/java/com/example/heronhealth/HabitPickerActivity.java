package com.example.heronhealth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.Arrays;
import java.util.List;

public class HabitPickerActivity extends AppCompatActivity {

    // All available habits — emoji + label
    public static final List<String[]> HABITS = Arrays.asList(
            new String[]{"🥚", "Eat more protein"},
            new String[]{"💧", "Drink more water"},
            new String[]{"🍎", "Eat more fruit"},
            new String[]{"🥦", "Eat more vegetables"},
            new String[]{"✅", "Log a daily meal"},
            new String[]{"🌾", "Eat more fiber"},
            new String[]{"💪", "Get more exercise"},
            new String[]{"🍸", "Drink less alcohol"},
            new String[]{"🍪", "Reduce added sugar"},
            new String[]{"🏃", "Hit my step goal"},
            new String[]{"😴", "Sleep 8 hours"},
            new String[]{"🧘", "Meditate daily"}
    );

    // SharedPreferences keys — shared with DashboardFragment
    public static final String PREF_HABIT_NAME       = "weekly_habit_name";
    public static final String PREF_HABIT_EMOJI      = "weekly_habit_emoji";
    public static final String PREF_HABIT_WEEK_START = "weekly_habit_week_start";

    private RecyclerView rvHabits;
    private MaterialButton btnNext;
    private String selectedHabitName  = "";
    private String selectedHabitEmoji = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_picker);

        rvHabits = findViewById(R.id.rvHabits);
        btnNext  = findViewById(R.id.btnNext);

        TextView btnClose = findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> finish());

        rvHabits.setLayoutManager(new LinearLayoutManager(this));
        rvHabits.setAdapter(new HabitAdapter());

        btnNext.setOnClickListener(v -> {

            if (selectedHabitName.isEmpty()) {
                Toast.makeText(this, "Please select a habit first.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Calculate the Monday of this week as the start point
            java.util.Calendar monday = java.util.Calendar.getInstance();
            int dow = monday.get(java.util.Calendar.DAY_OF_WEEK);
            int daysBack = (dow == java.util.Calendar.SUNDAY) ? 6 : dow - java.util.Calendar.MONDAY;
            monday.add(java.util.Calendar.DAY_OF_YEAR, -daysBack);

            String weekStart = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd", java.util.Locale.getDefault()
            ).format(monday.getTime());

            // 2. Pack the picked habit details into an Intent
            Intent intent = new Intent(HabitPickerActivity.this, HabitReminderActivity.class);
            intent.putExtra("HABIT_NAME", selectedHabitName);
            intent.putExtra("HABIT_EMOJI", selectedHabitEmoji);
            intent.putExtra("WEEK_START", weekStart);

            // 3. Move to the reminder screen to ask for the time
            startActivity(intent);

            // 4. Close the picker so the user can't accidentally navigate back into it
            finish();
        });
    }

    // ── Inner adapter ────────────────────────────────────────────────────────

    private class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitVH> {

        private int selectedPos = -1;

        @NonNull
        @Override
        public HabitVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit, parent, false);
            return new HabitVH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull HabitVH holder, int position) {

            String[] habit = HABITS.get(position);
            holder.tvEmoji.setText(habit[0]);
            holder.tvName.setText(habit[1]);

            // Highlight selected row
            holder.itemView.setBackgroundResource(
                    position == selectedPos
                            ? R.drawable.circle_done       // reuse your existing checked drawable
                            : R.drawable.box_uncheck
            );

            holder.itemView.setOnClickListener(v -> {
                int currentPos = holder.getBindingAdapterPosition();
                if (currentPos == RecyclerView.NO_POSITION) return;

                int old = selectedPos;
                selectedPos = currentPos;
                selectedHabitName = HABITS.get(selectedPos)[1];
                selectedHabitEmoji = HABITS.get(selectedPos)[0];

                notifyItemChanged(old);
                notifyItemChanged(selectedPos);
            });
        }

        @Override
        public int getItemCount() {
            return HABITS.size();
        }

        class HabitVH extends RecyclerView.ViewHolder {
            TextView tvEmoji, tvName;
            HabitVH(View v) {
                super(v);
                tvEmoji = v.findViewById(R.id.tvHabitEmoji);
                tvName  = v.findViewById(R.id.tvHabitName);
            }
        }
    }
}