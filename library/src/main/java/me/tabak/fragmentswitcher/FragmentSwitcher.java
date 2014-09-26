/*
 * Copyright 2014 Jacob Tabak, adapted from ViewPager in the Android Support Library
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.tabak.fragmentswitcher;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


/**
 * A fragment switcher similar to a {@link android.support.v4.view.ViewPager}
 * that cannot be swiped and does not keep offscreen fragments like a ViewPager.
 *
 * The instance state of the fragments in the adapter will be managed automatically.
 * Perfect for use with tabs, navigation drawers, or any interface that switches fragments.
 *
 * Compatible adapters include {@link me.tabak.fragmentswitcher.FragmentStateArrayPagerAdapter},
 * {@link me.tabak.fragmentswitcher.FragmentArrayPagerAdapter},
 * {@link android.support.v4.app.FragmentStatePagerAdapter},
 * {@link android.support.v4.app.FragmentStatePagerAdapter}
 */
@SuppressWarnings("UnusedDeclaration")
public class FragmentSwitcher extends FrameLayout {
  private Fragment mCurrentFragment;
  private static final String TAG = "FragmentSwitcher";
  private static final boolean DEBUG = false;
  private PagerAdapter mAdapter;
  private PagerObserver mObserver;
  private int mExpectedAdapterCount;
  private boolean mPopulatePending;
  private boolean mFirstLayout;
  private int mRestoredCurItem;
  private Parcelable mRestoredAdapterState;
  private ClassLoader mRestoredClassLoader;
  private boolean mInLayout;
  private int mCurrentPosition;
  private OnPageChangeListener mOnPageChangeListener;

  public FragmentSwitcher(Context context) {
    super(context);
  }

