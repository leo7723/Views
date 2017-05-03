package com.willplus.leo.stronger.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.willplus.leo.stronger.R;
import com.willplus.leo.stronger.Utills.DensityUtil;

import java.util.List;

/**
 * Created by changliliao on 2017/4/25.
 */

public class MyLineChart extends View {

    Context mContext;
    Paint mPaint;
    Paint mLinePaint;
    private int mXDown,mLastX;
    //滑动最短距离
    int a=0;

    float startX=0;
    float lastStartX =0;
    float cellCountW = 9.5f;
    float cellCountH = 12.5f;

    float cellH,cellW;
    float topPadding= 0.25f;

    PathEffect mEffect = new CornerPathEffect(20);//平滑过渡的角度

    int state = -100;
    int lineWidth;

    List<MyLineData> data;

    public void setData(List<MyLineData> data){
        this.data=data;
        state=-100;
        postInvalidate();
    }

    public MyLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext=context;
        a= (int) DensityUtil.px2dp(context, ViewConfiguration.get(context).getScaledDoubleTapSlop());
        setClickable(true);
        lineWidth=DensityUtil.dp2px(context,1);
        initPaint();
    }

    private void initPaint(){
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.blue));

        mLinePaint=new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(getResources().getColor(R.color.grey_lite));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        cellH=getHeight()/cellCountH;
        cellW=getWidth()/cellCountW;
        //画地步
        canvas.drawRect(0,(((int)cellCountH-1)+topPadding)*cellH,getWidth(),cellCountH*cellH,mPaint);

        if(data==null||data.size()==0){
            return;
        }

        DraWHorizontalLines(canvas);
        DraWVerticalLines(canvas);
        //--------画完背景----------

        canvas.saveLayer(0,0,getWidth(),getHeight(),mPaint,Canvas.ALL_SAVE_FLAG);
        
    }

    //画纵线
    public void DraWHorizontalLines(Canvas canvas){
        for(int i=0;i<11;i++){
            canvas.drawLine(0,(topPadding+i)*cellH,cellW*cellCountW,(topPadding+i)*cellH,mLinePaint);
        }
    }

    //画横坐标
    public void DraWVerticalLines(Canvas canvas){
        mLinePaint.reset();
        float i = 0.5f;
        for(MyLineData tmp:data){
            mLinePaint.setColor(getResources().getColor(R.color.grey_lite));
            mLinePaint.setTextSize(getWidth()/cellCountW/3.2f);
            MyLineData tmp2=getDataByX(mLastX);

            //加深选中项
            if(tmp2!=null&&tmp2.getTime().equals(tmp.getTime())&&state== MotionEvent.ACTION_UP&&Math.abs(mLastX-mXDown)<a){
                mLinePaint.setColor(getResources().getColor(R.color.grey));
            }else mLinePaint.setColor(getResources().getColor(R.color.grey_lite));

            String str1 =tmp.getTime().split(".")[0];
            canvas.drawText(str1,startX+cellW*i-mLinePaint.measureText(str1)/2
                    ,(((int)cellCountH-1)+topPadding+cellCountH)/2*cellH-1.5f*(mLinePaint.ascent()+mLinePaint.descent())
                    ,mLinePaint);

            //画背景竖线
            mLinePaint.setColor(getResources().getColor(R.color.grey_lite));
            canvas.drawLine(startX+cellW*i,topPadding*cellH,startX+cellW*i,(topPadding+10.5f)*cellH,mLinePaint);
            i++;

        }

        mPaint.setTextSize(getWidth()/cellCountW/3f);
        canvas.drawText("end",startX+cellW*i-mPaint.measureText("end")/2
                ,(((int)cellCountH-1)+topPadding+cellCountH)/2*cellH-(mPaint.ascent()+mPaint.descent())/2
                ,mPaint);
    }

    //画纵坐标
    public void DrawVertical(Canvas canvas){
        mLinePaint.reset();
        mLinePaint.setColor(getResources().getColor(R.color.grey));
        //BA最后一条线露出来
        canvas.drawRect(cellW*((int)cellCountW-0.5f+0.01f),0,cellW*((int)cellCountW+1),11.2f*cellH,mLinePaint);

        mLinePaint.setColor(getResources().getColor(R.color.blue));
        mLinePaint.setTextSize(getWidth()/cellCountW/3);

        int percent=100;
        for(int i=0;i<11;i+=2){
            canvas.drawText(String.valueOf(percent)+"%"
                    ,cellW*(int)cellCountW-mLinePaint.measureText(String.valueOf(percent)+"%")/2
                    ,(topPadding)*cellH-(mLinePaint.ascent()+mLinePaint.descent())/2
                    ,mLinePaint);
            percent-=20;
        }
    }

    //画渐变背景
    private void DrawDataBackground(Canvas canvas){
        if(data==null||data.size()==0){
            return;
        }
        LinearGradient lg=new LinearGradient(getWidth()/2,topPadding*cellH,getWidth()/2
                ,(topPadding+10)*cellH,getResources().getColor(R.color.dark_blue),getResources().getColor(R.color.blue)
                , Shader.TileMode.CLAMP);
        mPaint.setShader(lg);

        float i=0.5f;
        Path path=new Path();

        path.moveTo(startX+cellW*i,(topPadding+10)*cellH);
        path.lineTo(startX+cellW*i,(topPadding+10)*cellH);
        path.lineTo(startX+cellW*i,getHByValue(data.get(0).getNum()));
        for(MyLineData tmp:data){
            path.lineTo(startX+cellW*i,getHByValue(tmp.getNum()));
            i++;
        }
        path.lineTo(startX+cellW*(i-1),getHByValue(data.get(data.size()-1).getNum()));
        path.lineTo(startX+cellW*(i-1),(topPadding+10)*cellH-1);
        path.lineTo(startX+cellW*(i-1),(topPadding+10)*cellH);
        path.close();
        mPaint.setPathEffect(mEffect);
        canvas.drawPath(path,mPaint);
    }

    //画数据线
    public void DrawDataLine(Canvas canvas){
        float i =0.5f;
        mLinePaint.reset();
        mLinePaint.setStrokeWidth(lineWidth);
        mLinePaint.setColor(getResources().getColor(R.color.dark_blue));

        Path path=new Path();
        path.moveTo(startX+cellW*i-1,getHByValue(data.get(0).getNum()));
        path.lineTo(startX+cellW*i,getHByValue(data.get(0).getNum()));
        for(MyLineData tmp:data){
            path.lineTo(startX+cellW*i,getHByValue(data.get(0).getNum()));
        }
        path.lineTo(startX+cellW*(i-1),getHByValue(data.get(data.size()-1).getNum()));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setPathEffect(mEffect);
        canvas.drawPath(path,mPaint);
    }

    //显示气泡
    private void showPop(Canvas canvas){
        if(state==MotionEvent.ACTION_UP&&Math.abs(mLastX-mXDown)<a){
            MyLineData data1=getDataByX(mLastX);
            if(data1==null){
                return;
            }
            initPaint();
            //选中的线
            mLinePaint.setColor(getResources().getColor(R.color.dark_blue));
            canvas.drawLine(getXBykey(data1.getTime()),getHByValue(data1.getNum()),getXBykey(data1.getTime())
                    ,(topPadding+10f)*cellH,mLinePaint);
            //画气泡背景
            mPaint.setColor(0xffffffff);
            mPaint.setTextSize(getWidth()/cellCountW/3f);
            Paint.FontMetricsInt fontMetricsInt=mPaint.getFontMetricsInt();
            RectF r;

            //气泡距离顶点有0.5个格子高度的距离，气泡的高度是文字高度的1.5倍。宽度是文字宽度的1.6倍（0.8+0.8）
            float left = getXBykey(data1.getTime()) - mPaint.measureText(data1.getNum() + "%") * 0.8f;
            if(left < 0 ){
                left = 0;
            }
            float right = left + 2 * mPaint.measureText(data1.getNum() + "%") * 0.8f;
            if (data1.getNum() >= 10) {
                r = new RectF(left,
                        getHByValue(data1.getNum()) + 0.5f * cellH,
                        right,
                        getHByValue(data1.getNum()) + 0.5f * cellH + 1.5f * (fontMetricsInt.bottom - fontMetricsInt.top));
            } else {
                r = new RectF(left,
                        getHByValue(data1.getNum()) - 0.5f * cellH - 1.5f * (fontMetricsInt.bottom - fontMetricsInt.top),
                        right,
                        getHByValue(data1.getNum()) - 0.5f * cellH);
            }

            //画气泡上的文字
            canvas.drawRoundRect(r,90,90,mPaint);
            mPaint.setColor(getResources().getColor(R.color.dark_blue));

            float baseline = (r.bottom + r.top - fontMetricsInt.bottom - fontMetricsInt.top) / 2;

            canvas.drawText(data1.getNum()+"%",(r.left+r.right)/2-mPaint.measureText(data1.getNum()+"%")/2f
                    ,baseline,mPaint);
        }
    }
    //画线上的圆
    private void DrawCircle(Canvas canvas,MyLineData data1){
        mPaint.reset();
        mPaint.setStrokeWidth(lineWidth*2);
        mPaint.setColor(getResources().getColor(R.color.dark_blue));
        canvas.drawCircle(getXBykey(data1.getTime()), getHByValue(data1.getNum()), lineWidth * 5, mPaint);
        mPaint.setColor(0xffffffff);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(getXBykey(data1.getTime()), getHByValue(data1.getNum()), lineWidth * 5, mPaint);

        mPaint.setStrokeWidth(lineWidth);
    }


    //通过坐标获得附近的点
    private MyLineData getDataByX(int pointX){
        float i =0.5f;
        MyLineData result =  null;
        for(MyLineData tmp:data){
            float x =startX+cellW*i;
            if(Math.abs(x-pointX)<cellW/2){
                result=tmp;
                return result;
            }
            i++;
        }
        return result;
    }

    private float getHByValue(float value){
        return (topPadding+10)*cellH-(cellH*10)*value/100;
    }
    //通过横坐标文字获取坐标
    private float getXBykey(String key){
        float i =0.5f;
        for(MyLineData tmp:data){
            if(tmp.getTime().equals(key)){
                return startX+cellW*i;
            }
            i++;
        }
        return 0;
    }

    private void gotoEnd(){
        if(data==null||data.size()==0){
            return;
        }
        if(data.size()<cellCountW-1){
            startX=0;
            lastStartX=startX;
            postInvalidate();
            return;
        }
        startX=-(cellW)*(data.size()-cellCountW+1);
        lastStartX=startX;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (data == null || data.size() == 0) {
            return super.onTouchEvent(event);
        }
        final int action = event.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 按下
                mXDown = (int) event.getRawX();
                state = MotionEvent.ACTION_DOWN;
                break;

            case MotionEvent.ACTION_MOVE:
                // 移动
                mLastX = (int) event.getRawX();

                if (Math.abs(lastStartX - mXDown) < a) {
                    break;
                }

                //滑动限制
                if (lastStartX + mLastX - mXDown > 0.5f * cellW || lastStartX + mLastX - mXDown + cellW * (data.size() + 0.5f) < cellW * (cellCountW - 1)) {
                    break;
                }
                state = MotionEvent.ACTION_MOVE;
                startX = lastStartX + mLastX - mXDown;
                postInvalidate();
                break;

            case MotionEvent.ACTION_UP:
                // 抬起
                lastStartX = startX;
                state = MotionEvent.ACTION_UP;
                postInvalidate();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
