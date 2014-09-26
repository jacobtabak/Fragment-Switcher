FragmentSwitcher
================

FragmentSwitcher is an adapter-based, state-saving fragment container similar to a ViewPager.

It was designed with the NavigationDrawer and Tab patterns in mind but can be used
anywhere that you switch between fragments and would like to retain instance state.

* Retains instance state of Fragments even when removing them from your activity.
* Compatible with existing adapters: FragmentPagerAdapter and FragmentStatePagerAdapter
* Adapter determines fragment retention policy.  See the documentation for the two adapters above.
* Includes two new adapters: FragmentArrayPagerAdapter and FragmentStateArrayPagerAdapter. You no
longer have to subclass PagerAdapter to use FragmentSwitcher, just supply an array of fragments.

The sample app shows a demo of a navigation drawer with a lot fragments in a 
FragmentStateArrayPagerAdapter that can maintain the state of an edittext and listview.  The sample
also includes an example of saving a ListView's adapter content to InstanceState and restoring it
later.

Version History
---------------
1.1.0 (9/22/14)
* Added setOnPageChangeListener(FragmentSwitcher.OnPageChangeListener listener)
* Added support for generic fragment types in the FragmentSwitcher

1.0.0 (9/13/14)
* Initial release

Usage
-----

```java
mFragmentSwitcher = (FragmentSwitcher) findViewById(R.id.fragment_switcher)
mFragmentAdapter = new FragmentStateArrayPagerAdapter(getSupportFragmentManager());
mFragmentSwitcher.setAdapter(mFragmentAdapter);
List<Fragment> fragments = Arrays.asList(fragment1, fragment2, fragment3);
mFragmentAdapter.addAll(fragments);
mFragmentSwitcher.setCurrentItem(2);
```

Download
--------
Grab FragmentSwitcher with Gradle:

    compile 'com.timehop.fragmentswitcher:library:1.1.2'

License
-------
    Copyright 2014 Jacob Tabak

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.