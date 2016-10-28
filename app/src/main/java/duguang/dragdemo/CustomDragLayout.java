package duguang.dragdemo;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * 拖拽核心类
 */
public class CustomDragLayout extends RelativeLayout {

    private final ViewDragHelper mDragHelper;
    private onDragListener mDragListener;
    private View mDragView;//当前拖拽的View
    private LinearLayout mLlDrag1,mLlDrag2;
    private View mStartView;//拖拽后归位位置的View
    private Rect mRect = new Rect();
    //基准坐标系
    private Rect mBaseRect = new Rect();
    //是否完成所有
    private boolean mIsDoneAll = false;

    public CustomDragLayout(Context context) {
        this(context, null);
    }

    public CustomDragLayout(Context context, AttributeSet attrs) {
        super(context,attrs);
        mDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragHelper.processTouchEvent(event);
        return true;
    }

    public boolean moveViewToTarget(int x, int y,LinearLayout ll) {
        if (ll == null){
            return false;
        }
        if (mBaseRect.isEmpty()){
            getGlobalVisibleRect(mBaseRect);
        }
        x += mBaseRect.left;
        y += mBaseRect.top;
        ll.getGlobalVisibleRect(mRect);
        if (mRect.contains(x, y)){
            final int childCount = ll.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = ll.getChildAt(i);
                DragParam dp = (DragParam) child.getTag();
                mRect.setEmpty();
                child.getGlobalVisibleRect(mRect);
                if (mRect.contains(x, y)) {
                    //将位置移动到上边。
                    if (ll == mLlDrag1){
                        //当前源是否还有view可以用
                        if (dp.selectState == 1) return false;
                        mStartView = child;
                    }else {
                        //出坑,恢复坑的数据
                        if (dp.selectState == 0) return false;
                        mStartView = dp.startView;
                        dp.startView = null;
                        dp.selectState = 0;
                    }
                    //恢复源数据
                    if (mStartView != null) {
                        dp = (DragParam) mStartView.getTag();
                        dp.selectState = 0;
                    }
                    if (mDragListener != null) mDragListener.onViewCapture(mDragView,child,child == mStartView);
                    LayoutParams lp = (LayoutParams) mDragView.getLayoutParams();
                    lp.topMargin = mRect.top - mBaseRect.top;
                    lp.leftMargin = mRect.left - mBaseRect.left;
                    mDragView.layout(mRect.left - mBaseRect.left, mRect.top - mBaseRect.top, mRect.right - mBaseRect.left, mRect.bottom - mBaseRect.top);
                    mDragView.setVisibility(View.VISIBLE);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (MotionEvent.ACTION_DOWN == MotionEventCompat.getActionMasked(ev)){
            final float x = ev.getX();
            final float y = ev.getY();
            Log.i("onInterceptTouchEvent","x="+x+" ,y="+y);
            if (mDragHelper.getViewDragState() != 0 || mIsDoneAll) return true;
            if (!moveViewToTarget((int)x,(int)y,mLlDrag1)){
                moveViewToTarget((int)x,(int)y,mLlDrag2);
            }
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if(mDragHelper.continueSettling(true)) {
            Log.i("computeScroll","invalidate");
            invalidate();
        }
    }

    public void setDragListener(onDragListener dl){
        mDragListener = dl;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    //初始化对应的所有View
    public void initData(LinearLayout l1, LinearLayout l2, View dv){
        mLlDrag1 = l1;
        mLlDrag2 = l2;
        mDragView = dv;
        initTag(l1);
        initTag(l2);
        mIsDoneAll = false;
//        if (mDragView != null) mDragView.setVisibility(View.INVISIBLE);
    }

    //makeView
    public void initTag(LinearLayout ll){
        if (ll == null) return;
        for (int x = 0; x < ll.getChildCount(); x++){
            View v = ll.getChildAt(x);
            DragParam dp = new DragParam();
            v.setTag(dp);
        }
    }

    //拖拽的帮助类
    private class DragHelperCallback extends ViewDragHelper.Callback {

        private View mFilledView;
        private boolean mIsNeedRelease = false;
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //可以根据child来决定让哪个view被拖拽
            return mDragView != null && mDragView == child && !mIsDoneAll;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            Log.i("onViewDragStateChanged","state="+state);
            if (mLlDrag2 == null || mIsDoneAll) return;
            if (state == 0 && mIsNeedRelease){
                mIsNeedRelease = false;
                DragParam dp = null;
                boolean draglisNull = mDragListener != null;
                if (draglisNull) mDragListener.onRelaeseViewSelected(mFilledView,mStartView,mDragView);
                if (mFilledView != null) {
                    dp = (DragParam) mFilledView.getTag();
                    if (dp.startView != null && dp.startView != mStartView && draglisNull){
                        ((DragParam) dp.startView.getTag()).selectState = 0;
                        //需要恢复坑中原有的源
                        mDragListener.onViewReplace(dp.startView,mFilledView);
                    }
                    dp.startView = mStartView;
                    dp.selectState = 1;
                    //设置现在坑中源的数据
                    ((DragParam) mStartView.getTag()).selectState = 1;
                    mFilledView = null;
                }
                if (draglisNull) {
                    boolean isDone = true;
                    for (int x = 0; x < mLlDrag2.getChildCount(); x++){
                        View v = mLlDrag2.getChildAt(x);
                        dp = (DragParam) v.getTag();
                        if (dp.selectState == 0) {
                            isDone = false;
                            break;
                        }
                    }
                    if (isDone){
                        mIsDoneAll = true;
                        mDragListener.onAllDone();
                    }
                }
                if (mDragView != null) mDragView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            Log.i("onViewPositionChanged","left="+left+" ,top="+top+" ,dx="+dx+" ,dy="+dy);
            if (mLlDrag2 == null || mIsDoneAll) return;
            View fillView = null;
            Rect loc = new Rect();
            changedView.getGlobalVisibleRect(loc);
            //算出被移动的view的中心点
            int cx = (loc.right - loc.left)/2 + loc.left;
            int cy = (loc.bottom - loc.top)/2 + loc.top;
            for (int x = 0; x < mLlDrag2.getChildCount(); x++){
                View v = mLlDrag2.getChildAt(x);
                loc.setEmpty();
                v.getGlobalVisibleRect(loc);
                //判断中心点在哪个view的区域内，在就返回view，不在就返回null
                if (loc.contains(cx,cy)){
                    fillView = v;
                    break;
                }else {
                    fillView = null;
                }
            }
            if (mDragListener != null){
                if (mFilledView != fillView){
                    //抛给UI层，做对应变化
                    mDragListener.onFilledStatedChanged(mFilledView,fillView);
                    mFilledView = fillView;
                }
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            //释放的时候确定哪个view被选中了。
            Rect r = new Rect();
            //先判断是否将view拖到坑中。
            if (mFilledView != null){
                mFilledView.getGlobalVisibleRect(r);
            }else if (mStartView != null){
                mStartView.getGlobalVisibleRect(r);
            }
            mIsNeedRelease = true;
            if (!r.isEmpty()){
                mDragHelper.settleCapturedViewAt(r.left, r.top - 50);
                invalidate();
            }
        }

        @Override
        //被拖拽的view可以活动的边界
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return top;
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
        }
    }

    public interface onDragListener{

        //当有移动到有坑的view上
        void onFilled(View v);
        //当有占坑发生改变的时候
        void onFilledStatedChanged(View oldView, View newView);
        //当手指释放时候所占的view是哪个
        void onRelaeseViewSelected(View v, View sv, View dv);
        //当view被选择上
        void onViewCapture(View dv, View v, boolean isTop);
        //当源所占用的坑被替换时
        void onViewReplace(View sv, View fv);
        //当所有的坑都被占完的时候
        void onAllDone();
    }

    public static class DragParam{
        //预留参数
        public Object mData;
        //是否被选中 0未选中，1已选中
        public int selectState = 0;
        //坑中view的来源。
        public View startView = null;
    }
}
