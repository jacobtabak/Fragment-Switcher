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

package me.tabak.fragmentswitcher.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

import de.svenjacobs.loremipsum.LoremIpsum;

public class SampleFragment extends Fragment {
  public static final String KEY_TEXT = "text";
  private static final String KEY_RANDOM_STRINGS = "random_strings";
  private ListView mListView;
  private ArrayAdapter<String> mListAdapter;
  private ArrayList<String> mRandomStrings;

  public static SampleFragment newInstance(String text) {
    SampleFragment sampleFragment = new SampleFragment();
    Bundle args = new Bundle();
    args.putString(KEY_TEXT, text);
    sampleFragment.setArguments(args);
    return sampleFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      mRandomStrings = generateRandomStrings();
    } else {
      mRandomStrings = savedInstanceState.getStringArrayList(KEY_RANDOM_STRINGS);
    }
    initializeAdapter();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putStringArrayList(KEY_RANDOM_STRINGS, mRandomStrings);
  }

  private void initializeAdapter() {
    mListAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
    mListAdapter.addAll(mRandomStrings);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_sample, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    textView.setText(getArguments().getString(KEY_TEXT));
    mListView = (ListView) view.findViewById(R.id.fragment_list);
    mListView.setAdapter(mListAdapter);
  }

  private ArrayList<String> generateRandomStrings() {
    ArrayList<String> strings = new ArrayList<String>(100);
    LoremIpsum loremIpsum = new LoremIpsum();
    Random random = new Random();
    for (int i = 0; i < 100; i++) {
      strings.add(loremIpsum.getWords(10,random.nextInt(50)));
    }
    return strings;
  }
}