  public FragmentSwitcher(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public FragmentSwitcher(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  /**
   * Set a PagerAdapter that will supply views for this pager as needed.
   *
   * @param adapter Adapter to use
   */
  public void setAdapter(PagerAdapter adapter) {
    if (mAdapter != null) {
      mAdapter.unregisterDataSetObserver(mObserver);
      mAdapter.startUpdate(this);
      mAdapter.destroyItem(this, mCurrentPosition, mCurrentFragment);
      mAdapter.finishUpdate(this);
      mCurrentPosition = 0;
    }

    mAdapter = adapter;
    mExpectedAdapterCount = 0;

    if (mAdapter != null) {
      if (mObserver == null) {
        mObserver = new PagerObserver();
      }
      mAdapter.registerDataSetObserver(mObserver);
      mPopulatePending = false;
      final boolean wasFirstLayout = mFirstLayout;
      mFirstLayout = true;
      mExpectedAdapterCount = mAdapter.getCount();
      if (mRestoredCurItem >= 0) {
        mAdapter.restoreState(mRestoredAdapterState, mRestoredClassLoader);
        setCurrentItemInternal(mRestoredCurItem, true);
        mRestoredCurItem = -1;
        mRestoredAdapterState = null;
        mRestoredClassLoader = null;
      } else if (!wasFirstLayout) {
        populate();
      } else {
        requestLayout();
      }
    }
  }

  private class PagerObserver extends DataSetObserver {
    @Override
    public void onChanged() {
      dataSetChanged();
    }
    @Override
    public void onInvalidated() {
      dataSetChanged();
    }
  }

  public int getCurrentItem() {
    return mCurrentPosition;
  }

  public Fragment getCurrentFragment() {
    return mCurrentFragment;
  }

  /**
   * Set the currently selected page.
   *
   * @param item Item index to select
   */
  public void setCurrentItem(int item) {
    setCurrentItemInternal(item, false);
  }

  void setCurrentItemInternal(int item, boolean always) {
    if (mAdapter == null || mAdapter.getCount() <= 0) {
      return;
    }
    if (!always && mCurrentPosition == item && mCurrentFragment != null) {
      return;
    }

    if (item < 0) {
      item = 0;
    } else if (item >= mAdapter.getCount()) {
      item = mAdapter.getCount() - 1;
    }

    if (mFirstLayout) {
      // We don't have any idea how big we are yet and shouldn't have any pages either.
      // Just set things up and let the pending layout handle things.
      mCurrentPosition = item;
      requestLayout();
    } else {
      populate(item);
    }
  }

  /**
   * Callback interface for responding to changing state of the selected page.
   */
  public interface OnPageChangeListener {
    public void onPageChanged(int page);
  }

  Fragment addNewItem(int position) {
    try {
      return (Fragment) mAdapter.instantiateItem(this, position);
    } catch (ClassCastException e) {
      throw new RuntimeException("FragmentSwitcher's adapter must instantiate fragments", e);
    }
  }

  void dataSetChanged() {
    // This method only gets called if our observer is attached, so mAdapter is non-null.

    final int adapterCount = mAdapter.getCount();
    mExpectedAdapterCount = adapterCount;
    boolean needPopulate = mCurrentFragment == null;
    int newCurrItem = mCurrentPosition;

    boolean isUpdating = false;
    final int newPos = mAdapter.getItemPosition(mCurrentFragment);

    if (newPos == PagerAdapter.POSITION_NONE) {
      if (!isUpdating) {
        mAdapter.startUpdate(this);
        isUpdating = true;
      }

      mAdapter.destroyItem(this, mCurrentPosition, mCurrentFragment);
      mCurrentFragment = null;

      // Keep the current item in the valid range
      newCurrItem = Math.max(0, Math.min(mCurrentPosition, adapterCount - 1));
      needPopulate = true;
    } else if (mCurrentPosition != newPos) {
      // Our current item changed position. Follow it.
      newCurrItem = newPos;
      needPopulate = true;
    }

    if (isUpdating) {
      mAdapter.finishUpdate(this);
    }

    if (needPopulate) {
      setCurrentItemInternal(newCurrItem, true);
      requestLayout();
    }
  }

  void populate() {
    populate(mCurrentPosition);
  }

  void populate(int position) {
    if (mAdapter == null) {
      return;
    }

    // Bail now if we are waiting to populate.  This is to hold off
    // on creating views from the time the user releases their finger to
    // fling to a new position until we have finished the scroll to
    // that position, avoiding glitches from happening at that point.
    if (mPopulatePending) {
      if (DEBUG) Log.i(TAG, "populate is pending, skipping for now...");
      return;
    }

    // Also, don't populate until we are attached to a window.  This is to
    // avoid trying to populate before we have restored our view hierarchy
    // state and conflicting with what is restored.
    if (getWindowToken() == null) {
      return;
    }

    final int N = mAdapter.getCount();

    if (N != mExpectedAdapterCount) {
      String resName;
      try {
        resName = getResources().getResourceName(getId());
      } catch (Resources.NotFoundException e) {
        resName = Integer.toHexString(getId());
      }
      throw new IllegalStateException("The application's PagerAdapter changed the adapter's" +
                                      " contents without calling PagerAdapter#notifyDataSetChanged!" +
                                      " Expected adapter item count: " + mExpectedAdapterCount + ", found: " + N +
                                      " Pager id: " + resName +
                                      " Pager class: " + getClass() +
                                      " Problematic adapter: " + mAdapter.getClass());
    }

    mAdapter.startUpdate(this);

    if (mCurrentFragment != null && mCurrentPosition != position) {
      mAdapter.destroyItem(this, mCurrentPosition, mCurrentFragment);
    }

    // Locate the currently focused item or add it if needed.
    if ((mCurrentFragment == null || mCurrentPosition != position) && mAdapter.getCount() > 0) {
      mCurrentFragment = addNewItem(position);
      mCurrentPosition = position;
      if (mOnPageChangeListener != null) {
        mOnPageChangeListener.onPageChanged(mCurrentPosition);
      }
    }

    mAdapter.setPrimaryItem(this, mCurrentPosition, mCurrentFragment);

    mAdapter.finishUpdate(this);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    mFirstLayout = true;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    mInLayout = true;
    populate();
    mInLayout = false;
  }

  /**
   * This is the persistent state that is saved by FragmentSwitcher.
   */
  public static class SavedState extends BaseSavedState {
    int position;
    Parcelable adapterState;
    ClassLoader loader;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(position);
      out.writeParcelable(adapterState, flags);
    }

    @Override
    public String toString() {
      return "FragmentSwitcher.SavedState{"
             + Integer.toHexString(System.identityHashCode(this))
             + " position=" + position + "}";
    }

    public static final Creator<SavedState> CREATOR
        = ParcelableCompat.newCreator(new ParcelableCompatCreatorCallbacks<SavedState>() {
      @Override
      public SavedState createFromParcel(Parcel in, ClassLoader loader) {
        return new SavedState(in, loader);
      }

      @Override
      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    });

    SavedState(Parcel in, ClassLoader loader) {
      super(in);
      if (loader == null) {
        loader = getClass().getClassLoader();
      }
      position = in.readInt();
      adapterState = in.readParcelable(loader);
      this.loader = loader;
    }
  }

  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.position = mCurrentPosition;
    if (mAdapter != null) {
      ss.adapterState = mAdapter.saveState();
    }
    return ss;
  }

  @Override
  public void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState ss = (SavedState)state;
    super.onRestoreInstanceState(ss.getSuperState());

    if (mAdapter != null) {
      mAdapter.restoreState(ss.adapterState, ss.loader);
      setCurrentItemInternal(ss.position, true);
    } else {
      mRestoredCurItem = ss.position;
      mRestoredAdapterState = ss.adapterState;
      mRestoredClassLoader = ss.loader;
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (!checkLayoutParams(params)) {
      params = generateLayoutParams(params);
    }
    if (mInLayout) {
      addViewInLayout(child, index, params);
    } else {
      super.addView(child, index, params);
    }
  }

  @Override
  public void removeView(View view) {
    if (mInLayout) {
      removeViewInLayout(view);
    } else {
      super.removeView(view);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    mFirstLayout = false;
  }

  /**
   * Retrieve the current adapter supplying pages.
   *
   * @return The currently registered PagerAdapter
   */
  public PagerAdapter getAdapter() {
    return mAdapter;
  }

  /**
   * Set a listener that will be invoked whenever the page changes or is incrementally
   * scrolled. See {@link OnPageChangeListener}.
   *
   * @param listener Listener to set
   */
  public void setOnPageChangeListener(OnPageChangeListener listener) {
    mOnPageChangeListener = listener;
  }
}
