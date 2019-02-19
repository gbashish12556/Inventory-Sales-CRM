package com.truebil.crm.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.truebil.crm.BuildConfig;
import com.truebil.crm.Constants;
import com.truebil.crm.Helper;
import com.truebil.crm.Network.VolleyService;
import com.truebil.crm.R;
import com.truebil.crm.Utils.ScalingUtilities;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

public class DeliveryActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private SharedPreferences sharedPref;
    private Spinner hypothecationSpinner;
    private TextView validationErrorTextView, deliveryDateTextView, loanRequiredTextView;
    private EditText deliveryNote, otherHypothecationEditText;
    private AmazonS3 s3;
    private TransferUtility transferUtility;
    private File photoFile;
    private static int PICK_IMAGE_CAMERA = 0;
    private static int PICK_IMAGE_GALLERY = 1;
    private String TAG = "DeliveryActivity", mCurrentPhotoPath = "", serviceString = "", uploadedFileUrl = "", photoTimestamp = "";
    private int buyerVisitListingId;
    private ArrayList<String> bookletListModel = new ArrayList<>(), twelveMonthRSACrossSoldList = new ArrayList<>();
    private ListingImagesRecyclerViewAdapter recyclerViewAdapter;
    private ArrayAdapter<String> hypothecationAdapter;
    private ProgressBar progressBar;
    private Boolean isBookletUploading = false;
    private LinearLayout twelveMonthRSACrossSoldLinearLayout;
    private ArrayList<String> hypothecationArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        Helper.setupKeyboardHidingUI(findViewById(android.R.id.content), this);

        sharedPref = getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        if (getIntent() != null) {
            Intent intent = getIntent();
            buyerVisitListingId = Integer.parseInt(intent.getStringExtra("BUYER_VISIT_LISTING_ID"));
        }

        RelativeLayout addBooklet = findViewById(R.id.add_booklet_relative_layout);
        LinearLayout confirmPaymentLinearLayout = findViewById(R.id.confirm_details_linear_layout);
        TextView cancelTextView = findViewById(R.id.navigate_to_invoice);
        progressBar = findViewById(R.id.activity_delivery_progress_bar);
        deliveryDateTextView = findViewById(R.id.delivery_activity_delivery_date_text_view);
        loanRequiredTextView = findViewById(R.id.delivery_activity_loan_required_text_view);
        ImageButton backButton = findViewById(R.id.activity_delivery_back_arrow);
        twelveMonthRSACrossSoldLinearLayout = findViewById(R.id.delivery_activity_tweleve_month_rsa_cross_sold);
        hypothecationSpinner = findViewById(R.id.delivery_activity_input_hypothecation_spinner);
        deliveryNote = findViewById(R.id.delivery_activity_delivery_note_edittext);
        validationErrorTextView = findViewById(R.id.activity_delivery_error_text_view);
        RecyclerView listingImagesRecyclerView = findViewById(R.id.item_booklet_listing_recycler_view);
        otherHypothecationEditText = findViewById(R.id.delivery_activity_other_hypothecation_edit_text);
        otherHypothecationEditText.setVisibility(View.GONE);

        // Set up the RecyclerView
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        listingImagesRecyclerView.setLayoutManager(horizontalLayoutManager);
        recyclerViewAdapter = new ListingImagesRecyclerViewAdapter(this, bookletListModel);
        listingImagesRecyclerView.setAdapter(recyclerViewAdapter);

        // Fill Hypothecation Spinner
        hypothecationArray = Helper.getHypothecationList(getApplicationContext());
        hypothecationArray.add(0, getString(R.string.hypothecation_prompt));
        hypothecationAdapter = new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_spinner_item, hypothecationArray);
        hypothecationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hypothecationSpinner.setAdapter(hypothecationAdapter);

        // Display Hypothecation Edit text when "Others" selected
        hypothecationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (hypothecationSpinner.getSelectedItem().toString().toLowerCase().contains("other")) {
                    otherHypothecationEditText.setVisibility(View.VISIBLE);
                }
                else {
                    otherHypothecationEditText.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Prepare Twelve Month RSA Options
        ArrayList<String> rsa12MonthsList = Helper.getTwelveMonthRsaList(getApplicationContext());
        for (final String key : rsa12MonthsList) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setTextSize(15);
            checkBox.setText(key);
            checkBox.setId(Helper.getTwelveMonthRsaId(key, getApplicationContext()));

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Boolean isChecked = ((CheckBox)view).isChecked();
                    if (isChecked) {
                        twelveMonthRSACrossSoldList.add(String.valueOf(Helper.getTwelveMonthRsaId(key, getApplicationContext())));
                    }
                    else {
                        twelveMonthRSACrossSoldList.remove(String.valueOf(Helper.getTwelveMonthRsaId(key, getApplicationContext())));
                    }
                }
            });
            twelveMonthRSACrossSoldLinearLayout.addView(checkBox);
        }

        credentialsProvider();
        setTransferUtility();
        fetchDeliveryData();

        addBooklet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        cancelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        confirmPaymentLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadedFileUrl = TextUtils.join(", ", bookletListModel);
                serviceString = TextUtils.join(",", twelveMonthRSACrossSoldList);

                try {
                    if(!isBookletUploading) {
                        if (isValidForm()) {
                            JSONObject params = new JSONObject();
                            params.put("buyer_visit_listing_id", String.valueOf(buyerVisitListingId));
                            params.put("additional_comments", deliveryNote.getText().toString());
                            params.put("rsa_12_month_service", serviceString);
                            params.put("service_scanned_copy", uploadedFileUrl);

                            if (otherHypothecationEditText.getVisibility() == View.VISIBLE) {
                                params.put("hypothecation", otherHypothecationEditText.getText().toString());
                            }
                            else {
                                params.put("hypothecation", hypothecationSpinner.getSelectedItem().toString());
                            }

                            sendPostVolleyRequest(params);
                        }
                    }
                    else {
                        Toast.makeText(DeliveryActivity.this,"Please wait while uploading booklet", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendPostVolleyRequest(final JSONObject params) {

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Constants.Config.API_PATH + "/delivery/";

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, params,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            Toast.makeText(getApplicationContext(), "Delivery details submitted", Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        params.put("API", "/delivery (POST)");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    VolleyService.handleVolleyError(error, params, true, DeliveryActivity.this);
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }

    public void fetchDeliveryData() {

        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(getApplicationContext());

        String url = Constants.Config.API_PATH + "/delivery/?buyer_visit_listing_id=" + buyerVisitListingId;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        Boolean status = (Boolean) response.get("status");
                        if (status) {
                            parseJSON(response);
                        }
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        JSONObject params = new JSONObject();
                        params.put("API", "/delivery (GET)");
                        params.put("BUYER_VISIT_LISTING_ID", buyerVisitListingId);
                        VolleyService.handleVolleyError(error, params, true, DeliveryActivity.this);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                String dealerJWTToken = sharedPref.getString(Constants.SharedPref.JWT_TOKEN, "");
                headers.put("Authorization", "jwt " + dealerJWTToken);
                return headers;
            }
        };

        jsonRequest.setTag(TAG);
        requestQueue.add(jsonRequest);
    }

    public void parseJSON(JSONObject response) {

        try {

            if (!response.getJSONObject("data").isNull("additional_comments")) {
                deliveryNote.setText(response.getJSONObject("data").getString("additional_comments"));
            }

            if (!response.getJSONObject("data").isNull("date_of_delivery")) {
                deliveryDateTextView.setText(response.getJSONObject("data").getString("date_of_delivery"));
            }

            if (!response.getJSONObject("data").isNull("hypothecation")) {
                String hypothecation = response.getJSONObject("data").getString("hypothecation");
                int spinnerPosition = hypothecationAdapter.getPosition(hypothecation);

                if (spinnerPosition < 0) {
                    otherHypothecationEditText.setVisibility(View.VISIBLE);
                    otherHypothecationEditText.setText(hypothecation);

                    /** Set spinner to "Others" */
                    // 1. Iterate through the loan tenure list
                    for (String hypothecationOption: Helper.getHypothecationList(getApplicationContext())) {
                        // 2. Find the option that resembles "other" string
                        if (hypothecationOption.toLowerCase().contains("other")) {
                            // 3. Set that option as spinner selection
                            spinnerPosition = hypothecationAdapter.getPosition(hypothecationOption);
                            hypothecationSpinner.setSelection(spinnerPosition);
                        }
                    }
                }
                else {
                    hypothecationSpinner.setSelection(spinnerPosition);
                }
            }

            String isAnyLoan = "No";
            if (!response.getJSONObject("data").isNull("is_any_loan")) {
                if (response.getJSONObject("data").getBoolean("is_any_loan")) {
                    isAnyLoan = "Yes";
                }
                else {
                    hypothecationAdapter.add("N.A.");
                    hypothecationAdapter.notifyDataSetChanged();
                    int spinnerPosition = hypothecationAdapter.getPosition("N.A.");
                    hypothecationSpinner.setSelection(spinnerPosition);
                }
            }
            loanRequiredTextView.setText(isAnyLoan);

            if (!response.getJSONObject("data").isNull("scanned_copy")) {
                uploadedFileUrl = response.getJSONObject("data").getString("scanned_copy");
                String[] array = uploadedFileUrl.split(",");
                for(int i=0;i<array.length;i++){
                    bookletListModel.add(array[i].toString().trim());
                }
                recyclerViewAdapter.notifyDataSetChanged();
            }
            if (!response.getJSONObject("data").isNull("is_12_month")) {
                serviceString = response.getJSONObject("data").getString("is_12_month");
            }

            if (!response.getJSONObject("data").isNull("is_12_month")) {
                String[] serviceWarranty = response.getJSONObject("data").getString("is_12_month").split(",");
                for (int i = 0; i < serviceWarranty.length;i++) {
                    twelveMonthRSACrossSoldList.add(serviceWarranty[i]);
                    ((CheckBox)twelveMonthRSACrossSoldLinearLayout.findViewById(Integer.parseInt(serviceWarranty[i].trim()))).setChecked(true);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //Upload Image Feature
    private void selectImage() {
        final CharSequence[] options = {"Take Photo", "Choose From Gallery"};
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    PackageManager pm = getPackageManager();
                    int hasPerm = pm.checkPermission(Manifest.permission.CAMERA, getPackageName());
                    if (hasPerm == PackageManager.PERMISSION_GRANTED) {
                        if (isWriteStoragePermissionGranted()) {
                            dialog.dismiss();
                            takeCameraPicture();
                        }
                    }
                    else {
                        dialog.dismiss();
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(DeliveryActivity.this, Manifest.permission.CAMERA)) {
                            requestCameraPermission();
                        }
                    }
                }
                else if (options[item].equals("Choose From Gallery")) {
                    if (isReadStoragePermissionGranted()) {
                        dialog.dismiss();
                        takePhotoFromGallery();
                    }
                }
            }
        });
        builder.show();
    }

    boolean isValidForm() {
        if (serviceString.isEmpty()) {
            validationErrorTextView.setVisibility(View.VISIBLE);
            validationErrorTextView.setText("RSA service is required");
            return false;
        }
        else if (deliveryNote.getText().toString().isEmpty()) {
            validationErrorTextView.setVisibility(View.VISIBLE);
            validationErrorTextView.setText("Deliver note  is required");
            return false;
        }
        else if (uploadedFileUrl.isEmpty()) {
            validationErrorTextView.setVisibility(View.VISIBLE);
            validationErrorTextView.setText("Booklet is required");
            return false;
        }
        else if (hypothecationSpinner.getSelectedItem().toString().equals(getString(R.string.hypothecation_prompt))) {
            validationErrorTextView.setVisibility(View.VISIBLE);
            validationErrorTextView.setText("Hypothecation not selected");
            return false;
        }
        else if (otherHypothecationEditText.getVisibility() == View.VISIBLE && otherHypothecationEditText.getText().toString().isEmpty()) {
            validationErrorTextView.setVisibility(View.VISIBLE);
            validationErrorTextView.setText("Hypothecation not filled");
            return false;
        }
        validationErrorTextView.setVisibility(View.GONE);
        return true;
    }

    private void takeCameraPicture() {
        if (checkCameraHardware(DeliveryActivity.this)) {
            try {
                photoFile = createImageFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile);
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, PICK_IMAGE_CAMERA);
            }
        }
        else {
            Toast.makeText(DeliveryActivity.this, "No Camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void takePhotoFromGallery() {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, PICK_IMAGE_GALLERY);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
    }

    public boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    public boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
            else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    if (isWriteStoragePermissionGranted()) {
                        takeCameraPicture();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestCameraPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
            case 2:
                takeCameraPicture();
                break;
            case 3:
                takePhotoFromGallery();
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(DeliveryActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_CAMERA) {
                String path = photoFile.getAbsolutePath();
                path = getRightAngleImage(path);
                compressPicture(path);
            }
            else if (requestCode == PICK_IMAGE_GALLERY) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = this.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String path = cursor.getString(columnIndex);
                compressPicture(path);
            }
        }
    }

    private String getRightAngleImage(String photoPath) {
        try {
            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;

            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    degree = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                case ExifInterface.ORIENTATION_UNDEFINED:
                    degree = 0;
                    break;
                default:
                    degree = 90;
            }
            return rotateImage(degree,photoPath);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return photoPath;
    }

    private String rotateImage(int degree, String imagePath) {
        if (degree<=0) {
            return imagePath;
        }
        try {
            Bitmap b= BitmapFactory.decodeFile(imagePath);

            Matrix matrix = new Matrix();
            if (b.getWidth()>b.getHeight()) {
                matrix.setRotate(degree);
                b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
            }

            FileOutputStream fOut = new FileOutputStream(imagePath);
            String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
            String imageType = imageName.substring(imageName.lastIndexOf(".") + 1);

            FileOutputStream out = new FileOutputStream(imagePath);
            if (imageType.equalsIgnoreCase("png")) {
                b.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            else if (imageType.equalsIgnoreCase("jpeg")|| imageType.equalsIgnoreCase("jpg")) {
                b.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            fOut.flush();
            fOut.close();

            b.recycle();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return imagePath;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        galleryAddPic();
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    //S3 Upload Feature
    public void setFileToUpload() {
        isBookletUploading = true;
        progressBar.setVisibility(View.VISIBLE);
        photoTimestamp = String.valueOf((new Date()).getTime());
        bookletListModel.add(Constants.Config.S3_PATH + "" + photoTimestamp + ".jpeg");
        TransferObserver transferObserver = transferUtility.upload(
                "truebil-test",     /* The bucket to upload to */
                photoTimestamp + ".jpeg",       /* The key for the uploaded object */
                photoFile,     /* The file where the data to upload exists */
                CannedAccessControlList.PublicRead
        );
        transferObserverListener(transferObserver);
    }

    public void credentialsProvider() {
        // Initialize the Amazon Cognito credentials provider
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-southeast-1:da2b64b2-d6b3-4c22-a608-5f374184f78a", // Identity Pool ID
                Regions.AP_SOUTHEAST_1 // Region
        );

        setAmazonS3Client(credentialsProvider);
    }

    public void setAmazonS3Client(CognitoCachingCredentialsProvider credentialsProvider) {
        // Create an S3 client
        s3 = new AmazonS3Client(credentialsProvider);

        // Set the region of your S3 bucket
        s3.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
    }

    public void setTransferUtility() {
        transferUtility = new TransferUtility(s3, getApplicationContext());
    }

    public void transferObserverListener(TransferObserver transferObserver) {
        transferObserver.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (String.valueOf(state) == "COMPLETED") {
                    recyclerViewAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    isBookletUploading = false;
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            }

            @Override
            public void onError(int id, Exception ex) {
                progressBar.setVisibility(View.GONE);
                isBookletUploading = false;
                Toast.makeText(DeliveryActivity.this,"Failed",  Toast.LENGTH_SHORT);
            }
        });
    }

    //Compress Image
    public void compressPicture(String path){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Uri reduceSizePath = getImageContentUri(this, decodeFile(path, Constants.Config.IMAGE_WIDTH, Constants.Config.IMAGE_HEIGHT));
        Cursor new_cursor = this.getContentResolver().query(reduceSizePath, filePathColumn, null, null, null);
        new_cursor.moveToFirst();
        int new_columnIndex = new_cursor.getColumnIndex(filePathColumn[0]);
        String newUploadedFileUrl = new_cursor.getString(new_columnIndex);
        photoFile = new File(newUploadedFileUrl);
        setFileToUpload();
    }

    public Uri getImageContentUri(Context context, String filePath) {
        File imageFile = new File(filePath);
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        }
        else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            }
            else {
                return null;
            }
        }
    }

    public String decodeFile(String path,int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if ((unscaledBitmap.getWidth() >= DESIREDWIDTH) || (unscaledBitmap.getHeight() >= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            }
            else {
                unscaledBitmap.recycle();
                return path;
            }

            // Store to tmp file
            String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/TMMFOLDER");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = "tmp.png";
            File f = new File(mFolder.getAbsolutePath(), s);
            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, fos);
                fos.flush();
                fos.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            scaledBitmap.recycle();
        }
        catch (Throwable e) {
            e.printStackTrace();
        }

        if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;
    }

    public class ListingImagesRecyclerViewAdapter extends RecyclerView.Adapter<ListingImagesRecyclerViewAdapter.ViewHolder> {
        private LayoutInflater mInflater;
        private ArrayList<String> bookLetList;
        private Context ctx;

        ListingImagesRecyclerViewAdapter(Context ctx, ArrayList<String> bookLetList) {
            this.mInflater = LayoutInflater.from(ctx);
            this.bookLetList = bookLetList;
            this.ctx = ctx;
        }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.item_booklet_image_view, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            final ImageView imageView = holder.listingImageView;
            final ImageView errorImageView = holder.errorImageView;
            errorImageView.setVisibility(View.GONE);
            String path = bookLetList.get(position);
            Picasso.with(ctx).load(path).into(imageView, new Callback() {
                @Override
                public void onSuccess() {
                }

                @Override
                public void onError() {
                    errorImageView.setVisibility(View.VISIBLE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return bookLetList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView listingImageView;
            ImageView errorImageView;

            ViewHolder(View itemView) {
                super(itemView);
                listingImageView = itemView.findViewById(R.id.booklet_image);
                errorImageView = itemView.findViewById(R.id.broke_image_view);
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
            }
        }
    }
}
