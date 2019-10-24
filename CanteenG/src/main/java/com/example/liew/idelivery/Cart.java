package com.example.liew.idelivery;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liew.idelivery.Common.Common;
import com.example.liew.idelivery.Database.Database;
import com.example.liew.idelivery.Helper.RecyclerItemTouchHelper;
import com.example.liew.idelivery.Interface.RecyclerItemTouchHelperListener;
import com.example.liew.idelivery.Model.Order;
import com.example.liew.idelivery.Model.Request;
import com.example.liew.idelivery.ViewHolder.CartAdapter;
import com.example.liew.idelivery.ViewHolder.CartViewHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity implements  RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    public TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;








    //declare root layout
    RelativeLayout rootLayout;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //add calligraphy
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
        .setDefaultFontPath("fonts/restaurant_font.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());

        setContentView(R.layout.activity_cart);



        //init rootlayout
        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);



        //Firebase
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,ItemTouchHelper.LEFT,this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cart.size() > 0)
                    showAlertDialog();
                else
                    Toast.makeText(Cart.this, "Your cart is empty.", Toast.LENGTH_SHORT).show();

            }
        });

        loadListFood();
    }

    private void showAlertDialog(){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("is Confirm Your oreder?: ");

           LayoutInflater inflater = this.getLayoutInflater();
           View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);
          final MaterialEditText edtComment = (MaterialEditText)order_address_comment.findViewById(R.id.edtComment);


           alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {

                       //create new request
                       Request request = new Request(
                               Common.currentUser.getPhone(),
                               Common.currentUser.getName(),
                               Common.currentUser.getHomeAddress(),
                               txtTotalPrice.getText().toString(),
                               "0",
                               edtComment.getText().toString(),
                               "onspot payment",
                               cart
                       );

                       //submit to firebase
                       String order_number = String.valueOf(System.currentTimeMillis());
                       requests.child(order_number).setValue(request);

                       //delete cart
                       new Database(getBaseContext()).cleanCart(Common.currentUser.getPhone());

                   Toast.makeText(Cart.this, "Order Confirm Successfully", Toast.LENGTH_SHORT).show();
                   }
           });

           alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();

                   //remove fragment
                   getFragmentManager().beginTransaction()
                           .remove(getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment))
                           .commit();
               }
           });


        alertDialog.show();


    }




    private void loadListFood() {

        cart = new Database(this).getCarts(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculation total price
        float total = 0;
        for(Order order:cart)
            total +=(Float.parseFloat(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en","IN");
        java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
       txtTotalPrice.setText(fmt.format(total));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {

        //remove item at List<Order> by position
        cart.remove(position);

        //after that,delete all old data from SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());

        //final,update new data from List<Order> to SQLite
        for (Order item:cart)
            new Database(this).addToCart(item);

        //refresh
        loadListFood();
    }








    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder)
        {
            String name = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter)recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());

            final int deleteIndex = viewHolder.getAdapterPosition();
            adapter.removeItem(deleteIndex);

            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());

            //update txttotal
            //calculation total price
            float total = 0;
            List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
            for(Order item:orders)
                total +=(Float.parseFloat(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en","IN");
            java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
           txtTotalPrice.setText(fmt.format(total));


            //snackbar
            Snackbar snackbar = Snackbar.make(rootLayout,name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    adapter.restoreItem(deleteItem,deleteIndex);
                    new Database(getBaseContext()).addToCart(deleteItem);

                    //update txttotal
                    //calculation total price
                    float total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCarts(Common.currentUser.getPhone());
                    for(Order item:orders)
                        total +=(Float.parseFloat(item.getPrice()))*(Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en","IN");
                    java.text.NumberFormat fmt = java.text.NumberFormat.getCurrencyInstance(locale);
                    txtTotalPrice.setText(fmt.format(total));
                }
            });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
        }
    }
}

