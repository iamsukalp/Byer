package com.byer.byer.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.byer.byer.Models.CartModel;
import com.byer.byer.R;
import com.byer.byer.databinding.ActivityCartBinding;
import com.byer.byer.databinding.CartListItemBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static android.view.LayoutInflater.from;
import static com.byer.byer.Byer.retailerRef;
import static com.byer.byer.Byer.three_to_five;
import static com.byer.byer.Byer.two;
import static com.byer.byer.Byer.two_to_three;
import static com.byer.byer.Byer.userRef;

public class CartActivity extends AppCompatActivity {

   /* String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";
    private static final int GOOGLE_PAY_REQUEST_CODE = 123;*/
    private static final int UPI_PAYMENT=124;

    private ActivityCartBinding binding;
    private FirebaseAuth mAuth;
    private String userId,retailerId;
    private int orderTotal,deliveryFee,totalAmount;

    private Query query;
    private FirebaseRecyclerOptions<CartModel> options;
    private FirebaseRecyclerAdapter<CartModel,CartViewHolder> adapter;
    private int distance;

    private static final String NAME_TAG="name";
    private static final String PHONE_TAG="phone";
    private static final String IMAGE_TAG="image";
    private static final String SUBLOCALITY_TAG="sublocality";
    private static final String DISTANCE_TAG="distance";
    private static final String KEY_TAG="key";
    private static final String CATEGORY_TAG ="category" ;
    private static final String ORDER_ID_TAG="orderId";
    private static final String RETAILER_ID_TAG="retailerId";

    private String category;
    private String shopName;
    private String image;
    private String sublocality,phone;
    private boolean isHomeDelivery=false;
    private boolean isCash=false;
    private String orderID;
    private String paymentMode="UPI";
    private String deliveryMode="Pick Up";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();


        getCartData();

        // click listeners

        binding.cartAddMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(CartActivity.this, ProductListActivity.class);
                intent.putExtra(NAME_TAG,shopName);
                intent.putExtra(PHONE_TAG,phone);
                intent.putExtra(IMAGE_TAG,image);
                intent.putExtra(SUBLOCALITY_TAG,sublocality);
                intent.putExtra(DISTANCE_TAG,distance+"");
                intent.putExtra(KEY_TAG,retailerId);
                intent.putExtra(CATEGORY_TAG,category);

