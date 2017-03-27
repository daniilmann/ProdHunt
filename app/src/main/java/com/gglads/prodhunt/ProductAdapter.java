package com.gglads.prodhunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gglads.prodhunt.Entities.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProdItemHolder> {

    private List<Product> products = null;

    public ProductAdapter() {
        this(new ArrayList<Product>());
    }

    public ProductAdapter(List<Product> products) {
        this.products = products;
    }

    @Override
    public ProdItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_item, parent, false);
        return new ProdItemHolder(item);
    }

    @Override
    public void onBindViewHolder(ProdItemHolder holder, int position) {
        Product prod = products.get(position);
        prod.loadThumb(holder.getThumb());
        holder.setName(prod.getName());
        holder.setDesc(prod.getDesc());
        holder.setUpvote(prod.getUpvotes());
        holder.setScreenLink(prod.getScreenLink());
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void addProduct(Product product) {
        if (product != null && !products.contains(product)) {
            products.add(product);
            notifyDataSetChanged();
        }
    }

    public void addProducts(List<Product> products) {
        if (products != null) {
            products.removeAll(this.products);
            this.products.addAll(products);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        products.clear();
        notifyDataSetChanged();
    }

    public static class ProdItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView thumb = null;
        private TextView name = null;
        private TextView desc = null;
        private TextView upvote = null;
        private String screenLink = null;

        public ProdItemHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            thumb = (ImageView) view.findViewById(R.id.prod_thumb_iv);
            name = (TextView) view.findViewById(R.id.prod_name_tv);
            desc = (TextView) view.findViewById(R.id.prod_desc_tv);
            upvote = (TextView) view.findViewById(R.id.prod_upvote_tv);
        }

        public ImageView getThumb() {
            return thumb;
        }

        public void setThumb(Bitmap thumb) {
            if (this.thumb != null && thumb != null)
                this.thumb.setImageBitmap(thumb);
        }

        public TextView getName() {
            return name;
        }

        public void setName(String name) {
            if (this.name != null && name != null)
                this.name.setText(name);
        }

        public TextView getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            if (this.desc != null & desc != null)
                this.desc.setText(desc);
        }

        public TextView getUpvote() {
            return upvote;
        }

        public void setUpvote(String upvote) {
            if (this.upvote != null)
                this.upvote.setText(upvote);
        }

        public void setScreenLink(String link) {
            screenLink = link;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(itemView.getContext(), ProductActivity.class);
            intent.putExtra("NAME", name.getText());
            intent.putExtra("DESC", desc.getText());
            intent.putExtra("IMG", screenLink);
            itemView.getContext().startActivity(intent);
        }
    }
}
