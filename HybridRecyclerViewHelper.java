package consumer.bixy.com.bixyconsumer.utils;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import consumer.bixy.com.bixyconsumer.R;
import consumer.bixy.com.bixyconsumer.databinding.InterestRowBinding;
import consumer.bixy.com.bixyconsumer.databinding.SubInterestRowBinding;
import consumer.bixy.com.bixyconsumer.model.response.GetInterestModel;

/**
 * Interest Selector Custom Handler. Its a wrapper around Recycler adapter.
 * <p>
 * Created by ankitgiri on 26-10-2016.
 */

/*
 * The trick used to create the expandable list.
 *  -We have used standard GridLayoutManager two types of rows, regular and expanded.
 *  -To make expandedView take all full horizontal space we have extended SpanSizeLookup.
 *
 */

public class HybridRecyclerViewHelper {
    private static final String TAG = "HolderTAG";

    private static final int ROW_TYPE_NORMAL = 10;
    private static final int ROW_TYPE_EXPANDED = 11;

    private Adapter mAdapter;
    private Context mContext;
    private boolean isExpanded;
    private int expandedPos;
    private int clickedPos;
    private InterestRowBinding clickedRowBinding;
    private ItemClickListener itemClickListener;
    private GridLayoutManager gridLayoutManager;

    public interface ItemClickListener {
        void itemClick(int pos);

        void subItemClick(int itemPos, int subItemPos, boolean isChecked);
    }

    public HybridRecyclerViewHelper(Context context, GridLayoutManager gridLayoutManager, ItemClickListener itemClickListener) {
        this.mContext = context;
        this.itemClickListener = itemClickListener;
        this.gridLayoutManager = gridLayoutManager;
        gridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
        mAdapter = new Adapter();
    }

    public Adapter getAdapter() {
        return mAdapter;
    }

