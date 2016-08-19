package com.example.ky.bpm.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.ToggleButton;

import com.example.ky.bpm.R;
public class HistoryViewFragment extends Fragment {
    private final RadioGroup.OnCheckedChangeListener ToggleListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            for(int j = 0; j < group.getChildCount(); j++){
                final ToggleButton view = (ToggleButton)group.getChildAt(j);
                view.setChecked(view.getId() == checkedId);
            }
        }
    };
    private RadioGroup m_group;
    private ToggleButton toggle_day, toggle_week, toggle_month, toggle_year;

    public HistoryViewFragment() {
        // Required empty public constructor
    }

    public static HistoryViewFragment newInstance() {
        HistoryViewFragment fragment = new HistoryViewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootview = inflater.inflate(R.layout.fragment_history_view, container, false);
        m_group  =(RadioGroup)rootview.findViewById(R.id.toggle_group);
        m_group.setOnCheckedChangeListener(ToggleListener);
        toggle_day = (ToggleButton)rootview.findViewById(R.id.toggle_day);
        toggle_day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_week = (ToggleButton)rootview.findViewById(R.id.toggle_week);
        toggle_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_month = (ToggleButton)rootview.findViewById(R.id.toggle_month);
        toggle_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        toggle_year = (ToggleButton)rootview.findViewById(R.id.toggle_year);
        toggle_year.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleClick(v);
            }
        });
        return rootview;
    }
    public void onToggleClick(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
        // app specific stuff ..
    }
}
