package com.zhangyu.fleamarket.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etsy.android.grid.StaggeredGridView;
import com.zhangyu.fleamarket.R;
import com.zhangyu.fleamarket.adapter.PictureListAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PictureListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PictureListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PictureListFragment extends Fragment {

  private OnFragmentInteractionListener mListener;
  private View rootView;

  /**
   * Use this factory method to create a new instance of
   * this fragment using the provided parameters.
   *
   * @return A new instance of fragment PictureListFragment.
   */
  // TODO: Rename and change types and number of parameters
  public static PictureListFragment newInstance() {
    PictureListFragment fragment = new PictureListFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  public PictureListFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    rootView = inflater.inflate(R.layout.fragment_picture_list, container, false);

    int[] images = new int[] { R.drawable.ic_drawer,
      R.drawable.ic_drawer, R.drawable.ic_drawer,
      R.drawable.ic_drawer, R.drawable.ic_drawer };
    List<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
    for (int i = 0; i < 5; i++) {
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put("ItemImage", images[i]);
      map.put("ItemTitle", "This is Title " + i);
      map.put("ItemText", "This is text " + i);
      data.add(map);
    }

    PictureListAdapter listAdapter = new PictureListAdapter(this.getActivity().getApplicationContext(), data);
    StaggeredGridView gridView = (StaggeredGridView) rootView.findViewById(R.id.grid_view);
    gridView.setAdapter(listAdapter);
    return rootView;
  }

  // TODO: Rename method, update argument and hook method into UI event
  public void onButtonPressed(Uri uri) {
    if (mListener != null) {
      mListener.onFragmentInteraction(uri);
    }
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
//    try {
//      mListener = (OnFragmentInteractionListener) activity;
//    } catch (ClassCastException e) {
//      throw new ClassCastException(activity.toString()
//        + " must implement OnFragmentInteractionListener");
//    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {
    // TODO: Update argument type and name
    public void onFragmentInteraction(Uri uri);
  }

  public View getRootView() {
    return rootView;
  }

}
