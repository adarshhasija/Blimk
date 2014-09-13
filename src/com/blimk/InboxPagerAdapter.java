package com.blimk;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class InboxPagerAdapter extends FragmentPagerAdapter {

	public InboxPagerAdapter(FragmentManager fm) {
		super(fm);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Fragment getItem(int i) {
		Fragment fragment = new InboxFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(InboxFragment.ARG_OBJECT, i);
        fragment.setArguments(args);
        return fragment;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return "OBJECT " + (position + 1);
	}

}
