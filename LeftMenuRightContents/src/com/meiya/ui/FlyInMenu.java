package com.meiya.ui;

/**
 * 
 * @Description: TODO

 * @File: FlyInMenu.java

 * @Paceage com.meiya.ui

 * @Author huangxj

 * @Date ����10:51:34

 * @Version 
 */
import java.util.logging.Logger;

import com.meiya.adapter.MenuListAdapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

public class FlyInMenu extends FrameLayout {

	private static final int SNAP_VELOCITY = 400;
	private static final int FINAL_DP = 50;

	private int menuId, rightLayoutId;

	private View menu;
	private View rightLayout;

	private int touchSlop;

	private float lastMotionX;
	private Context myContext;

	private int testRight = 240;
	private float startX;
	private boolean sudu = false;

	private boolean isOpened = true;//ֻ��ʾcontent����
	private VelocityTracker velocityTracker;
	private int velocityX;

	public int duration = 500;
	public boolean linearFlying = true;
	private int finalDis;

	private Handler myHandler;
	private int everyMSpd;

	private enum State {
		ANIMATING, READY, TRACKING,
	};

	private State mState;

	public FlyInMenu(Context context, AttributeSet attrs) {
		super(context, attrs);
		myContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.FlyInMenu);
		duration = a.getInteger(R.styleable.FlyInMenu_animationDuration, 500);
		RuntimeException e = null;
		menuId = a.getResourceId(R.styleable.FlyInMenu_menu, 0);
		if (menuId == 0) {
			e = new IllegalArgumentException(
					a.getPositionDescription()
							+ ": The handle attribute is required and must refer to a valid child.");
		}
		rightLayoutId = a.getResourceId(R.styleable.FlyInMenu_content, 0);
		if (rightLayoutId == 0) {
			e = new IllegalArgumentException(
					a.getPositionDescription()
							+ ": The content attribute is required and must refer to a valid child.");
		}
		a.recycle();

		if (e != null) {
			throw e;
		}

		myHandler = new Handler();
		
		mState = State.READY;
		
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		menu = findViewById(menuId);
		if (menu == null) {
			throw new RuntimeException();
		}
		rightLayout = findViewById(rightLayoutId);
		if (rightLayout == null) {
			throw new RuntimeException();
		}

		removeView(menu);
		removeView(rightLayout);

		addView(menu);
		addView(rightLayout);
		//触发移动事件的最短距离
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		//获取屏幕分辨率
		final float scale = myContext.getResources().getDisplayMetrics().density;
		LayoutParams params = (LayoutParams)menu.getLayoutParams();
		params.rightMargin = (int) (FINAL_DP * scale + 0.5f);
		menu.setLayoutParams(params);
		
		initView();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		System.out.println("onLayout");
		super.onLayout(changed, left, top, right, bottom);
		final float scale = myContext.getResources().getDisplayMetrics().density;
		testRight = this.getWidth() - (int) (FINAL_DP * scale + 0.5f);
		//移动速度
		everyMSpd = (testRight * 16) / duration;
		
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(VIEW_LOG_TAG, "viewgroup intercept down");
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(VIEW_LOG_TAG, "viewgroup intercept move");
			break;
		case MotionEvent.ACTION_UP:
			Log.d(VIEW_LOG_TAG, "viewgroup intercept up");
			break;
		}

		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

