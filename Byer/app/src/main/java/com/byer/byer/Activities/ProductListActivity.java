package com.byer.byer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;


import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;



import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byer.Adapters.ProductListAdapter;
import com.byer.byer.Models.ProductModel;
import com.byer.byer.Models.SubcategoryModel;
import com.byer.byer.R;
import com.byer.byer.databinding.ActivityProductListBinding;
import com.byer.byer.databinding.ProductSubcategoryListItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


import static com.byer.byer.Byer.productRef;
import static com.byer.byer.Byer.subcategoryRef;
import static com.byer.byer.Byer.userRef;

public class ProductListActivity extends AppCompatActivity {

    private static final String CATEGORY_TAG ="category" ;
    private ActivityProductListBinding binding;
    private static final String NAME_TAG="name";
    private static final String PHONE_TAG="phone";
    private static final String IMAGE_TAG="image";
    private static final String SUBLOCALITY_TAG="sublocality";
    private static final String DISTANCE_TAG="distance";
    private static final String KEY_TAG="key";
    private String name,phone,image,sublocality;

    public static int DominantRGB;
    private FirebaseAuth mAuth;
    private String userId;
    private Query itemQuery,subCategoryQuery;
    private Resources res;
    private LinearLayoutManager itemLinearLayoutManager,subcategoryLinearLayoutManager;
    private Snackbar snackbar ;
    private TextView snackbarText;
    private TextView snackbarCartBtn;
    private String inCartId;
    private boolean isCartHasItemsFromOtherRetailer=false;
    public static String category,distance,retailerId;
    private  int item_index=0;


    private FirebaseRecyclerOptions<SubcategoryModel> subCategoryOptions;
    private FirebaseRecyclerAdapter<SubcategoryModel,SubcategoryViewHolder> subCategoryAdapter;

