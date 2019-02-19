package com.truebil.crm.CustomLayouts;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class StaticExpandedGridView extends GridView {

    public StaticExpandedGridView(Context context) {
        super(context);
    }

    public StaticExpandedGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StaticExpandedGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST));
        getLayoutParams().height = getMeasuredHeight();
    }
}