//		Log.d(VIEW_LOG_TAG, "mState="+mState);
		
		Log.d("isOpened = ", "isopen="+isOpened);
		
		if (mState == State.ANIMATING) {
			return false;
		}

		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(event);

		final int action = event.getAction();
		float x = event.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(VIEW_LOG_TAG, "down");

			if (!isOpened) {
				if (event.getX() < rightLayout.getLeft())
					return false;
			}

			if (isOpened) {
				menu.setVisibility(VISIBLE);
			}

			lastMotionX = x;
			startX = x;
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.d(VIEW_LOG_TAG, "move");

			mState = State.TRACKING;

			int deltaX = (int) (lastMotionX - x);
			lastMotionX = x;
			// ���һ�������Ѿ������ұ߾�ֹͣ����
			if (deltaX < 0) {
				if (rightLayout.getLeft() >= testRight)
					break;
				if (deltaX < (rightLayout.getLeft() - testRight))
					deltaX = (int) rightLayout.getLeft() - testRight;

			}
			// ���󻬣� ������߾�ֹͣ����
			else {
				if (rightLayout.getLeft() < 0){
					break;
				}
				if (deltaX > rightLayout.getLeft())
					deltaX = rightLayout.getLeft();
			}

			// rightLayout.scrollBy(deltaX, 0);
			rightLayout.offsetLeftAndRight(-deltaX);
			// rightLayout.scrollBy(-deltaX, 0);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			Log.d(VIEW_LOG_TAG, "up");

			final VelocityTracker tempVelocityTracker = velocityTracker;
			tempVelocityTracker.computeCurrentVelocity(1000);
			velocityX = (int) tempVelocityTracker.getXVelocity();
			System.out.println("velocityX=" + velocityX);
			if (velocityX > SNAP_VELOCITY || velocityX < -SNAP_VELOCITY) {
				sudu = true;
			} else {
				sudu = false;
			}

			if (velocityTracker != null) {
				velocityTracker.recycle();
				velocityTracker = null;
			}

			// ������һ�ε������
			if (Math.abs(x - startX) < touchSlop) {
				if (isOpened) {
					Log.d("action up", "onclick...");
					
					menu.setVisibility(GONE);
					
					setToOpen();
				} else {
					setToClose();
				}
			}
			// ������һ�λ����¼�
			else {
				// ��һ�λ�������֮ǰ�Ǵ򿪵�״̬
				if (isOpened) {
					// ��һ������Ļ�����������
					if (x - startX < 0)
					{
						Log.d("action up", "fling to left...");
						
						menu.setVisibility(GONE);
						
						break;
					}

					// ��������ҵĻ���������ٶ�����ֵ

					if (sudu) {
						setToClose();
					}
					// ����ٶȲ�����ֵ
					else {
						// �ж��Ƿ񻬹�һ���������
						if (rightLayout.getLeft() > (testRight / 2)) {
							setToClose();
						} else {
							setToOpen();
						}
					}

				}
				// ���֮ǰ�ǹرյ�״̬
				else {
					// ��������ҵĻ�����������
					if (x - startX > 0)
						break;

					if (sudu) {
						setToOpen();
					} else {
						if (rightLayout.getLeft() < (testRight / 2)) {
							setToOpen();
						} else {
							setToClose();
						}
					}
				}
			}

			break;
		case MotionEvent.ACTION_CANCEL:
			mState = State.READY;
			break;
		}
		return true;
	}

	private void setToClose() {
		isOpened = false;
		finalDis = rightLayout.getLeft() - testRight;
		if (finalDis == 0) {
			mState = State.READY;
		} else {
			updateConUI(-finalDis, false);
		}
	}

	private void setToOpen() {
		isOpened = true;
		finalDis = rightLayout.getLeft();
		Log.d("finalDis", finalDis+"");
		if (finalDis == 0) {
			mState = State.READY;
			menu.setVisibility(View.GONE);
		} else {
			updateConUI(finalDis, true);
		}
	}

	private void updateConUI(int length, final boolean minus) {
		mState = State.ANIMATING;
		Log.d(VIEW_LOG_TAG, "mState=" + mState);
		int i = 0;
		while (length > 0) {
			if (length < everyMSpd) {
				if (minus)
					updatePer(-length, i);
				else
					updatePer(length, i);
				length = 0;
			} else {
				length -= everyMSpd;
				if (minus)
					updatePer(-everyMSpd, i);
				else
					updatePer(everyMSpd, i);
			}
			i++;
		}

	}

	private void updatePer(final int length, final int number) {

		myHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				rightLayout.offsetLeftAndRight(length);
				invalidate();
				if (rightLayout.getLeft() == 0
						|| rightLayout.getLeft() == testRight) {
					mState = State.READY;
					if (rightLayout.getLeft() == 0)
						menu.setVisibility(GONE);
					else {
						menu.setVisibility(VISIBLE);
					}
					System.out.println("end update state=" + mState);
					
				}

			}
		}, 16 * number);
	}
	
	//��ӵĲ���
	private MenuListAdapter menuListAdapter;
	
	private ListView mListView;
	
	private void initView(){
		mListView = (ListView) findViewById(R.id.list);
		menuListAdapter = new MenuListAdapter(myContext);
		mListView.setAdapter(menuListAdapter);
		menuListAdapter.notifyDataSetChanged();
	}

}
