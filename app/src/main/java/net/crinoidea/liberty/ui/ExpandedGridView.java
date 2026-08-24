package net.crinoidea.liberty.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * Expands to all rows so the surrounding screen scrolls as one unit.
 */
public final class ExpandedGridView extends GridView {
    public ExpandedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandedHeight = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST
        );
        super.onMeasure(widthMeasureSpec, expandedHeight);
    }
}
