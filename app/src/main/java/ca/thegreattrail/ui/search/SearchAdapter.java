package ca.thegreattrail.ui.search;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Locale;
import ca.thegreattrail.R;
/**
 * Created by hardikfumakiya on 2016-12-24.
 */

public class SearchAdapter extends BaseAdapter {

    Context mContext;
    ArrayList<ListViewItem> listItems;
    ArrayList<ListViewItem> searchItems;

    ArrayList<ListViewItem> sectionItems;

    //LayoutInflater inflater;
    public SearchAdapter(Context mContext, ArrayList<ListViewItem> listItems, ArrayList<ListViewItem> searchItem, ArrayList<ListViewItem> sectionItem) {
        this.mContext = mContext;
        this.listItems = listItems;
        //this.inflater = activity.getLayoutInflater();

        this.searchItems=searchItem;
        this.sectionItems=sectionItem;
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getType();
    }

    //Since we have two types of items here, we'll return 2.

    @Override
    public int getViewTypeCount() {
        return 2;
    }

//We'll use a switch case on the type and then typecast it to the relevant
// HeaderObject or the ListItemObject.

    //We'll also use the ViewHolder pattern so that android can recycle the views
//and we do not inflate it every time getView() is called. We'll need to create two ViewHolder //Objects for both the item types.
    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault()).trim();

        if(!charText.equals(""))
        {
            listItems.clear();
            int y=0;
            for(int i=0;i<sectionItems.size();i++)
            {
                ListViewItem sectionItem=sectionItems.get(i);
                listItems.add(sectionItem);
                int x=sectionItem.getNo_of_item();

                for(int j=y;j<(x+y);j++)
                {
                    if(searchItems.size()>j)
                    {
                        ListViewItem item=searchItems.get(j);
                        Log.d("filter data:"+charText,"text:"+item.getObject());
                        if(item.getObject()!=null && item.getObject().toLowerCase().contains(charText))
                            listItems.add(item);
                    }
                }
                y=x+y;
            }
        }
        else
        {
            listItems.clear();
            int y=0;
            for(int i=0;i<sectionItems.size();i++)
            {
                ListViewItem sectionItem=sectionItems.get(i);
                listItems.add(sectionItem);
                int x=sectionItem.getNo_of_item();
                for(int j=y;j<searchItems.size();j++)
                {
                    listItems.add(searchItems.get(j));
                }

                y=x+y;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return listItems.size();
    }

    @Override
    public ListViewItem getItem(int position) {
        return listItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //Let's assume the two layouts to inflated are called list_item_layout and header_layout.
    @Override
    public View getView(final int position, View convertView, ViewGroup parent)
    {
        int rowType = getItemViewType(position);
        ListViewItem item=listItems.get(position);
        TextView itemNameView = null;
        ViewHolderListItem holder;
        if(convertView == null){

            holder = new ViewHolderListItem();

            if(rowType == ListItemType.CONTEXT_PLUGIN_VIEW){
                // row title
                convertView = LayoutInflater.from(mContext).inflate(R.layout.search_item, parent,false);
                holder.itemNameView = (TextView)convertView.findViewById(R.id.text);

            }
            else if(rowType == ListItemType.HEADER_VIEW){
                // row item
                convertView = LayoutInflater.from(mContext).inflate(R.layout.section_item, parent,false);
                holder.itemNameView = (TextView)convertView.findViewById(R.id.textSeparator);

            }

            //holder.itemNameView=itemNameView;
            convertView.setTag(holder);

        }
        else{

            holder= (ViewHolderListItem)convertView.getTag();

            //itemNameView = (TextView)holder.itemNameView;
        }
        //Log.d("list :"+position,item.getObject());
        if(item.getObject().equals(mContext.getResources().getString(R.string.label_more)))
            holder.itemNameView.setTextColor(mContext.getResources().getColor(R.color.more));
        else
            if(rowType == ListItemType.HEADER_VIEW)
            holder.itemNameView.setTextColor(mContext.getResources().getColor(R.color.white));
        else
            holder.itemNameView.setTextColor(mContext.getResources().getColor(R.color.black));

        holder.itemNameView.setText(item.getObject());

        return convertView;
    }

    public class ViewHolderListItem {
        TextView itemNameView;
    }

    public class ListItemType {
        final public static int HEADER_VIEW = 0;
        final public static int CONTEXT_PLUGIN_VIEW = 1;
    }
}
