# ViewDragDemo

一个拖拽View的Demo

# Describe

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


# ScreenShot

![Image][1]


[1]: https://github.com/z56402344/ViewDragDemo/blob/master/gif.gif

