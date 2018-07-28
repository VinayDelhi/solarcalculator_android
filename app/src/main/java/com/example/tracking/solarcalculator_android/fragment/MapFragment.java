package com.example.tracking.solarcalculator_android.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.tracking.solarcalculator_android.R;
import com.example.tracking.solarcalculator_android.activity.MainActivity;


public class MapFragment extends Fragment {

    private View convertView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        convertView = inflater.inflate(R.layout.fragment_map, container, false);

        renderForm();
        return convertView;

    }

    private void renderForm(){
       // setActionbarTitle();
    }

    /*private void setActionbarTitle(){
        try{
            MainActivity mainActivity=(MainActivity) getActivity();
            if(mainActivity!=null){
                mainActivity.setToolbarTitle("Map");
            }
        }catch (Exception e){

            e.printStackTrace();
        }
    }*/

   /* private void addFragment (Fragment fragment){
        try{
            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(R.id.MainActivity_FrameContainer, fragment);
            ft.addToBackStack(null);
            ft.commit();
            // }
        }catch (Exception e){
            e.printStackTrace();
        }
    }*/
}