                startActivity(intent);
            }
        });

        binding.cartPlaceOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCash){
                    placeOrder();
                }
                else{
                    startPaymentFlow();
                }

            }
        });

        binding.cartDeliveryMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               if (checkedId==R.id.cart_delivery_home){
                   isHomeDelivery=true;
                   deliveryMode="Home Delivery";

               }
               else{
                   isHomeDelivery=false;
                   deliveryMode="Pick Up";
               }
                getDeliveryFee(distance);
                getTotalAmount();
            }
        });


        binding.cartPaymentMode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId==R.id.cart_payment_upi){
                    isCash=false;
                    paymentMode="UPI";
                }
                else{
                    isCash=true;
                    paymentMode="Cash";
                }
            }
        });

    }

    private void startPaymentFlow() {

    Uri uri =
                        new Uri.Builder()
                                .scheme("upi")
                                .authority("pay")
                                .appendQueryParameter("pa", "sukalp18@okaxis")
                                .appendQueryParameter("pn", shopName)
                             //   .appendQueryParameter("mc", "your-merchant-code")
                            //    .appendQueryParameter("tr", "your-transaction-ref-id")
                           //     .appendQueryParameter("tn", "your-transaction-note")
                                .appendQueryParameter("am", String.valueOf(totalAmount))
                                .appendQueryParameter("cu", "INR")
                              //  .appendQueryParameter("url", "your-transaction-url")
                                .build();
             /*   Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                intent.setPackage(GOOGLE_PAY_PACKAGE_NAME);
                startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE);*/

                Intent upiPayIntent = new Intent(Intent.ACTION_VIEW);
                upiPayIntent.setData(uri);
// will always show a dialog to user to choose an app
                Intent chooser = Intent.createChooser(upiPayIntent, "Pay with");

// check if intent resolves
                if(null != chooser.resolveActivity(getPackageManager())) {
                    startActivityForResult(chooser, UPI_PAYMENT);
                } else {
                    Toast.makeText(CartActivity.this,"No UPI app found, please install one to continue",Toast.LENGTH_SHORT).show();
                }
            }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == UPI_PAYMENT) {
            ArrayList<String> dataList = new ArrayList<>();
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {

                if (data != null) {

                    String trxt = data.getStringExtra("response");
                    dataList.add(trxt);
                    upiPaymentDataOperation(dataList);

                } else {

                    dataList.add("nothing");
                    upiPaymentDataOperation(dataList);
                }


            }

            else {
                //when user simply back without payment
                   Log.e("UPI", "onActivityResult: " + "Return data is null");
                   dataList.add("nothing");
                   upiPaymentDataOperation(dataList);




            }


        }
    }

    private void upiPaymentDataOperation(ArrayList<String> data) {
        if (isConnectionAvailable(CartActivity.this)) {
            String str = data.get(0);
            Log.e("UPIPAY", "upiPaymentDataOperation: "+str);
            String paymentCancel = "";
            if(str == null) str = "discard";
            String status = "";
            String approvalRefNo = "";
            String response[] = str.split("&");
            for (int i = 0; i < response.length; i++) {
                String equalStr[] = response[i].split("=");
                if(equalStr.length >= 2) {
                    if (equalStr[0].toLowerCase().equals("Status".toLowerCase())) {
                        status = equalStr[1].toLowerCase();
                    }
                    else if (equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase()) || equalStr[0].toLowerCase().equals("txnRef".toLowerCase())) {
                        approvalRefNo = equalStr[1];
                    }
                }
                else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                //Code to handle successful transaction here.

                binding.cartViewSwitcher.setDisplayedChild(1);
                getWindow().setNavigationBarColor(getResources().getColor(R.color.logo_color,null));
                getWindow().setStatusBarColor(getResources().getColor(R.color.logo_color,null));
                binding.cartViewSwitcher.setBackgroundColor(getResources().getColor(R.color.logo_color,null));
                binding.cartPaymentStatusAnim.setAnimation(R.raw.success_anim);
                binding.cartPaymentStatusText.setText("Transaction successful!\n"+"Reference no. "+approvalRefNo);

            }
            else if("Payment cancelled by user.".equals(paymentCancel)) {


                    binding.cartViewSwitcher.setDisplayedChild(1);
                    getWindow().setNavigationBarColor(getResources().getColor(R.color.payment_failed_bg,null));
                    getWindow().setStatusBarColor(getResources().getColor(R.color.payment_failed_bg,null));
                    binding.cartViewSwitcher.setBackgroundColor(getResources().getColor(R.color.payment_failed_bg,null));
                    binding.cartPaymentStatusAnim.setAnimation(R.raw.failure_anim);
                    binding.cartPaymentStatusText.setText("Payment cancelled by user!");
                    placeOrder();

            }
            else {
                binding.cartViewSwitcher.setDisplayedChild(1);
                getWindow().setNavigationBarColor(getResources().getColor(R.color.payment_failed_bg,null));
                getWindow().setStatusBarColor(getResources().getColor(R.color.payment_failed_bg,null));
                binding.cartViewSwitcher.setBackgroundColor(getResources().getColor(R.color.payment_failed_bg,null));
                binding.cartPaymentStatusAnim.setAnimation(R.raw.failure_anim);
                binding.cartPaymentStatusText.setText("Transaction failed.Please try again!");

            }
        } else {
            binding.cartViewSwitcher.setDisplayedChild(1);
            getWindow().setNavigationBarColor(getResources().getColor(R.color.white,null));
            getWindow().setStatusBarColor(getResources().getColor(R.color.white,null));
            binding.cartPaymentStatusAnim.setAnimation(R.raw.no_connection_anim);
            binding.cartPaymentStatusText.setText("Internet connection is not available.\nPlease check and try again");
            binding.cartPaymentStatusText.setTextColor(getResources().getColor(R.color.grey,null));


        }
    }

    private void placeOrder() {

        final HashMap<String,Object> orderMap = new HashMap<>();
        orderMap.put("amount",totalAmount+"");
        orderMap.put("retailerId",retailerId);
        orderMap.put("userName",HomeActivity.userName);
        orderMap.put("shopName",shopName);
        orderMap.put("userId",userId);
        orderMap.put("paymentMode",paymentMode);
        orderMap.put("deliveryMode",deliveryMode);
        orderMap.put("category",category);
        orderMap.put("timestamp", ServerValue.TIMESTAMP);
        orderMap.put("status","new");

        getOrderID();

        retailerRef.child(category).child(retailerId).child("Orders").child(orderID).setValue(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){


                        userRef.child(userId).child("Orders").child(orderID).setValue(orderMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        addOrderItems(orderID);
                                    }
                                    else{
                                        Log.d("ORDER_LOG",task.getException().getMessage());
                                        Toast.makeText(CartActivity.this, "Couldn't Place the Order! Try Again!", Toast.LENGTH_SHORT).show();
                                    }
                            }
                        });

                }
                else{
                    Log.d("ORDER_LOG",task.getException().getMessage());
                    Toast.makeText(CartActivity.this, "Couldn't Place the Order! Try Again!", Toast.LENGTH_SHORT).show();
                }
            }
        })  ;


    }

    private void addOrderItems(final String orderID) {
           query.addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                   for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                       String key=dataSnapshot.getKey();
                       final HashMap<String, Object> itemMap = new HashMap<>();
                       itemMap.put("name", dataSnapshot.child("name").getValue().toString());
                       itemMap.put("count", dataSnapshot.child("count").getValue().toString());
                       itemMap.put("quantity", dataSnapshot.child("quantity").getValue().toString());
                       itemMap.put("price", dataSnapshot.child("price").getValue().toString());
                       itemMap.put("unit",dataSnapshot.child("unit").getValue().toString());


                       retailerRef.child(category).child(retailerId).child("Orders").child(orderID).child("Items").child(key).setValue(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if (!task.isSuccessful()) {
                                   Log.d("ORDER_ITEM_LOG", task.getException().getMessage());
                                   Toast.makeText(CartActivity.this, "Couldn't add items to the order!", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });

                       userRef.child(userId).child("Orders").child(orderID).child("Items").child(key).setValue(itemMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull @NotNull Task<Void> task) {
                               if (!task.isSuccessful()) {
                                   Log.d("ORDER_ITEM_LOG", task.getException().getMessage());
                                   Toast.makeText(CartActivity.this, "Couldn't add items to the order!", Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                   }

                   takeUserToOrderTrackingPage();
               }

               @Override
               public void onCancelled(@NonNull @NotNull DatabaseError error) {

               }
           });
    }


    private void getOrderID() {
        orderID=retailerRef.child(category).child(retailerId).child("Orders").push().getKey();
    }
    private void takeUserToOrderTrackingPage() {
       Handler handler=new Handler();
       handler.postDelayed(new Runnable() {
           @Override
           public void run() {
               Intent intent=new Intent(CartActivity.this,TrackOrderActivity.class);
               intent.putExtra(ORDER_ID_TAG,orderID);
               intent.putExtra(RETAILER_ID_TAG,retailerId);
               intent.putExtra(CATEGORY_TAG,category);
               startActivity(intent);
               finish();
           }
       },3000);

    }

    public static boolean isConnectionAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected()
                    && netInfo.isConnectedOrConnecting()
                    && netInfo.isAvailable();
        }
        return false;
    }
    private void getCartTotal() {
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int sum=0;
                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    int temp= Integer.parseInt(dataSnapshot.child("price").getValue().toString());
                    sum=sum+temp;

                }

                orderTotal=sum;
                binding.cartOrderTotal.setText(sum+"");
                getTotalAmount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalAmount() {
        totalAmount=orderTotal+deliveryFee;
        binding.cartTotal.setText(totalAmount+"");

    }

    private void getCartData() {



        options=new FirebaseRecyclerOptions.Builder<CartModel>().setQuery(query,CartModel.class).build();
        adapter=new FirebaseRecyclerAdapter<CartModel, CartViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull final CartViewHolder holder, final int position, @NonNull final CartModel model) {

                category=model.getCategory();
                retailerId=model.getRetailerId();
                distance= Integer.parseInt(model.getDistance());
                getDeliveryFee(distance);

                holder.cartBinding.cartListName.setText(model.getName());
                holder.cartBinding.cartListQuantity.setText(model.getQuantity()+" "+model.getUnit());
                holder.cartBinding.cartListCount.setText(model.getCount());
                holder.cartBinding.cartListPrice.setText(getResources().getString(R.string.Rs)+" "+model.getPrice());

                holder.cartBinding.cartListAdd.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        int count;
                        count= Integer.parseInt(holder.cartBinding.cartListCount.getText().toString());
                        holder.cartBinding.cartListCount.setText((count+1)+"");
                        holder.cartBinding.cartListAdd.setEnabled(false);
                        holder.cartBinding.cartListSubstract.setEnabled(false);
                        updateCart(holder,adapter.getRef(position).getKey(),(Integer.parseInt(model.getPrice())/(count)),(count+1));
                    }
                });

                holder.cartBinding.cartListSubstract.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(View v) {
                        int count;
                        holder.cartBinding.cartListAdd.setEnabled(false);
                        holder.cartBinding.cartListSubstract.setEnabled(false);
                        count= Integer.parseInt(holder.cartBinding.cartListCount.getText().toString());
                        if (count==1){

                            removeCart(adapter.getRef(position).getKey());
                        }
                        else{
                            holder.cartBinding.cartListCount.setText((count-1)+"");
                            updateCart(holder, adapter.getRef(position).getKey(),(Integer.parseInt(model.getPrice())/(count)),(count-1));
                        }
                    }
                });

                getCartTotal();
                getShopDetails();
            }

            @NonNull
            @Override
            public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new CartViewHolder(CartListItemBinding.inflate(from(parent.getContext()),parent,false));
            }
        };
        adapter.startListening();
        binding.cartList.setAdapter(adapter);
    }

    private void getDeliveryFee(int distance) {
        if (isHomeDelivery){
            if (distance <= 2){
                deliveryFee=Integer.parseInt(two);
            }
            else if (distance <=3){
                deliveryFee=Integer.parseInt(two_to_three);
            }
            else if ( distance <=5){
                deliveryFee=Integer.parseInt(three_to_five);
            }else{
                deliveryFee=100;
            }
        }
        else{
            deliveryFee=0;
        }
        binding.cartDeliveryCharge.setText(deliveryFee+"");
    }

    private void removeCart(String key) {
        userRef.child(userId).child("Cart").child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(CartActivity.this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                    getCartTotal();
                }
                else{
                    userRef.child(userId).child("Cart").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Intent intent =new Intent(CartActivity.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else{
                                Toast.makeText(CartActivity.this, "Couldn't update the cart", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    private void updateCart(CartViewHolder holder, String key, int price, int i) {

        HashMap<String, Object> cartMap=new HashMap<>();
        cartMap.put("count",i+"");
        cartMap.put("price",(price*i)+"");
        userRef.child(userId).child("Cart").child(key).updateChildren(cartMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(CartActivity.this, "Couldn't update cart", Toast.LENGTH_SHORT).show();
                }
                holder.cartBinding.cartListAdd.setEnabled(true);
                holder.cartBinding.cartListSubstract.setEnabled(true);

                getCartTotal();
            }
        });

    }

    private void getShopDetails() {
        retailerRef.child(category).child(retailerId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shopName= Objects.requireNonNull(snapshot.child("name").getValue()).toString();
                image=snapshot.child("image").getValue().toString();
                if (snapshot.hasChild("sublocality")){

                    sublocality= snapshot.child("sublocality").getValue().toString();
                }else{
                    sublocality="location";
                }

                phone=snapshot.child("phone").getValue().toString();

                binding.cartShopName.setText(shopName);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }









    private void init() {
        // firebase auth
        mAuth=FirebaseAuth.getInstance();
        userId=mAuth.getCurrentUser().getUid();

        // firebase query
        query= userRef.child(userId).child("Cart");

        //setting up recycler view
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this);
        binding.cartList.setLayoutManager(linearLayoutManager);
        binding.cartList.setHasFixedSize(true);

        // default delivery mode Pickup
        binding.cartDeliveryMode.check(R.id.cart_delivery_pickup);

        //default payment mode upi
        binding.cartPaymentMode.check(R.id.cart_payment_upi);


    }



    private class CartViewHolder extends RecyclerView.ViewHolder {
        CartListItemBinding cartBinding;
        public CartViewHolder(CartListItemBinding cb) {
            super(cb.getRoot());
            cartBinding=cb;
        }
    }
}