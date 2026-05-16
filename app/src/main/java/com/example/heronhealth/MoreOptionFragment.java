package com.example.heronhealth;

import static android.app.ProgressDialog.show;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MoreOptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MoreOptionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    Switch switcher;
    boolean nightMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btnLogout;
    LinearLayout personalInfo, deleteAcc;
    View view;

    AlertDialog.Builder builder;
    MyDatabaseHelper myDb;

    public MoreOptionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MoreOptionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MoreOptionFragment newInstance(String param1, String param2) {
        MoreOptionFragment fragment = new MoreOptionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_more_option, container, false);

        switcher = view.findViewById(R.id.switchTheme);

        sharedPreferences = requireActivity().getSharedPreferences("theme_prefs", 0);
        editor = sharedPreferences.edit();

        nightMode = sharedPreferences.getBoolean("nightMode",false);

        if (nightMode){
            switcher.setChecked(true);
        }

        switcher.setOnCheckedChangeListener((buttonView, isChecked) ->{
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("nightMode", true);
            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("nightMode", false);
            }
            editor.apply();
        });

        personalInfo = view.findViewById(R.id.itemPersonalInfo);
        deleteAcc = view.findViewById(R.id.itemDeleteAccount);
        btnLogout = view.findViewById(R.id.btnLogout);
        builder = new AlertDialog.Builder(requireContext());
        myDb = new MyDatabaseHelper(requireContext());
        personalInfo.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), PersonalInfoActivity.class);
            startActivity(intent);

        });
        deleteAcc.setOnClickListener(view1 -> {

            builder.setTitle("Confirm Delete")
                    .setMessage("Are you sure you want to delete your account? This will permanently delete your account.");
                    builder.setCancelable(false).setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences sharedPref = requireActivity().getSharedPreferences("HeronHealthPrefs", android.content.Context.MODE_PRIVATE);
                            String email = sharedPref.getString("userEmail", "");

                            if (!email.isEmpty()) {
                                boolean isDeleted = myDb.softDeleteUser(email);

                                if (isDeleted) {
                                    Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                    logoutUser();


                                }
                            }

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        });
        btnLogout.setOnClickListener(view1 -> {
            logoutUser();
        });
        return  view;
    }
    private void logoutUser() {
        SharedPreferences sharedPref = requireActivity().getSharedPreferences("HeronHealthPrefs", android.content.Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}