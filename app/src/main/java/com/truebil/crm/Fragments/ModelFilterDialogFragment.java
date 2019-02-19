package com.truebil.crm.Fragments;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.truebil.crm.Adapters.ModelFilterGridViewAdapter;
import com.truebil.crm.Constants;
import com.truebil.crm.CustomLayouts.StaticExpandedGridView;
import com.truebil.crm.Models.FilterCarModel;
import com.truebil.crm.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ModelFilterDialogFragment extends DialogFragment {

    private static String TAG = "ModelFilterDialogFragment";
    private ModelFilterFragmentInterface mCallback;

    public ModelFilterDialogFragment() {
    }

    public interface ModelFilterFragmentInterface {
        void onSelectModel(FilterCarModel carModel, String listingMake);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (ModelFilterFragmentInterface) context;
        }
        catch (ClassCastException e) {
            Log.d(TAG, "Calling class must implement AddNewListingFragmentInterface");
        }

        // Check if parent Fragment implements listener
        if (getParentFragment() instanceof ModelFilterFragmentInterface) {
            mCallback = (ModelFilterFragmentInterface) getParentFragment();
        } else {
            throw new RuntimeException("The parent fragment must implement ModelFilterFragmentInterface");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_model_filter, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);
        LinearLayout contentLinearLayout = view.findViewById(R.id.dialog_fragment_model_filter_content_linear_layout);
        ImageButton closeButton = view.findViewById(R.id.dialog_fragment_model_filter_close_image_button);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        String makesJSONArray = sharedPref.getString(Constants.Keys.MAKES_LIST, null);
        if (makesJSONArray != null) {
            try {
                JSONArray makes = new JSONArray(makesJSONArray);
                for (int i = 0; i < makes.length(); i++) {
                    JSONObject make = makes.getJSONObject(i);
                    final String makeName = make.getString("name");
                    ArrayList<FilterCarModel> models = getAllModels(make.getJSONArray("models"));
                    View makeView = inflater.inflate(R.layout.item_model_filter, contentLinearLayout, false);
                    TextView listingMake = makeView.findViewById(R.id.item_model_filter_listing_make_text_view);
                    listingMake.setText(makeName);

                    StaticExpandedGridView gridView = makeView.findViewById(R.id.item_model_filter_models_grid_view);
                    ModelFilterGridViewAdapter adapter = new ModelFilterGridViewAdapter(models, getActivity());
                    gridView.setAdapter(adapter);
                    contentLinearLayout.addView(makeView);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            FilterCarModel carModel = (FilterCarModel) view.getTag();
                            Log.d(TAG, "onItemClick: " + carModel.getId() + " " + carModel.getName());
                            mCallback.onSelectModel(carModel, makeName);
                            getDialog().dismiss();
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    private ArrayList<FilterCarModel> getAllModels(JSONArray models) {
        ArrayList<FilterCarModel> temp = new ArrayList<>();
        for (int i = 0 ; i < models.length(); i++) {
            try {
                JSONObject model = models.getJSONObject(i);
                FilterCarModel mModel = new FilterCarModel(model.getString("name"), model.getInt("id"));
                temp.add(mModel);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return temp;
    }
}
