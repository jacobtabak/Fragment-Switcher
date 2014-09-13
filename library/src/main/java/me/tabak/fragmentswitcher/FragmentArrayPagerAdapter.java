/*
 * Copyright 2014 Jacob Tabak
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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A {@link android.support.v4.app.FragmentPagerAdapter} that does not need to
 * be subclassed and can be filled like an array.
 */
@SuppressWarnings("UnusedDeclaration")
public class FragmentArrayPagerAdapter extends FragmentPagerAdapter {
  private List<Fragment> mItems = new ArrayList<Fragment>();

  public FragmentArrayPagerAdapter(FragmentManager fm) {
    super(fm);
  }

  @Override
  public Fragment getItem(int i) {
    return mItems.get(i);
  }

  @Override
  public int getCount() {
    return mItems.size();
  }

  /**
   * Adds the specified fragment at the end of the array.
   * @param fragment
   */
  public void add(Fragment fragment) {
    mItems.add(fragment);
    notifyDataSetChanged();
  }

  /**
   * Adds the specified Collection of fragments at the end of the array.
   * @param fragments
   */
  public void addAll(Collection<Fragment> fragments) {
    mItems.addAll(fragments);
    notifyDataSetChanged();
  }

  /**
   * Adds the specified fragments at the end of the array.
   * @param fragments
   */
  public void addAll(Fragment... fragments) {
    for (Fragment fragment : fragments) {
      mItems.add(fragment);
    }
    notifyDataSetChanged();
  }

  /**
   * Remove all elements from the list.
   */
  public void clear() {
    mItems.clear();
    notifyDataSetChanged();
  }

  /**
   * Inserts the specified fragment at the specified index in the array.
   * @param fragment
   * @param index
   */
  public void insert(Fragment fragment, int index) {
    mItems.add(index, fragment);
    notifyDataSetChanged();
  }
}
