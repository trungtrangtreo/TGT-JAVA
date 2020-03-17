package ca.thegreattrail.ui.traildetail;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

import ca.thegreattrail.R;

/**
 * Created by hardikfumakiya on 2017-03-09.
 */

public class RouteAdapter extends BaseAdapter {

    private ArrayList<DirectionTrailFragment.Step> mData ;
    Context context;

    private LayoutInflater mInflater;


    public RouteAdapter(Context context, ArrayList<DirectionTrailFragment.Step> routes) {
        this.context=context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mData=routes;
    }

    @Override
    public int getCount() {

        return mData.size();
    }

    @Override
    public DirectionTrailFragment.Step getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.route_item, null);
            holder.nameRoute = (TextView) convertView.findViewById(R.id.nameRoute);
            holder.durationRoute = (TextView) convertView.findViewById(R.id.durationRoute);
            holder.direction = (ImageView) convertView.findViewById(R.id.trailDirection);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        DirectionTrailFragment.Step route=mData.get(position);

        SpannableString instruction= new SpannableString(route.instructions);
        holder.nameRoute.setText(Html.fromHtml("<b>"+(position+1)+"</b>. "+instruction), TextView.BufferType.SPANNABLE);
        holder.durationRoute.setText(route.distance+"  "+route.duration);

        if(route.maneuver!=null){
            String manueverImg=FetchDirectionImage(route.maneuver);

            //Log.d(route.maneuver,""+manueverImg);

            int resImage = context.getResources().getIdentifier(manueverImg , "drawable", context.getPackageName());
            holder.direction.setImageResource(resImage);
        }
//        else{
//            holder.direction.setVisibility(View.INVISIBLE);
//        }

        return convertView;
    }

    private String FetchDirectionImage(String directionType) {

        if (directionType.equals("turn-sharp-left"))
            return "turn_sharp_left";
        else if (directionType.equals("uturn-right"))
            return "uturn_right";
        else if (directionType.equals("turn-slight-right"))
            return "turn_slight_right";
        else if (directionType.equals("merge"))
            return "merge";
        else if (directionType.equals("roundabout-left"))
            return "round_about_left";
        else if (directionType.equals("roundabout-right"))
            return "round_about_right";
        else if (directionType.equals("uturn-left"))
            return "uturn_left";
        else if (directionType.equals("turn-slight-left"))
            return "turn_slight_left";
        else if (directionType.equals("turn-left"))
            return "turn_left";
        else if (directionType.equals("ramp-right"))
            return "ramp_right";
        else if (directionType.equals("turn-right"))
            return "turn_right";
        else if (directionType.equals("fork-right"))
            return "fork_right";
        else if (directionType.equals("ferry-train"))
            return "ferry_train";
        else if (directionType.equals("turn-sharp-right"))
            return "turn_sharp_right";
        else if (directionType.equals("ramp-left"))
            return "ramp_left";
        else if (directionType.equals("ferry"))
            return "ferry";
        else if (directionType.equals("straight"))
            return "straight";
        else if (directionType.equals("fork-left"))
            return "fork_left";
        else
            return "null";
    }

    public class ViewHolder {
        TextView durationRoute;
        TextView nameRoute;
        ImageView direction;
    }

}
