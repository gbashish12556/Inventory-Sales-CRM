package com.truebil.crm.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.truebil.crm.Models.ServiceAmountListModel;
import com.truebil.crm.R;

import java.util.ArrayList;


public class ServiceAmountListToggleAdapter extends RecyclerView.Adapter {

    ArrayList<ServiceAmountListModel> serviceAmountDataList;
    Context mContext;

    public ServiceAmountListToggleAdapter(ArrayList<ServiceAmountListModel> serviceAmountDataList, Fragment mContext) {
        this.serviceAmountDataList = serviceAmountDataList;
        this.mContext = mContext.getContext();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new com.truebil.crm.Adapter.ServiceAmountListToggleAdapter.ServiceAmountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        ((com.truebil.crm.Adapter.ServiceAmountListToggleAdapter.ServiceAmountViewHolder) holder).bindData(serviceAmountDataList.get(position), mContext);
    }

    @Override
    public int getItemCount() {
        return serviceAmountDataList.size();
    }

    @Override
    public int getItemViewType(final int position) {
        return R.layout.item_services_with_toggle;
    }

    public class ServiceAmountViewHolder extends RecyclerView.ViewHolder {

        private TextView serviceNameTextView;
        private CheckBox isSelectedCheckBox;
        private Switch isInclusiveSwitch;
        private EditText amountEditTextView;

        ServiceAmountViewHolder(final View itemView) {
            super(itemView);
            serviceNameTextView =  itemView.findViewById(R.id.item_services_with_toggle_service_name);
            amountEditTextView = itemView.findViewById(R.id.item_services_with_toggle_is_inclusive_service_amount);
            isSelectedCheckBox = itemView.findViewById(R.id.item_services_with_toggle_is_selected);
            isInclusiveSwitch = itemView.findViewById(R.id.item_services_with_toggle_is_inclusive_switch);
        }

        void bindData(final ServiceAmountListModel viewModel, final Context mContext) {

            serviceNameTextView.setText(viewModel.getServiceName());
            isSelectedCheckBox.setChecked(viewModel.getIsSelected());
            isInclusiveSwitch.setChecked(viewModel.getInclusive());
            if (viewModel.getServiceAmount() != 0) {
                amountEditTextView.setText(String.valueOf(viewModel.getServiceAmount()));
            }

            isSelectedCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean isSelected  = ((CheckBox) view).isChecked();
                    viewModel.setSelected(isSelected);
                }
            });

            if (viewModel.getInclusive()) {
                amountEditTextView.setText("");
                amountEditTextView.setEnabled(false);
            }
            else {
                if (viewModel.getServiceAmount() != 0) {
                    amountEditTextView.setText(String.valueOf(viewModel.getServiceAmount()));
                }
                amountEditTextView.setEnabled(true);
            }

            isInclusiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    viewModel.setInclusive(isChecked);
                    if (isChecked) {
                        amountEditTextView.setText("");
                        amountEditTextView.setEnabled(false);
                        viewModel.setServiceAmount(0);
                    }
                    else {
                        amountEditTextView.setEnabled(true);
                    }
                }
            });

            amountEditTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    amountEditTextView.setSelection(amountEditTextView.getText().length());
                }
            });

            amountEditTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (!amountEditTextView.getText().toString().equalsIgnoreCase("")) {
                        viewModel.setServiceAmount(Integer.parseInt(amountEditTextView.getText().toString()));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
}

