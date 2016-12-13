package ru.gkpromtech.exhibition.organizations;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.utils.ImageLoader;

class OrganizationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements Filterable {

    private int mSpanCount = 1;

    public List<OrganizationsFragment.Item> mItems;
    private OrganizationsFilter mFilter;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private OnItemClickListener mClickListener;

    public void setOnItemClickListener(OnItemClickListener clickListener) {
        mClickListener = clickListener;
    }

    public class OrganizationViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageLogo;
        public TextView textName;
        public TextView textPlaceCorner;
        public TextView textPlace;

        OrganizationViewHolder(View itemView) {
            super(itemView);
            imageLogo = (ImageView) itemView.findViewById(R.id.imageLogo);
            textName = (TextView) itemView.findViewById(R.id.textName);
            textPlaceCorner = (TextView) itemView.findViewById(R.id.textPlaceCorner);
            textPlace = (TextView) itemView.findViewById(R.id.textPlace);

            itemView.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mClickListener != null) {
                                mClickListener.onItemClick(v, getLayoutPosition());
                            }
                        }
                    }
            );
        }
    }

    public OrganizationsAdapter(List<OrganizationsFragment.Item> items) {
        super();
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        View view = layoutInflater.inflate(R.layout.layout_organizations_list_item,
                parent, false);

        OrganizationViewHolder viewHolder = new OrganizationViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        OrganizationsFragment.Item item = mItems.get(position);

        OrganizationViewHolder organizationViewHolder = (OrganizationViewHolder) viewHolder;

        organizationViewHolder.textName.setText(item.organization.shortname);

        if (item.placesStr == null) {
            organizationViewHolder.textPlaceCorner.setVisibility(View.GONE);
            organizationViewHolder.textPlace.setVisibility(View.GONE);
        } else {
            organizationViewHolder.textPlaceCorner.setVisibility(View.VISIBLE);
            organizationViewHolder.textPlace.setVisibility(View.VISIBLE);
            organizationViewHolder.textPlace.setText(item.placesStr);
        }

        ImageLoader.load(item.organization.logo, organizationViewHolder.imageLogo, R.drawable.no_logo,
                true);

        int k;

        if (mSpanCount == 1) {
            k = position % 2;
        }
        else {
            k = (position % mSpanCount) % 2;
            int v = (position / mSpanCount) % 2;

            if (v > 0) {
                k = Math.abs(k - 1);
            }
        }

        viewHolder.itemView.setBackgroundResource(k == 0
                ? R.drawable.listview_selector_even
                : R.drawable.listview_selector_odd);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new OrganizationsFilter(this);
        }

        return mFilter;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof StaggeredGridLayoutManager) {
            mSpanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
    }

}
