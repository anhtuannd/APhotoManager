package de.k3b.android.fotoviewer.directory;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import de.k3b.android.fotoviewer.R;
import de.k3b.io.Directory;
import de.k3b.io.DirectoryBuilder;
import de.k3b.io.DirectoryNavigator;

import java.util.Comparator;
import java.util.List;

/**
 * A fragment with a Listing of Directories.
 *
 * [pathBar]
 * [treeView]
 *
 * Activities that contain this fragment must implement the
 * {@link OnDirectoryInteractionListener} interface
 * to handle interaction events.
 */
public class DirectoryFragment extends Fragment {

    private static final String TAG = "DirFragment";

    // Layout
    private HorizontalScrollView pathBarScroller;
    private LinearLayout pathBar;
    private ExpandableListView treeView;

    // local data
    protected Activity mContext;
    private DirectoryListAdapter adapter;
    private DirectoryNavigator navigation;

    // api to fragment owner
    private OnDirectoryInteractionListener mListener;

    public DirectoryFragment() {
        // Required empty public constructor
    }

    /****** live cycle ********/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_directory, container, false);

        mContext = this.getActivity();

        this.pathBar = (LinearLayout) view.findViewById(R.id.path_owner);
        this.pathBarScroller = (HorizontalScrollView) view.findViewById(R.id.path_scroller);


        treeView = (ExpandableListView)view.findViewById(R.id.directory_tree);
        Directory directories = DirectoryLoader.getDirectories();
        DirectoryBuilder.createStatistics(directories.getChildren());
        navigation = new DirectoryNavigator(directories);

        adapter = new DirectoryListAdapter(mContext,
                navigation, treeView);
        treeView.setAdapter(adapter);

        treeView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return DirectoryFragment.this.onChildDirectoryClick(childPosition, navigation.getChild(groupPosition, childPosition));
            }
        });
        treeView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return DirectoryFragment.this.onParentDirectoryClick(navigation.getGroup(groupPosition));
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnDirectoryInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDirectoryInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /*********************** gui interaction *******************************************/
    private boolean onParentDirectoryClick(Directory dir) {
        return false;
    }

    private boolean onChildDirectoryClick(int childPosition, Directory selectedChild) {
        Log.d(TAG, "onChildDirectoryClick(" +
                selectedChild.getAbsolute() + ")");

        // naviationchange only if there are children below child
        Directory newGrandParent = ((selectedChild != null) && (selectedChild.getDirCount() > 0)) ? selectedChild.getParent() : null;

        navigateTo(childPosition, newGrandParent);
        updatePathBar(selectedChild);
        return false;
    }

    private void onPathBarButtonClick(Directory selectedChild) {
        Log.d(TAG, "onChildDirectoryClick(" +
                selectedChild.getAbsolute() + ")");

        // naviationchange only if there are children below child
        Directory newGrandParent = ((selectedChild != null) && (selectedChild.getDirCount() > 0)) ? selectedChild.getParent() : null;
        List<Directory> siblings = (newGrandParent != null) ? newGrandParent.getChildren() : null;

        if (siblings != null) {
            int childPosition = siblings.indexOf(selectedChild);
            navigateTo(childPosition, newGrandParent);
            updatePathBar(selectedChild);
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }

    /*********************** local helper *******************************************/

    private void updatePathBar(Directory selectedChild) {
        pathBar.removeAllViews();

        Button first = null;
        Directory current = selectedChild;
        while (current.getParent() != null) {
            Button button = createPathButton(current);
            // add parent left to chlild
            // gui order root/../child.parent/child
            pathBar.addView(button, 0);
            if (first == null) first = button;
            current = current.getParent();
        }

        // scroll to right where deepest child is
        pathBarScroller.requestChildFocus(pathBar, first);
    }

    private Button createPathButton(Directory currentDir) {
        Button result = new Button(getActivity());
        result.setTag(currentDir);
        result.setText(DirectoryListAdapter.getText(null, currentDir));

        result.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPathBarButtonClick((Directory) v.getTag());
            }
        });
        return result;
    }

    /** reload tree to new newGrandParent by preserving selection */
    private void navigateTo(int newGroupSelection, Directory newGrandParent) {
        if (newGrandParent != null) {
            Log.d(TAG, "=> setCurrentGrandFather(" +
                    newGrandParent.getAbsolute() + ")");
            navigation.setCurrentGrandFather(newGrandParent);
            this.treeView.setAdapter(adapter);
            if (newGroupSelection >= 0) {
                /// find selectedChild as new selectedGroup and expand it
                treeView.expandGroup(newGroupSelection, true);
            }
        }
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
    public interface OnDirectoryInteractionListener {
        // TODO: Update argument type and name
        // public void onFragmentInteraction(Uri uri);
    }

    public class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }

}