    //We modify the Span size when a cell is clicked
    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        @Override
        public int getSpanSize(int position) {
            if (isExpanded && position == expandedPos) {
                return 3;
            } else {
                return 1;
            }
        }
    };

    //Wrapped adapter
    public class Adapter extends RecyclerView.Adapter<Holder> {
        List<GetInterestModel.Result> mArrayList = new ArrayList<>();

        @Override

        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;

            switch (viewType) {
                case ROW_TYPE_NORMAL:
                    view = LayoutInflater.from(mContext).inflate(R.layout.interest_row, parent, false);
                    break;

                case ROW_TYPE_EXPANDED:
                    view = LayoutInflater.from(mContext).inflate(R.layout.sub_interest_row, parent, false);
                    break;

            }
            return new Holder(view, viewType);
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            holder.bindHolder(position);

            switch (getItemViewType(position)) {
                case ROW_TYPE_NORMAL:
                    bindNormalRow(holder, position);
                    break;

                case ROW_TYPE_EXPANDED:
                    bindExpandedRow(holder, position);
                    break;

            }
        }

        @Override
        public int getItemCount() {
            return mArrayList.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (isExpanded && position == expandedPos) {
                return ROW_TYPE_EXPANDED;
            } else {
                return ROW_TYPE_NORMAL;
            }
        }

        private void bindNormalRow(Holder holder, int position) {
            holder.interestRowBinding.textView.setText(mArrayList.get(position).categoryName);
            holder.interestRowBinding.interestRowImg.setImageResource(getItemThumb(mArrayList.get(position)));
            if (isExpanded && position == clickedPos) {
                holder.interestRowBinding.cardContainer.setCardElevation(10);

            } else {
                holder.interestRowBinding.cardContainer.setCardElevation(3);
            }
        }

        private void bindExpandedRow(Holder holder, int position) {
            int columnPos = clickedPos % 3;
            if (columnPos == 0) {
                holder.subInterestRowBinding.subInterestParent.setBackgroundResource(R.drawable.dropbox_first);

            } else if (columnPos == 1) {
                holder.subInterestRowBinding.subInterestParent.setBackgroundResource(R.drawable.dropbox_second);

            } else if (columnPos == 2) {
                holder.subInterestRowBinding.subInterestParent.setBackgroundResource(R.drawable.dropbox_third);
            }

            AppCompatCheckBox checkBox;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            holder.subInterestRowBinding.leftContainer.removeAllViews();
            holder.subInterestRowBinding.rightContainer.removeAllViews();

            for (int i = 0; i < mArrayList.get(position).interests.size(); i++) {

                checkBox = (AppCompatCheckBox) LayoutInflater.from(mContext).inflate(R.layout.checkbox_interest, new LinearLayout(mContext), false);
                checkBox.setLayoutParams(layoutParams);
                checkBox.setText(mArrayList.get(position).interests.get(i).interestName);
                checkBox.setTag(i);
                checkBox.setOnClickListener(checkBoxClickListener);
                checkBox.setChecked(mArrayList.get(position).interests.get(i).isSelected);

                if (mArrayList.get(position).interests.get(i).isSelected) {
                    checkBox.setTypeface(null, Typeface.BOLD);
                } else {
                    checkBox.setTypeface(null, Typeface.NORMAL);

                }

                if (i % 2 == 0) {
                    holder.subInterestRowBinding.leftContainer.addView(checkBox);

                } else {
                    holder.subInterestRowBinding.rightContainer.addView(checkBox);

                }

            }


        }

        private int getItemThumb(GetInterestModel.Result interestModel) {
            return interestModel.selectedItemCount > 0 ? interestModel.resAct : interestModel.resInact;
        }

        public void setData(List<GetInterestModel.Result> mArrayList) {
            this.mArrayList = new ArrayList<>(mArrayList);
            notifyDataSetChanged();
        }

        public List<GetInterestModel.Result> getData() {
            return mArrayList;
        }

        private View.OnClickListener checkBoxClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) v;
                int pos = (int) v.getTag();
                int beforeCount = mArrayList.get(clickedPos).selectedItemCount;
                if (checkBox.isChecked()) {
                    mArrayList.get(clickedPos).selectedItemCount++;

                } else {
                    mArrayList.get(clickedPos).selectedItemCount--;

                }
                if (beforeCount == 0 && mArrayList.get(clickedPos).selectedItemCount == 1
                        || beforeCount == 1 && mArrayList.get(clickedPos).selectedItemCount == 0) {

                    notifyItemChanged(clickedPos);
                }

                if (checkBox.isChecked()) {
                    checkBox.setTypeface(null, Typeface.BOLD);
                } else {
                    checkBox.setTypeface(null, Typeface.NORMAL);

                }

                mArrayList.get(clickedPos).interests.get(pos).isSelected = checkBox.isChecked();
                itemClickListener.subItemClick(clickedPos, pos, checkBox.isChecked());

            }
        };
    }

    class Holder extends RecyclerView.ViewHolder {

        int pos;
        int rowType;
        InterestRowBinding interestRowBinding;
        SubInterestRowBinding subInterestRowBinding;

        Holder(View itemView, int type) {
            super(itemView);
            rowType = type;
            switch (type) {
                case ROW_TYPE_NORMAL:
                    interestRowBinding = DataBindingUtil.bind(itemView);
                    break;

                case ROW_TYPE_EXPANDED:
                    subInterestRowBinding = DataBindingUtil.bind(itemView);
                    break;
            }
        }

        void bindHolder(int position) {
            this.pos = position;
            switch (rowType) {
                case ROW_TYPE_NORMAL:
                    interestRowBinding.getRoot().setOnClickListener(interestRowClickListener);

                    break;

                case ROW_TYPE_EXPANDED:
                    subInterestRowBinding.getRoot().setOnClickListener(subInterestRowClickListener);
                    break;
            }
        }


        /*
        *Normal Row Click.
        * Here we identify the click pos and position to expand. or close the drawer
         */
        private View.OnClickListener interestRowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click bindHolder: " + Holder.this + " @ " + pos);
                itemClickListener.itemClick(pos);
                if (isExpanded) {
                    //Collapse
                    isExpanded = false;
                    //dummy data removed
                    mAdapter.mArrayList.remove(expandedPos);
                    mAdapter.notifyItemRemoved(expandedPos);
                    interestRowBinding.cardContainer.setCardElevation(3);
                    mAdapter.notifyItemRangeChanged(clickedPos, getAdapter().getItemCount() - clickedPos - 1);
                    ///gridLayoutManager.scrollToPosition(expandedPos - 1);

                } else {
                    //Expand
                    int modulo = pos % 3;
                    expandedPos = pos + (3 - modulo);
                    clickedPos = pos;

                    //dummy data
                    GetInterestModel.Result result = mAdapter.mArrayList.get(pos);
                    mAdapter.mArrayList.add(expandedPos, result);
                    mAdapter.notifyItemInserted(expandedPos);
                    isExpanded = true;
                    interestRowBinding.cardContainer.setCardElevation(10);
                    //clickedRowBinding = interestRowBinding;
                    gridLayoutManager.scrollToPositionWithOffset(expandedPos, v.getHeight() / 2);

                }
            }
        };

        //expanded row click.
        private View.OnClickListener subInterestRowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }
}
