package com.example.ky.bpm;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ky.bpm.Model.RecordModel;
import com.example.ky.bpm.comps.CommonFunctions;

import java.util.HashMap;
import java.util.List;

/**
 * Created by RockStar0116 on 2016.08.19.
 */
public class ExpandListAdapter extends BaseExpandableListAdapter {
    private Context _context;
    private List<RecordModel> header_list;
    private java.util.HashMap<RecordModel, List<RecordModel>> item_list;

    public ExpandListAdapter(Context context, List<RecordModel> header, HashMap<RecordModel,List<RecordModel>> items){
        this._context = context;
        this.header_list = header;
        this.item_list = items;
    }
    @Override
    public int getGroupCount() {
        return header_list.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.item_list.get(header_list.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.header_list.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.item_list.get(this.header_list.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        RecordModel header_item = (RecordModel)getGroup(groupPosition);
        if(convertView == null){
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_header, null);
        }
        TextView txt_date_header = (TextView)convertView.findViewById(R.id.txt_date_header);
        TextView txt_avg_header = (TextView)convertView.findViewById(R.id.txt_avg_header);
        ImageView img_indicator = (ImageView)convertView.findViewById(R.id.img_header_indicator);
        if(isExpanded){
            img_indicator.setImageResource(R.drawable.ic_expand_less_black_24dp);
        }
        else{
            img_indicator.setImageResource(R.drawable.ic_expand_more_black_24dp);
        }
        String date_header_content = header_item.getDate_history();
        txt_date_header.setText(date_header_content);
        txt_avg_header.setText(header_item.getBpm_history()+"");
        return convertView;
    }
    /////////////////
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        RecordModel item_model = (RecordModel)getChild(groupPosition,childPosition);
        if(convertView == null){
            LayoutInflater infalInflater = (LayoutInflater) this._context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
        TextView txt_date_item = (TextView)convertView.findViewById(R.id.txt_date_item);
        TextView txt_avg_item = (TextView)convertView.findViewById(R.id.txt_avg_item);
        txt_date_item.setText(item_model.getDate_history());
        txt_avg_item.setText(item_model.getBpm_history() + "");
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
