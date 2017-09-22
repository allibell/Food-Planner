package com.foodplanner;

/**
 * Thanks & credit to https://medium.com/@ipaulpro/drag-and-swipe-with-recyclerview-b9456d2b1aaf#.ndllchrdz
 */

public interface ItemTouchHelperAdapter {

    boolean onItemMove(int fromPosition, int toPosition);

    void onItemDismiss(int position);

    void onItemSwipeEnd(int position);
}
