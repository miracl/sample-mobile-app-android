package com.miracl.mpinsdk.bootstrapsample;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.miracl.mpinsdk.MPinMfaAsync;
import com.miracl.mpinsdk.model.Status;
import com.miracl.mpinsdk.model.User;

import java.util.List;

/**
 * A fragment representing a list of Users.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnUsersFragmentInteractionListener}
 * interface.
 */
public class UsersFragment extends Fragment {
    private UserRecyclerViewAdapter mAdapter;
    private List<User> mUsers;
    private OnUsersFragmentInteractionListener mListener;

    public UsersFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            final RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

            SampleApplication.getMfaSdk().getUsers(new MPinMfaAsync.Callback<List<User>>() {
                @Override
                protected void onResult(@NonNull Status status, @Nullable List<User> result) {
                    super.onResult(status, result);
                    if (status.getStatusCode() == Status.Code.OK) {
                        mUsers = result;
                        mAdapter = new UserRecyclerViewAdapter(result, new UserRecyclerViewAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                mListener.onUsersFragmentInteraction(mUsers.get(position).getId());
                            }

                            @Override
                            public void onDeleteClick(int position) {
                                removeUser(position);
                            }
                        });
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.setAdapter(mAdapter);
                            }
                        });
                    }
                }
            });
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUsersFragmentInteractionListener) {
            mListener = (OnUsersFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void removeUser(final int position) {
        if (mUsers != null) {
            SampleApplication.getMfaSdk().deleteUser(mUsers.get(position), new MPinMfaAsync.Callback<Void>() {
                @Override
                protected void onResult(@NonNull Status status, @Nullable Void result) {
                    super.onResult(status, result);
                    mUsers.remove(position);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
        }
    }

    public interface OnUsersFragmentInteractionListener {
        void onUsersFragmentInteraction(String userId);
    }
}
