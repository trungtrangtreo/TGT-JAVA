package ca.thegreattrail.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import ca.thegreattrail.R;

public class MainAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private OnCloseNavigationViewListener listener;
    private int Header = 0;
    private int Normal = 1;
    private int Footer = 2;
    private ArrayList<String> navs;

    public MainAdapter(Context context, ArrayList<String> navs, OnCloseNavigationViewListener listener) {
        this.context = context;
        this.listener = listener;
        this.navs = navs;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == Normal) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_nav_main, parent, false);
            return new ItemViewHolder(view);
        } else if (viewType == Header) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.nav_header_main, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == Footer) {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.footer_nav_main, parent, false);
            return new FooterViewHolder(view);
        } else {
            View view = LayoutInflater.from(context)
                    .inflate(R.layout.item_nav_main, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {

          ((ItemViewHolder) holder).tvName.setText(navs.get(position-1));

            switch (position) {
                case 1:
                case 3:
                case 5:
                case 7:
                    ((ItemViewHolder) holder).tvName.setBackgroundColor(
                            ContextCompat.getColor(
                                    context,
                                    R.color.gray_5));
                    break;
                case 8:
                    ((ItemViewHolder) holder).tvName.setTextColor(ContextCompat.getColor(context, R.color.blue_1));
                    break;
            }

        } else if (holder instanceof FooterViewHolder) {

        } else {
            ((HeaderViewHolder) holder).ivClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCloseNavigationView();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return navs.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return Header;
        } else if (position == navs.size() + 1) {
            return Footer;
        } else {
            return Normal;
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        ImageView ivClose;

        public HeaderViewHolder(View view) {
            super(view);
            ivClose = view.findViewById(R.id.ivClose);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {

        public FooterViewHolder(View view) {
            super(view);

        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        TextView tvName;

        public ItemViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);

        }
    }

    interface OnCloseNavigationViewListener {
        void onCloseNavigationView();
    }


}