    private List<String> keyList = new ArrayList<>();
    private List<ProductModel> itemList = new ArrayList<>();
    private List<ProductModel> itemListAll = new ArrayList<>();
    private ProductListAdapter itemAdapter=new ProductListAdapter(this,keyList,itemList,itemListAll);



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        getCartPrompt();

    }

    @Override
    protected void onStart() {
        super.onStart();
        getSubcategories(subCategoryQuery);
        getProducts(itemQuery);
    }

    private void getSubcategories(Query subCategoryQuery) {
        subCategoryOptions=new FirebaseRecyclerOptions.Builder<SubcategoryModel>().setQuery(subCategoryQuery,SubcategoryModel.class).build();
        subCategoryAdapter=new FirebaseRecyclerAdapter<SubcategoryModel, SubcategoryViewHolder>(subCategoryOptions) {
            @Override
            protected void onBindViewHolder(@NonNull SubcategoryViewHolder holder, int position, @NonNull SubcategoryModel model) {


                Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder)
                        .networkPolicy(NetworkPolicy.OFFLINE).into(holder.subcategoryBinding.subcategoryListImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.ic_image_placeholder)
                                .into(holder.subcategoryBinding.subcategoryListImage);
                    }
                });

                holder.subcategoryBinding.subcategoryListName.setText(model.getName());

                holder.subcategoryBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item_index=position;
                        Query query;
                        if(position==0){
                            query=itemQuery;
                        }else{
                            query=productRef.child(retailerId).orderByChild("subcategory").equalTo(model.getName());
                        }
                        getProducts(query);
                        notifyDataSetChanged();
                    }
                });

                if (item_index==position){
              //      holder.itemView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.logo_color_pay_info,null)));
                    holder.itemView.setBackgroundTintList(ColorStateList.valueOf(DominantRGB));
                    if (isColorDark(DominantRGB)){
                        holder.subcategoryBinding.subcategoryListName.setTextColor(getResources().getColor(R.color.white,null));
                    }else{
                        holder.subcategoryBinding.subcategoryListName.setTextColor(getResources().getColor(R.color.black,null));
                    }

                }
                else{
                    holder.itemView.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white,null)));
                    holder.subcategoryBinding.subcategoryListName.setTextColor(getResources().getColor(R.color.grey,null));
                }


            }

            @NonNull
            @Override
            public SubcategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new SubcategoryViewHolder(ProductSubcategoryListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        };

        subCategoryAdapter.startListening();
        binding.productSubcategoryList.setAdapter(subCategoryAdapter);

    }


    private void getCartPrompt() {
        userRef.child(userId).child("Cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()){
                    snackbar.show();

                    int temp=0;
                    int sum = 0;
                    int tempCount=0;
                    int countSum=0;

                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                        inCartId=dataSnapshot.child("retailerId").getValue().toString();
                        temp= Integer.parseInt(dataSnapshot.child("price").getValue().toString());
                        tempCount=Integer.parseInt(dataSnapshot.child("count").getValue().toString());
                        countSum=countSum+tempCount;
                        sum=sum+temp;

                    }
                    if (retailerId.equals(inCartId)){
                        isCartHasItemsFromOtherRetailer=false;
                        snackbarText.setText("Items: "+countSum+" "+" "+getResources().getString(R.string.Rs)+" "+sum);
                        snackbarCartBtn.setText("Cart");
                        snackbarCartBtn.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_cart_btn, 0);
                    }
                    else{
                        isCartHasItemsFromOtherRetailer=true;
                        snackbarText.setTextColor(Color.RED);
                        snackbarText.setText("You have items from different shop in your cart!");
                        snackbarCartBtn.setText("Clear cart");
                        snackbarCartBtn.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_substract, 0);
                    }
                }
                else{
                    snackbar.dismiss();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void getProducts(Query query) {

        itemList.clear();
        itemListAll.clear();
        keyList.clear();
        itemAdapter.notifyDataSetChanged();

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ProductModel model=dataSnapshot.getValue(ProductModel.class);
                    itemList.add(model);
                    itemListAll.add(model);
                    keyList.add(dataSnapshot.getKey());

                }
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }





    private void init() {
        // getting resource
        res=this.getResources();



        //firebase authentication
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

        //getting data from intent
        name=getIntent().getStringExtra(NAME_TAG);
        phone=getIntent().getStringExtra(PHONE_TAG);
        image=getIntent().getStringExtra(IMAGE_TAG);
        category=getIntent().getStringExtra(CATEGORY_TAG);
        sublocality=getIntent().getStringExtra(SUBLOCALITY_TAG);
        distance=getIntent().getStringExtra(DISTANCE_TAG);
        retailerId=getIntent().getStringExtra(KEY_TAG);



        // setting up retailer data

        Picasso.get().load(image).placeholder(R.drawable.ic_image_placeholder).networkPolicy(NetworkPolicy.OFFLINE).into(binding.productShopImage, new Callback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(image).placeholder(R.drawable.ic_image_placeholder).into(binding.productShopImage);
            }
        });

        binding.collapsedToolbar.setTitle(name);

        // getting palette color
        getPaletteAsync();

        binding.appbarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset==0){ // expanded
                    binding.toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent,null));
                    binding.collapsedToolbar.setContentScrimColor(getResources().getColor(android.R.color.transparent,null));
                }
                if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()){ //collapsed
                    binding.toolbar.setBackgroundColor(DominantRGB);
                    binding.collapsedToolbar.setContentScrimColor(DominantRGB);
                }
            }
        });


        // setting up recyclerView
        itemLinearLayoutManager=new GridLayoutManager(this,2);
        subcategoryLinearLayoutManager=new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false);

        subcategoryLinearLayoutManager.isAutoMeasureEnabled();
        // subcategoryLinearLayoutManager.isSmoothScrollbarEnabled();
        binding.productSubcategoryList.setHasFixedSize(true);
        binding.productSubcategoryList.setLayoutManager(subcategoryLinearLayoutManager);
        binding.productSubcategoryList.setNestedScrollingEnabled(false);

        itemLinearLayoutManager.isAutoMeasureEnabled();
        binding.productList.setHasFixedSize(true);
        binding.productList.setLayoutManager(itemLinearLayoutManager);
        binding.productList.setNestedScrollingEnabled(true);
        binding.productList.setAdapter(itemAdapter);


        // query definition
        itemQuery=productRef.child(retailerId);
        subCategoryQuery=subcategoryRef.child(category).orderByChild("name");

        // snackbar
        customSnackbar();

        customSearchView();

    }

    private void customSearchView() {

        ImageView searchViewIcon = (ImageView)binding.productSearchView.findViewById(R.id.search_mag_icon);

        //Get parent of gathered icon
        ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
        //Remove it from the left...
        linearLayoutSearchView.removeView(searchViewIcon);
        //then put it back (to the right by default)
        linearLayoutSearchView.addView(searchViewIcon);
        EditText searchEditText = binding.productSearchView.findViewById(R.id.search_src_text);
        searchEditText.setHint("Search");
        searchEditText.setHintTextColor(Color.BLACK);
        ImageView imvClose = binding.productSearchView.findViewById(R.id.search_close_btn);
        imvClose.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_close_color));

        binding.productSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                itemAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                itemAdapter.getFilter().filter(newText);
                return false;
            }
        });

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    binding.appbarLayout.setExpanded(false);
                }
            }
        });


    }

    private void customSnackbar() {
        snackbar=Snackbar.make(binding.coordinatorLayout,"",Snackbar.LENGTH_INDEFINITE);
        View customSnack=getLayoutInflater().inflate(R.layout.snackbar_layout,null);

        snackbar.getView().setBackground(ResourcesCompat.getDrawable(res,R.drawable.round_edges,null));
        Snackbar.SnackbarLayout snackbarLayout= (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0,0,0,0);

        snackbarText=customSnack.findViewById(R.id.snackback_text);
        snackbarCartBtn=customSnack.findViewById(R.id.snackbar_cartBtn);




        // snackbar action button listener
        snackbarCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCartHasItemsFromOtherRetailer){
                    userRef.child(userId).child("Cart").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            snackbar.dismiss();
                        }
                    });
                }
                else{
                    Intent intent=new Intent(ProductListActivity.this,CartActivity.class);
                    startActivity(intent);
                }
            }
        });

        snackbarLayout.addView(customSnack,0);


    }

    private void getPaletteAsync() {
        Drawable drawable=binding.productShopImage.getDrawable();
        BitmapDrawable bitmapDrawable= (BitmapDrawable) drawable;

        Bitmap bitmap=bitmapDrawable.getBitmap();
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onGenerated(@Nullable Palette palette) {
                assert palette != null;

                Palette.Swatch swatch1=palette.getDominantSwatch();
                if (swatch1!=null){

                    DominantRGB=swatch1.getRgb();
                  //  binding.productContactBtn.setBackgroundTintList(ColorStateList.valueOf(DominantRGB));
                    getWindow().setStatusBarColor(DominantRGB);

                }

                GradientDrawable drawable = (GradientDrawable)snackbarCartBtn.getBackground();
                drawable.setStroke(3, DominantRGB); // set stroke width and stroke color

                GradientDrawable badgeDrawable= (GradientDrawable) binding.productCartBtnBadge.getBackground();
                badgeDrawable.setColor(DominantRGB);

                binding.productCartBtn.setImageTintList(ColorStateList.valueOf(DominantRGB));

                if(isColorDark(DominantRGB)){

                    binding.collapsedToolbar.setExpandedTitleColor(res.getColor(R.color.white,null));
                    binding.collapsedToolbar.setCollapsedTitleTextColor(res.getColor(R.color.white,null));



                }
                else{

                    binding.collapsedToolbar.setExpandedTitleColor(res.getColor(R.color.black,null));
                    binding.collapsedToolbar.setCollapsedTitleTextColor(res.getColor(R.color.black,null));

                }


            }
        });


    }

    public  boolean isColorDark(int color){
        double darkness = 1-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255;
        // It's a dark color
        return !(darkness < 0.5); // It's a light color
    }





    private static class SubcategoryViewHolder extends RecyclerView.ViewHolder {
        ProductSubcategoryListItemBinding subcategoryBinding;
        public SubcategoryViewHolder(ProductSubcategoryListItemBinding tb) {
            super(tb.getRoot());
            subcategoryBinding=tb;


        }
    }


}