package th.ac.ku.ixistic.taxifare;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ixistic on 5/21/16 AD.
 */
public class HistoryFragment extends Fragment {

    private Bundle bundle;
    private ArrayAdapter<String> adapter;
    private Firebase ref;
    private List listDel;
    private List history;
    private ListView listView;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_history, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        List data = new ArrayList();
        data.add("Please login before use this function.");
        adapter = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_list_item_1,data);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog diaBox = AskOption(position);
                diaBox.show();
            }
        });

        return view;
    }

    public void updateData(Bundle bundle){
        this.bundle = bundle;
        ref = new Firebase("https://taxifare.firebaseio.com/users/"+bundle.getString("idFacebook")+"/history/");
        // Attach an listener to read the data at our posts reference
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                listDel = new ArrayList();
                List data = new ArrayList();
                history = new ArrayList();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    History post = postSnapshot.getValue(History.class);
                    listDel.add(postSnapshot.getKey().toString());
                    data.add(post.getDate());
                    history.add(post);
                }
                Collections.reverse(listDel);
                Collections.reverse(data);
                Collections.reverse(history);
                updatedDataList(data);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.d("The read failed", firebaseError.getMessage());
            }
        });
    }
    public void updatedDataList(List itemsArrayList) {

        adapter.clear();

        adapter.addAll(itemsArrayList);

        adapter.notifyDataSetChanged();

    }

    private AlertDialog AskOption(final int position)
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle("Menu")
                .setMessage("Select option")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("View", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent appInfo = new Intent(getActivity(), HistoryDetailActivity.class);
                        appInfo.putExtra("idFacebook", bundle.getString("idFacebook"));
                        appInfo.putExtra("key", (String) listDel.get(position));
                        startActivity(appInfo);
                    }

                })


                .setNegativeButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        AlertDialog diaBox = AskOption2(position);
                        diaBox.show();
                        dialog.dismiss();
                    }

                })
                .create();
        return myQuittingDialogBox;

    }
    private AlertDialog AskOption2(final int position)
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getActivity())
                //set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
                .setIcon(android.R.drawable.ic_menu_delete)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        ref.child((String) listDel.get(position)).removeValue();
                        dialog.dismiss();
                    }

                })


                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;

    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences sp = getActivity().getSharedPreferences("Main", Context.MODE_PRIVATE);
        if(sp.getBoolean("status", false)) {
            loadPref();
        }
    }

    public void loadPref(){
        SharedPreferences sp = getActivity().getSharedPreferences("Main", Context.MODE_PRIVATE);
        bundle = new Bundle();
        bundle.putString("idFacebook", sp.getString("id", ""));
        bundle.putString("first_name", sp.getString("first_name", ""));
        bundle.putString("last_name", sp.getString("last_name", ""));
        bundle.putString("email", sp.getString("email", ""));
        bundle.putString("gender", sp.getString("gender", ""));
        bundle.putString("birthday", sp.getString("birthday", ""));
        bundle.putString("location", sp.getString("location", ""));
        bundle.putString("profile_pic", sp.getString("profile_pic", ""));
        updateData(bundle);
    }


}
