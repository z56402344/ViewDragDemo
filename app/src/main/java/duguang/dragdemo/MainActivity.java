package duguang.dragdemo;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CustomDragLayout.onDragListener {

    private TextView mTvDragView;
    private CustomDragLayout mCdQue;
    private LinearLayout mLlEn,mLlCn;
    private ArrayList<QuesContent> mQueArr = new ArrayList<QuesContent>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mCdQue = (CustomDragLayout) findViewById(R.id.mCdQue);
        mLlCn = (LinearLayout) findViewById(R.id.mLlCn);
        mLlEn = (LinearLayout) findViewById(R.id.mLlEn);
        mTvDragView = (TextView) findViewById(R.id.mTvDragView);
        mCdQue.initData(mLlEn, mLlCn, mTvDragView);
        mCdQue.setDragListener(this);
        init();
    }

    public void init(){
        makeData();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        TextView v = null;
        for (int x = 0; x < mQueArr.size(); x++) {
            QuesContent qc = mQueArr.get(x);
            qc.t = x;
            lp.leftMargin = 54;
            lp.rightMargin = 54;
            lp.topMargin = x == 0 ? 0 : 30;
            v = (TextView) View.inflate(getApplicationContext(),R.layout.item_red_off_match_word2,null);
            setParent(v, mLlEn, lp);
            v.setHeight(100);
            CustomDragLayout.DragParam dp = new CustomDragLayout.DragParam();
            v.setText(qc.en);
            dp.mData = qc;
            v.setTag(dp);

            dp = new CustomDragLayout.DragParam();
            dp.mData = qc;
            v = (TextView) View.inflate(getApplicationContext(),R.layout.item_red_off_match_word1,null);
            v.setHeight(100);
            v.setText(qc.cn);
            v.setTag(dp);
            setParent(v,mLlCn,lp);
        }
    }

    //造假数据
    private void makeData() {
        QuesContent q1 = new QuesContent();
        q1.cn = "好";
        q1.en = "good";
        mQueArr.add(q1);
        q1 = new QuesContent();
        q1.cn = "吃";
        q1.en = "eat";
        mQueArr.add(q1);
        q1 = new QuesContent();
        q1.cn = "不";
        q1.en = "no";
        mQueArr.add(q1);
    }

    @Override
    public void onFilled(View v) {

    }

    @Override
    public void onFilledStatedChanged(View oldView, View newView) {
        if (newView != null && ((CustomDragLayout.DragParam) newView.getTag()).selectState != 1) newView.setBackgroundResource(R.drawable.btn_rectangle_oval_yellow4);
        if (oldView != null && ((CustomDragLayout.DragParam) oldView.getTag()).selectState != 1) {
            oldView.setBackgroundResource(R.drawable.selector_btn_red_off2);
            oldView.setSelected(false);
        }
    }

    @Override
    public void onRelaeseViewSelected(View v, View sv, View dv) {
        if (v != null) {
            v.setBackgroundResource(R.drawable.selector_btn_red_off2);
            v.setSelected(true);
            ((TextView) v).setText(((TextView) dv).getText());
        }else {
            if (sv != null) sv.setSelected(false);
        }
    }

    @Override
    public void onViewCapture(View dv, final View v,boolean istop) {
        if (v != null){
            v.setSelected(true);
            if (dv != null) ((TextView) dv).setText(((TextView) v).getText());
            if (!istop){
                final CustomDragLayout.DragParam dp = (CustomDragLayout.DragParam) v.getTag();
                if (dp != null && dp.mData != null)
                    ((TextView) v).setText(((QuesContent) dp.mData).cn);
            }
        }
    }

    @Override
    public void onViewReplace(View sv, View fv) {
        if (sv != null) sv.setSelected(false);
    }

    @Override
    public void onAllDone() {
        doneAll();
        Toast.makeText(this,"完成选择，进行数据对比",Toast.LENGTH_SHORT).show();
    }

    public static void setParent(View v, ViewGroup vg, ViewGroup.LayoutParams lp) {
        setParent(v, vg, lp, -1);
    }
    // 从父窗口移除
    public static void setParent(View v,ViewGroup vg,ViewGroup.LayoutParams lp, int order) {
        if (v==null) return;
        ViewGroup old = (ViewGroup)v.getParent();
        if ( old==vg) return;
        try { // 华为H60-L03
            if (old != null) old.removeView(v);
            if (vg!=null) {
                if (lp != null) {
                    if (order != -1){
                        vg.addView(v,lp);
                    }else{
                        vg.addView(v, order, lp);
                    }
                }else {
                    if (order != -1){
                        vg.addView(v, order);
                    }else{
                        vg.addView(v);
                    }
                }
            }
        } catch (Throwable e) {
        }
    }

    private boolean doneAll(){
        //清除拖拽。进行答案校对
        boolean pass = true;
        for (int x = 0; x < mLlCn.getChildCount(); x++){
            View c = mLlCn.getChildAt(x);
            CustomDragLayout.DragParam dP = (CustomDragLayout.DragParam) c.getTag();
            QuesContent qc1 = ((QuesContent) dP.mData);
            if (dP.startView != null){
                dP = (CustomDragLayout.DragParam) dP.startView.getTag();
                QuesContent qc2 = ((QuesContent) dP.mData);
                c.setBackgroundResource(qc1.t == qc2.t ? R.drawable.btn_rectangle_oval_green : R.drawable.btn_rectangle_oval_red2);
                if (pass && qc1.t != qc2.t) pass = false;
            }
        }
        mCdQue.removeView(mTvDragView);
        return pass;
    }


    public static class QuesContent{
        public String en;
        public String cn;
        int t = 0;
    }
}
